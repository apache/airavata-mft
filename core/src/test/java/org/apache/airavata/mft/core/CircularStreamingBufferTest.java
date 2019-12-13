/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.core;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CircularStreamingBufferTest {

    public static void main(String args[]) throws Exception {
        test3();
    }

    public static void test3() throws Exception {
        final CircularStreamingBuffer sb = new CircularStreamingBuffer();
        InputStream is = sb.getInputStream();
        OutputStream os = sb.getOutputStream();
        File file = new File("/Users/dimuthu/data.csv");
        FileInputStream fis = new FileInputStream(file);

        List<Byte> src = new ArrayList<>();
        List<Byte> dst = new ArrayList<>();


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    byte[] buf = new byte[1024];

                    int filesize = (int)file.length();
                    int bufSize;
                    while (true) {
                        if (buf.length < filesize) bufSize = buf.length;
                        else bufSize = (int) filesize;
                        bufSize = fis.read(buf, 0, bufSize);
                        if (bufSize < 0) {
                            // error
                            break;
                        }
                        System.out.println("Write " + bufSize);
                        os.write(buf, 0, bufSize);
                        os.flush();

                        for (int i = 0 ; i < bufSize; i++) {
                            src.add(buf[i]);
                        }

                        filesize -= bufSize;
                        if (filesize == 0L) break;
                    }
                    os.close();
                    System.out.println("Done writing");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int res = 0;
                byte[] buf = new byte[1024];

                try {
                    int filesize = (int)file.length();

                    long totalWritten = 0;
                    while (true) {
                        int len = is.read(buf, 0, buf.length);
                        if (len == -1) {
                            break;
                        } else {
                            for (int i = 0; i < len; i++) {
                                dst.add(buf[i]);
                            }
                            totalWritten += len;
                            System.out.println("Read " + totalWritten);
                            if (totalWritten == filesize) {
                                break;
                            }
                        }
                    }
                    System.out.println("Done reading");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("Done");
        System.out.println("Source length " + src.size());
        System.out.println("Destination length " + dst.size());

        boolean match = true;
        for (int i = 0 ; i < src.size(); i++) {
            match &= src.get(i).equals(dst.get(i));
            if (!match) {
                System.out.println("Failed at index " + i);
                break;
            }
        }

        System.out.println("Match : " + match);
    }

    public static void test2() throws Exception {
        final CircularStreamingBuffer sb = new CircularStreamingBuffer();
        InputStream is = sb.getInputStream();
        OutputStream os = sb.getOutputStream();
        File file = new File("/Users/dimuthu/data.csv");
        FileInputStream fis = new FileInputStream(file);

        List<Byte> src = new ArrayList<>();
        List<Byte> dst = new ArrayList<>();


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    byte[] buf = new byte[1024];

                    int filesize = (int)file.length();
                    int bufSize;
                    while (true) {
                        if (buf.length < filesize) bufSize = buf.length;
                        else bufSize = (int) filesize;
                        bufSize = fis.read(buf, 0, bufSize);
                        if (bufSize < 0) {
                            // error
                            break;
                        }
                        System.out.println("Write " + bufSize);
                        os.write(buf, 0, bufSize);
                        os.flush();

                        for (int i = 0 ; i < bufSize; i++) {
                            src.add(buf[i]);
                        }

                        filesize -= bufSize;
                        if (filesize == 0L) break;
                    }
                    os.close();
                    System.out.println("Done writing");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int res = 0;
                try {
                    while ((res = is.read()) != -1) {
                        System.out.println("Read " + res);
                        dst.add((byte) res);
                    }
                    System.out.println("Done reading");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("Done");
        System.out.println("Source length " + src.size());
        System.out.println("Destination length " + dst.size());

        boolean match = true;
        for (int i = 0 ; i < src.size(); i++) {
            match &= src.get(i).equals(dst.get(i));
            if (!match) {
                System.out.println("Failed at index " + i);
                break;
            }
        }

        System.out.println("Match : " + match);
    }

    public static void test1() throws Exception {
        final CircularStreamingBuffer sb = new CircularStreamingBuffer();
        InputStream is = sb.getInputStream();
        OutputStream os = sb.getOutputStream();
        File file = new File("/Users/dimuthu/data.csv");
        FileInputStream fis = new FileInputStream(file);

        List<Byte> src = new ArrayList<>();
        List<Byte> dst = new ArrayList<>();


        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    int read = 0;
                    while ((read = fis.read()) != -1) {
                        os.write(read);
                        src.add((byte) read);
                    }
                    os.close();
                    System.out.println("Done writing");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                int res = 0;
                try {
                    while ((res = is.read()) != -1) {
                        System.out.println("Read " + res);
                        dst.add((byte) res);
                    }
                    System.out.println("Done reading");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("Done");
        System.out.println("Source length " + src.size());
        System.out.println("Destination length " + dst.size());

        boolean match = true;
        for (int i = 0 ; i < src.size(); i++) {
            match &= src.get(i).equals(dst.get(i));
            if (!match) {
                System.out.println("Failed at index " + i);
                break;
            }
        }

        System.out.println("Match : " + match);
    }
}
