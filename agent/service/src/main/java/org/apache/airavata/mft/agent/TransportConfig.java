package org.apache.airavata.mft.agent;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "agent")
public class TransportConfig {


    private Map<String, String> transport;

    public Map<String, String> getTransport() {
        return transport;
    }

    public void setTransport(Map<String, String> transport) {
        this.transport = transport;
    }

}
