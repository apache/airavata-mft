Airavata Managed File Transfer Service and Clients

## Running from IDE

* Start consul server - ./consul agent -dev
* Start ResourceServiceApplication
* Start SecretServiceApplication
* Start MftController
* Start ApiServiceApplication

## Building from the script

* Go to scripts directory
* Run ```/bin/bash build.sh```
* This will build the whole project and unzip distributions of each service to the build direcotry of the root of the project

#### Defaults for Resource and Secret Service
* The Backends for Secret and Resource services are by default set to File based backend so you can find sample config json 
files in conf directory of each distribution
* You can easily change the backend by updating the applicationContext.xml file in conf directory

#### Starting the distribution
* You should have consul running inorder to start MFT. You can start consul by running ```/bin/bash start-consul.sh <os distribution>```. 
For example ```/bin/bash start-consul.sh mac```. You can see supported distributions by running ```/bin/bash start-consul.sh -h```
* If your OS distribution is not provided in the script, you can manually install Consul using pre compiled binaries https://www.consul.io/docs/install/index.html#precompiled-binaries
* To start MFT stack, run ```/bin/bash start-mft.sh```. This will start all the services and an Agent to transfer data
* To stop MFT stack, run ```/bin/bash start-mft.sh```
* If you want to see logs of any running service, run ```/bin/bash log.sh <service name>```. For example, ```/bin/bash log.sh secret```. 
To view available services, run  ```/bin/bash log.sh -h```

#### Sample API Call

* This sample is for the Java gRPC client of MFT API. Request resource ids and secret ids are analogous to default values 
in secret.json and resources.json files available for File based backends. 

* You should have mft-api-client dependency in your project

```
<dependency>
    <groupId>org.apache.airavata</groupId>
    <artifactId>mft-api-client</artifactId>
    <version>0.01-SNAPSHOT</version>
</dependency>
```

```
import org.apache.airavata.mft.api.client.MFTApiClient;
import org.apache.airavata.mft.api.service.*;

public class SampleClient {
    public static void main(String args[]) throws InterruptedException {

        MFTApiServiceGrpc.MFTApiServiceBlockingStub client = MFTApiClient.buildClient("localhost", 7004);

        String sourceId = "remote-ssh-resource2";
        String sourceToken = "local-ssh-cred";
        String destId = "remote-ssh-resource";
        String destToken = "local-ssh-cred";

        TransferApiRequest request = TransferApiRequest.newBuilder()
                .setSourceId(sourceId)
                .setSourceToken(sourceToken)
                .setSourceType("SCP")
                .setDestinationId(destId)
                .setDestinationToken(destToken)
                .setDestinationType("SCP")
                .setAffinityTransfer(false).build();

        // Submitting the transfer to MFT
        TransferApiResponse transferApiResponse = client.submitTransfer(request);
        while(true) {
            // Monitoring transfer status
            try {
                TransferStateApiResponse transferState = client.getTransferState(TransferStateApiRequest.newBuilder().setTransferId(transferApiResponse.getTransferId()).build());
                System.out.println("Latest Transfer State " + transferState.getState());

            } catch (Exception e) {
                System.out.println("Errored " + e.getMessage());
            }
            Thread.sleep(1000);
        }
    }
}
```