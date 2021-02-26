package org.apache.airavata.mft.secret.server.backend.custos;

import org.apache.custos.clients.CustosClientProvider;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CustosClientsFactory {

    private String custosHost;

    private int custosPort;

    private Map<String, CustosClientProvider> custosClientProviderMap = new ConcurrentHashMap<>();

    public CustosClientsFactory(String custosHost, int custosPort, String custosId, String custosSecret) {
        this.custosHost = custosHost;
        this.custosPort = custosPort;
        CustosClientProvider custosClientProvider = new CustosClientProvider.Builder().setServerHost(custosHost)
                .setServerPort(custosPort)
                .setClientId(custosId)
                .setClientSec(custosSecret).build();
        custosClientProviderMap.put(custosId, custosClientProvider);

    }


    public CustosClientProvider getCustosClientProvider(String custosId, String custosSecret) {

        if (custosClientProviderMap.containsKey(custosId)) {
            return custosClientProviderMap.get(custosId);
        }

        CustosClientProvider custosClientProvider = new CustosClientProvider.Builder().setServerHost(custosHost)
                .setServerPort(custosPort)
                .setClientId(custosId)
                .setClientSec(custosSecret).build();
        custosClientProviderMap.put(custosId, custosClientProvider);
        return custosClientProvider;

    }

    public Optional<CustosClientProvider> getCustosClientProvider(String custosId) {

        if (custosClientProviderMap.containsKey(custosId)) {
            return Optional.of(custosClientProviderMap.get(custosId));
        }
        return Optional.empty();

    }


}
