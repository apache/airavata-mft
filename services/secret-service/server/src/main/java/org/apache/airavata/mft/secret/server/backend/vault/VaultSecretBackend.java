package org.apache.airavata.mft.secret.server.backend.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Seal;
import com.bettercloud.vault.api.mounts.*;
import com.bettercloud.vault.response.LogicalResponse;
import com.bettercloud.vault.response.MountResponse;
import com.bettercloud.vault.response.SealResponse;
import com.google.gson.Gson;
import org.apache.airavata.mft.credential.stubs.azure.*;
import org.apache.airavata.mft.credential.stubs.box.*;
import org.apache.airavata.mft.credential.stubs.dropbox.*;
import org.apache.airavata.mft.credential.stubs.ftp.*;
import org.apache.airavata.mft.credential.stubs.gcs.*;
import org.apache.airavata.mft.credential.stubs.http.*;
import org.apache.airavata.mft.credential.stubs.odata.*;
import org.apache.airavata.mft.credential.stubs.s3.*;
import org.apache.airavata.mft.credential.stubs.scp.*;
import org.apache.airavata.mft.credential.stubs.swift.*;
import org.apache.airavata.mft.secret.server.backend.SecretBackend;
import org.apache.airavata.mft.secret.server.backend.vault.entity.SCPSecretEntity;
import org.dozer.DozerBeanMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class VaultSecretBackend implements SecretBackend {
    private  Vault vault;

    private VaultInitData vaultGlobalCreds = null;

    private final Map<String, String> pathMap = new HashMap<>();

    private final String vaultCredsPath="./airavata-mft/vault/keys/unseal.keys";

    private final String VAULT_ADDR = "http://127.0.0.1:8200";


    private final DozerBeanMapper mapper = new DozerBeanMapper();

    // How to start vault server and initialize the vault through API
    // Implement

    // Reference: https://stackoverflow.com/questions/1053467/how-do-i-save-a-string-to-a-text-file-using-java

    public VaultInitData setVaultInit(String body) {
        VaultInitData vaultInitData = new VaultInitData();
        JSONObject field = new JSONObject(body);
        JSONArray keysArray = field.getJSONArray("keys");
        JSONArray keysBase64Array = field.getJSONArray("keys_base64");
        String root_token = field.getString("root_token");

        String[] keys = new String[keysArray.length()];
        for(int j=0; j<keysArray.length(); ++j) {
            keys[j] = keysArray.getString(j);
        }

        String[] keys_base64 = new String[keysArray.length()];
        for(int j=0; j<keysBase64Array.length(); ++j){
            keys_base64[j] = keysBase64Array.getString(j);
        }
        vaultInitData.setKeys(keys);
        vaultInitData.setKeys_base64(keys_base64);
        vaultInitData.setRoot_token(root_token);

        return vaultInitData;
    }

    private VaultInitData initializeVault() throws IOException {
        String body = "{\"secret_shares\": 1, \"secret_threshold\": 1}";
        URL url = new URL(VAULT_ADDR + "/v1/sys/init");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        try (DataOutputStream dos = new DataOutputStream(connection.getOutputStream())) {
            dos.writeBytes(body);
        }

        StringBuilder response= new StringBuilder();
        try (BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = bf.readLine()) != null) {
                response.append(line);
            }
        }

        // Before returning write it to a file

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(vaultCredsPath))) {
            writer.write(response.toString());
        } catch (IOException e) {
            System.out.println("Error while writing the key file");
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return setVaultInit(response.toString());
    }

    private VaultInitData readVaultCreds() {
        StringBuilder output = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(vaultCredsPath))) {
            // Reference: https://stackoverflow.com/questions/5868369/how-can-i-read-a-large-text-file-line-by-line-using-java
            for (String line; (line=bufferedReader.readLine()) != null; ) {
                output.append(line);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: FileNotFoundException in reading the vault creds file");
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println("Error: IOException in reading vault creds file");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return setVaultInit(output.toString());
    }

    private boolean unsealVault(Seal seal) {
        SealResponse sealResponse;
        try {
            sealResponse = seal.unseal(vaultGlobalCreds.getKeys()[0]);
        } catch (VaultException ve) {
            ve.printStackTrace();
            System.out.println("Error: Unsealing with Key 1 failed");
            throw new RuntimeException("Error: Unsealing with Key 1 failed");
        }
        return sealResponse.getSealed();
    }

    private void mountPath() {
        Mounts mounts = vault.mounts();

        final MountPayload payloadLocal = new MountPayload()
                .defaultLeaseTtl(TimeToLive.of(100, TimeUnit.HOURS))
                .maxLeaseTtl(TimeToLive.of(100, TimeUnit.HOURS))
                .maxLeaseTtl(TimeToLive.of(100, TimeUnit.HOURS))
                .description("Expiration time is set for 100 hours");

        try {
            mounts.enable("secret", MountType.KEY_VALUE_V2, payloadLocal);
        } catch (VaultException e) {
            e.printStackTrace();
            System.out.println("Error: Unable to mount the path");
            throw new RuntimeException(e);
        }

    }

    private void populatePathsToMap() {
        String pathPrefix = "secret/";
        String[] entityList = {"azure", "ftp", "gcs", "odata", "s3", "scp"};

        int i=0;
        while (i< entityList.length) {
            pathMap.put(entityList[i], pathPrefix+entityList[i]);
            ++i;
        }
    }

    /**
     *
     */
    @Override
    public void init() {
        int VAULT_OPEN_TIMEOUT = 5;
        int VAULT_READ_TIMEOUT = 30;
        int VAULT_KV_VERSION = 2;

        VaultConfig config;
        String path="./airavata-mft/vault/vault-data";
        File f = new File(path);

        if (!f.isDirectory()) {
            try {
                vaultGlobalCreds = initializeVault();
            } catch (IOException e) {
                System.out.println("Error in init function");
                throw new RuntimeException(e);
            }
        } else {
            vaultGlobalCreds = readVaultCreds();
        }

        try {
            config = new VaultConfig()
                    .address(VAULT_ADDR)
                    .token(vaultGlobalCreds.getRoot_token())
                    .openTimeout(VAULT_OPEN_TIMEOUT)
                    .readTimeout(VAULT_READ_TIMEOUT)
                    .engineVersion(VAULT_KV_VERSION)
                    .build();
        } catch (VaultException ve) {
            System.out.println("Error: Vault Exception, unable to config and build");
            ve.printStackTrace();
            throw new RuntimeException(ve);
        }

        vault = new Vault(config);
        Seal sealLocal = vault.seal();

        SealResponse sealResponse;
        try {
            sealResponse = sealLocal.sealStatus();
        } catch (VaultException ve) {
            System.out.println("Error: Cannot get seal response");
            ve.printStackTrace();
            throw new RuntimeException(ve);
        }

        if (sealResponse.getSealed()) {
            boolean sealStatus = unsealVault(sealLocal);
            if (sealStatus) {
                System.out.println("Error: Unsealing vault failed");
                throw new RuntimeException("Error: Unsealing vault failed");
            }
        }

        try {
            MountResponse response = vault.mounts().list();
            Map<String, Mount> mounts = response.getMounts();
            if (!mounts.containsKey("secret/")) {
                mountPath();
            }
        } catch (VaultException e) {
            System.out.println("Error; Checking mounts failed");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        populatePathsToMap();
    }

    /**
     *
     */
    @Override
    public void destroy() {

    }

    /**
     * @param request
     * @return
     */
    @Override
    public SCPSecret createSCPSecret(SCPSecretCreateRequest request) {
        SCPSecretEntity entity = new SCPSecretEntity();
        Gson gson = new Gson();

        // Generate UUID
        String uuid = UUID.randomUUID().toString();
        entity.setSecretId(uuid);
        entity.setPrivateKey(request.getPrivateKey());
        entity.setPublicKey(request.getPublicKey());
        entity.setPassphrase(request.getPassphrase());
        entity.setUser(request.getUser());

        Map<String, Object> secrets = new HashMap<>();
        // SecretId is chosen as the key because SCPSecretGetRequest only has getters for SecretId
        secrets.put(entity.getSecretId(), gson.toJson(entity));

        try {
            vault.logical().write(pathMap.get("scp"), secrets);
        } catch (VaultException e) {
            e.printStackTrace();
            System.out.println("Error while writing secrets to secret/scp ");
            throw new RuntimeException(e);
        }

        return mapper.map(entity, SCPSecret.newBuilder().getClass()).build();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<SCPSecret> getSCPSecret(SCPSecretGetRequest request) throws Exception {
        String secretId = request.getSecretId();

        String readString;
        Gson gson = new Gson();

        try {
            readString = vault.withRetries(5, 1000)
                    .logical()
                    .read(pathMap.get("scp"))
                    .getData()
                    .get(secretId);
        } catch (VaultException e) {
            e.printStackTrace();
            System.out.println("Error in reading from vault in readWrite");
            throw new RuntimeException(e);
        }

        if (readString == null) {
            return Optional.empty();
        }

        SCPSecretEntity entity = gson.fromJson(readString, SCPSecretEntity.class);
        SCPSecret scpSecret = mapper.map(entity, SCPSecret.newBuilder().getClass()).build();

        return Optional.of(scpSecret);
    }



    /**
     * @param request
     * @return
     */
    @Override
    public boolean updateSCPSecret(SCPSecretUpdateRequest request) {
        Gson gson = new Gson();

        Map<String, String> readMap;

        try {
            readMap = vault.withRetries(5, 1000).logical().read(pathMap.get("scp")).getData();
        } catch (VaultException e) {
            e.printStackTrace();
            System.out.println("Error while reading the secrets in the deleteSecrets() method");
            throw new RuntimeException(e);
        }

        String value = readMap.get(request.getSecretId());
        SCPSecretEntity entity = gson.fromJson(value, SCPSecretEntity.class);

        // update & populate entity
        entity.setSecretId(request.getSecretId());
        entity.setUser(request.getUser());
        entity.setPassphrase(request.getPassphrase());
        entity.setPrivateKey(request.getPrivateKey());
        entity.setPublicKey(request.getPublicKey());

        // Modify the required kv pair in the read map
        readMap.put(request.getSecretId(), gson.toJson(entity));


        // Copy it to new map with <k, v> as <string, object>
        Map<String, Object> secrets = new HashMap<>(readMap);

        // write it again to vault
        try {
            vault.logical().write(pathMap.get("scp"), secrets);
        } catch (VaultException e) {
            e.printStackTrace();
            System.out.println("Error while writing secrets to secret/scp ");
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * @param request
     * @return
     */
    @Override
    public boolean deleteSCPSecret(SCPSecretDeleteRequest request) {
        Map<String, String> readMap;

        try {
            readMap = vault.withRetries(5, 1000).logical().read(pathMap.get("scp")).getData();
        } catch (VaultException e) {
            e.printStackTrace();
            System.out.println("Error while reading the secrets in the deleteSecrets() method");
            throw new RuntimeException(e);
        }

        if (readMap.keySet().size() <= 1) {
            try {
                vault.logical().delete(pathMap.get("scp"));
                return true;
            } catch (VaultException e) {
                e.printStackTrace();
                System.out.println("Error while deleting the secrets in the deleteSecrets() method");
                throw new RuntimeException(e);
            }
        }

        readMap.remove(request.getSecretId());

        Map<String, Object> secrets = new HashMap<>(readMap);

        try {
            vault.logical().write(pathMap.get("scp"), secrets);
        } catch (VaultException e) {
            System.out.println("Error while writing secrets to secret/scp ");
        }


        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<S3Secret> getS3Secret(S3SecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public S3Secret createS3Secret(S3SecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateS3Secret(S3SecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteS3Secret(S3SecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<BoxSecret> getBoxSecret(BoxSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public BoxSecret createBoxSecret(BoxSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateBoxSecret(BoxSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteBoxSecret(BoxSecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<AzureSecret> getAzureSecret(AzureSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public AzureSecret createAzureSecret(AzureSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateAzureSecret(AzureSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteAzureSecret(AzureSecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<GCSSecret> getGCSSecret(GCSSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public GCSSecret createGCSSecret(GCSSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateGCSSecret(GCSSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteGCSSecret(GCSSecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<DropboxSecret> getDropboxSecret(DropboxSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public DropboxSecret createDropboxSecret(DropboxSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateDropboxSecret(DropboxSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteDropboxSecret(DropboxSecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<FTPSecret> getFTPSecret(FTPSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public FTPSecret createFTPSecret(FTPSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateFTPSecret(FTPSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteFTPSecret(FTPSecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<SwiftSecret> getSwiftSecret(SwiftSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public SwiftSecret createSwiftSecret(SwiftSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateSwiftSecret(SwiftSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteSwiftSecret(SwiftSecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<ODataSecret> getODataSecret(ODataSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public ODataSecret createODataSecret(ODataSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateODataSecret(ODataSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteODataSecret(ODataSecretDeleteRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public Optional<HTTPSecret> getHttpSecret(HTTPSecretGetRequest request) throws Exception {
        return Optional.empty();
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public HTTPSecret createHttpSecret(HTTPSecretCreateRequest request) throws Exception {
        return null;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean updateHttpSecret(HTTPSecretUpdateRequest request) throws Exception {
        return false;
    }

    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public boolean deleteHttpSecret(HTTPSecretDeleteRequest request) throws Exception {
        return false;
    }
}

class VaultInitData {
    private String[] keys;
    private String[] keys_base64;
    private String root_token;

    public String[] getKeys() {
        return keys;
    }

    public String[] getKeys_base64() {
        return keys_base64;
    }

    public String getRoot_token() {
        return root_token;
    }

    public void setKeys(String[] keys) {
        this.keys = keys;
    }

    public void setKeys_base64(String[] keys_base64) {
        this.keys_base64 = keys_base64;
    }

    public void setRoot_token(String root_token) {
        this.root_token = root_token;
    }
}
