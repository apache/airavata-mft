package org.apache.airavata.mft.transport.scp;

import com.jcraft.jsch.JSchException;
import org.apache.airavata.mft.core.streaming.TransportStream;

import java.io.IOException;

public class Main {
    public static void main(final String[] arg) throws IOException, JSchException {

        final TransportStream stream = new TransportStream();

        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                int asInt;
                try {
                    long read = 0;
                    while (true) {
                        if (stream.getInputStream().available() > 0) {
                            asInt = stream.getInputStream().read();
                            read++;
                            //char c = (char)asInt;
                            //System.out.print(c);
                        } else {
                            if (stream.getStreamCompleted().get()) {
                                break;
                            } else {
                                try {
                                    Thread.sleep(100);
                                    System.out.println("Waiting " + read);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable r2 = new Runnable() {
            @Override
            public void run() {
                SCPReceiver receiver = new SCPReceiver();
                try {
                    receiver.receive("1", stream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        Thread t1 = new Thread(r1);
        t1.start();

        Thread t2 = new Thread(r2);
        t2.start();
        System.out.println("Done");
    }
}
