package org.apache.airavata.mft.command.line;

public final class CommandLineUtil {

    public static void printTable(int[] columnWidths, String[][] content) {
        for (String[] row : content) {
            for (int i = 0; i < columnWidths.length; i++) {
                System.out.print("|");
                for (int loc = 0; loc < columnWidths[i]; loc++) {
                    if (row[i].length() > loc) {
                        System.out.print(row[i].charAt(loc));
                    } else {
                        System.out.print(" ");
                    }
                }
            }
            System.out.println("|");
        }
    }
}
