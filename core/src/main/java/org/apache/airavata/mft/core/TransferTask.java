package org.apache.airavata.mft.core;

import org.apache.airavata.mft.core.ConnectorContext;
import org.apache.airavata.mft.core.api.Connector;

import java.util.concurrent.Callable;

public class TransferTask implements Callable<Integer> {

    private Connector connector;
    private ConnectorContext context;

    public TransferTask(Connector connector, ConnectorContext context) {
        this.connector = connector;
        this.context = context;
    }

    @Override
    public Integer call() throws Exception {
        this.connector.startStream(context);
        return 0;
    }
}
