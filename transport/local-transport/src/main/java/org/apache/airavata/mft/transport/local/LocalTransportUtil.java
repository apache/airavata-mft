package org.apache.airavata.mft.transport.local;

public class LocalTransportUtil {

    public static LocalResourceIdentifier getLocalResourceIdentifier(String id) {
        LocalResourceIdentifier identifier = new LocalResourceIdentifier();

        switch (id) {
            case "1":
                identifier.setPath("/Users/dimuthu/data.csv");
                return identifier;
            case "2":
                identifier.setPath("/Users/dimuthu/new.txt");
                return identifier;
        }
        return null;
    }
}
