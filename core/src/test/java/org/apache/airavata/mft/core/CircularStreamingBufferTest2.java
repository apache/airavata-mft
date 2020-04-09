package org.apache.airavata.mft.core;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;

public class CircularStreamingBufferTest2 {

    public static void main(String args[]) throws InterruptedException {
        //final DoubleStreamingBuffer sb = new DoubleStreamingBuffer();
        final CircularStreamingBuffer sb = new CircularStreamingBuffer();

        String randStr = RandomStringUtils.random(20000000);
        byte[] randStrBytes = randStr.getBytes();
        int arrLength = 2000000;
        byte[] sourceBytes = new byte[arrLength];
        System.arraycopy(randStrBytes,0,sourceBytes,0,arrLength);

        System.out.println("Source size " + sourceBytes.length);
        byte[] destBytes = new byte[sourceBytes.length];

        Thread writeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (byte b : sourceBytes) {
                        sb.getOutputStream().write(b);
                    }
                    sb.getOutputStream().flush();
                    sb.getOutputStream().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int val;
                    int counter = 0;
                    while ((val = sb.getInputStream().read()) != -1) {
                        //System.out.println("loop " + val + " " + counter);
                        //Thread.sleep(1);
                        destBytes[counter] = (byte) val;
                        counter ++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        writeThread.start();
        readThread.start();

        System.out.println("Started threads");
        long start = System.nanoTime();
        writeThread.join();
        readThread.join();
        long end = System.nanoTime();

        System.out.println("Both threads completed - time " + (end - start)/1000000);

        //System.out.println(new String(sourceBytes));
        //System.out.println(new String(destBytes));

        System.out.println("Equal " + new String(sourceBytes).equals(new String(destBytes)));






    }
}
