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

package org.apache.airavata.mft.controller.spawner;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import org.apache.airavata.mft.agent.stub.SecretWrapper;
import org.apache.airavata.mft.agent.stub.StorageWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class EC2AgentSpawner extends CloudAgentSpawner {

    private static final Logger logger = LoggerFactory.getLogger(EC2AgentSpawner.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private String instanceId;
    private CountDownLatch portForwardLock;
    Future<String> launchFuture;
    Future<Boolean> terminateFuture;

    public EC2AgentSpawner(StorageWrapper storageWrapper, SecretWrapper secretWrapper) {
        super(storageWrapper, secretWrapper);
    }

    @Override
    public void launch() {

        launchFuture = executor.submit( () -> {
            String imageId = "ami-0ecc74eca1d66d8a6"; // Ubuntu base image
            String keyNamePrefix = "mft-aws-agent-key-";
            String secGroupName = "MFTAgentSecurityGroup";
            String agentId = UUID.randomUUID().toString();
            String systemUser = "ubuntu";

            String mftKeyDir = System.getProperty("user.home") + File.separator + ".mft" + File.separator + "keys";
            String accessKey = secretWrapper.getS3().getAccessKey();
            String secretKey = secretWrapper.getS3().getSecretKey();
            String region = storageWrapper.getS3().getRegion();

            try {
                BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

                AmazonEC2 amazonEC2 = AmazonEC2ClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                                "https://ec2." + region + ".amazonaws.com", region))
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .build();

                DescribeSecurityGroupsRequest desSecGrp = new DescribeSecurityGroupsRequest();
                DescribeSecurityGroupsResult describeSecurityGroupsResult = amazonEC2.describeSecurityGroups(desSecGrp);
                List<SecurityGroup> securityGroups = describeSecurityGroupsResult.getSecurityGroups();
                boolean hasMftSecGroup = securityGroups.stream().anyMatch(sg -> sg.getGroupName().equals(secGroupName));

                if (!hasMftSecGroup) {
                    CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
                    csgr.withGroupName(secGroupName).withDescription("MFT Agent Security Group");

                    CreateSecurityGroupResult createSecurityGroupResult = amazonEC2.createSecurityGroup(csgr);

                    IpPermission ipPermission = new IpPermission();

                    IpRange ipRange1 = new IpRange().withCidrIp("0.0.0.0/0");

                    ipPermission.withIpv4Ranges(Collections.singletonList(ipRange1))
                            .withIpProtocol("tcp")
                            .withFromPort(22)
                            .withToPort(22);

                    AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest =
                            new AuthorizeSecurityGroupIngressRequest();
                    authorizeSecurityGroupIngressRequest.withGroupName(secGroupName)
                            .withIpPermissions(ipPermission);
                    amazonEC2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
                }

                List<String> localKeys = new ArrayList<>();
                if (Files.isDirectory(Path.of(mftKeyDir))) {
                    Stream<Path> keyPaths = Files.list(Path.of(mftKeyDir));
                    keyPaths.forEach(p -> localKeys.add(p.toFile().getName()));
                }

                Optional<KeyPairInfo> availableKeyPair = Optional.empty();
                if (!localKeys.isEmpty()) {

                    DescribeKeyPairsResult keyPairs = amazonEC2.describeKeyPairs();
                    availableKeyPair = keyPairs.getKeyPairs().stream()
                            .filter(kp -> localKeys.stream()
                                    .anyMatch(lk -> lk.equals(kp.getKeyName()))).findFirst();
                }

                String keyName;

                if (availableKeyPair.isEmpty()) {
                    logger.info("Creating Key pair");
                    keyName = keyNamePrefix + UUID.randomUUID().toString();
                    CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();

                    createKeyPairRequest.withKeyName(keyName);

                    CreateKeyPairResult createKeyPairResult = amazonEC2.createKeyPair(createKeyPairRequest);

                    KeyPair keyPair = createKeyPairResult.getKeyPair();

                    String privateKey = keyPair.getKeyMaterial();

                    Files.createDirectories(Path.of(mftKeyDir));
                    Files.write(Path.of(mftKeyDir, keyName), privateKey.getBytes(StandardCharsets.UTF_8));
                    logger.info("Created key pair " + keyName);

                } else {
                    keyName = availableKeyPair.get().getKeyName();
                    logger.info("Using existing key pair " + keyName);
                }

                RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

                runInstancesRequest.withImageId(imageId)
                        .withInstanceType(InstanceType.T1Micro)
                        .withMinCount(1)
                        .withMaxCount(1)
                        .withKeyName(keyName)
                        .withTagSpecifications(
                                new TagSpecification().withResourceType(ResourceType.Instance)
                                        .withTags(new Tag().withKey("Type").withValue("MFT-Agent"),
                                                new Tag().withKey("AgentId").withValue(agentId)))
                        .withSecurityGroups(secGroupName);


                logger.info("Launching the EC2 VM to start Agent {}", agentId);
                RunInstancesResult result = amazonEC2.runInstances(runInstancesRequest);

                instanceId = result.getReservation().getInstances().get(0).getInstanceId();

                try {
                    DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
                    describeInstancesRequest.setInstanceIds(Collections.singletonList(instanceId));

                    InstanceState instanceState = null;
                    String publicIpAddress = null;

                    logger.info("Waiting until instance {} is ready", instanceId);

                    for (int i = 0; i < 30; i++) {
                        DescribeInstancesResult describeInstancesResult = amazonEC2.describeInstances(describeInstancesRequest);
                        Instance instance = describeInstancesResult.getReservations().get(0).getInstances().get(0);
                        instanceState = instance.getState();
                        publicIpAddress = instance.getPublicIpAddress();

                        logger.info("Instance state {}, public ip {}", instanceState.getName(), publicIpAddress);

                        if (instanceState.getName().equals("running") && publicIpAddress != null) {
                            break;
                        }
                        Thread.sleep(2000);
                    }

                    logger.info("Waiting 30 seconds until the ssh interface comes up in instance {}", instanceId);
                    Thread.sleep(30000);
                    if ("running".equals(instanceState.getName()) && publicIpAddress != null) {
                        SSHProvider portForwardAgent = new SSHProvider();
                        portForwardAgent.initConnection(publicIpAddress, 22,
                                Path.of(mftKeyDir, keyName).toAbsolutePath().toString(), systemUser);
                        logger.info("Created SSH Connection. Installing dependencies...");

                        int exeCode = portForwardAgent.runCommand("sudo apt install -y openjdk-11-jre-headless");
                        if (exeCode != 0)
                            throw new IOException("Failed to install jdk on new VM");
                        exeCode = portForwardAgent.runCommand("sudo apt install -y unzip");
                        if (exeCode != 0)
                            throw new IOException("Failed to install unzip on new VM");
                        exeCode = portForwardAgent.runCommand("wget https://github.com/apache/airavata-mft/releases/download/v0.0.1/MFT-Agent-0.01-bin.zip");
                        if (exeCode != 0)
                            throw new IOException("Failed to download mft distribution");
                        exeCode = portForwardAgent.runCommand("unzip MFT-Agent-0.01-bin.zip");
                        if (exeCode != 0)
                            throw new IOException("Failed to unzip mft distribution");

                        exeCode = portForwardAgent.runCommand("sed -ir \"s/^[#]*\\s*agent.id=.*/agent.id=" + agentId + "/\" /home/ubuntu/MFT-Agent-0.01/conf/application.properties");
                        if (exeCode != 0)
                            throw new IOException("Failed to update agent id in config file");

                        portForwardLock = new CountDownLatch(1);
                        CountDownLatch portForwardPendingLock = portForwardAgent.createSshPortForward(8500, portForwardLock);

                        logger.info("Waiting until the port forward is setup");
                        portForwardPendingLock.await();

                        exeCode = portForwardAgent.runCommand("sh MFT-Agent-0.01/bin/agent-daemon.sh start");
                        if (exeCode != 0)
                            throw new IOException("Failed to start the MFT Agent");

                        // Waiting 10 seconds to start the Agent
                        Thread.sleep(10000);

                    } else {
                        logger.info("Instance {} was not setup properly", instanceId);
                        throw new Exception("Instance " + instanceId + " was not setup properly");
                    }
                } catch (Exception e) {
                    logger.error("Failed preparing instance {}. Deleting the instance", instanceId);
                    TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
                    terminateInstancesRequest.setInstanceIds(Collections.singleton(instanceId));
                    amazonEC2.terminateInstances(terminateInstancesRequest);
                    throw e;
                }

                return agentId;
            } catch (Exception e) {
                logger.error("Failed to spin up the EC2 Agent", e);
                throw new RuntimeException("Failed to spin up the EC2 Agent", e);
            }
        });
    }

    @Override
    public void terminate() {

        terminateFuture =  executor.submit(() -> {
            if (instanceId != null) {
                String accessKey = secretWrapper.getS3().getAccessKey();
                String secretKey = secretWrapper.getS3().getSecretKey();
                String region = storageWrapper.getS3().getRegion();

                BasicAWSCredentials awsCreds = new BasicAWSCredentials(accessKey, secretKey);

                AmazonEC2 amazonEC2 = AmazonEC2ClientBuilder.standard().withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                                "https://ec2." + region + ".amazonaws.com", region))
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .build();

                if (portForwardLock != null) {
                    portForwardLock.countDown();
                }

                TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
                terminateInstancesRequest.setInstanceIds(Collections.singleton(instanceId));
                amazonEC2.terminateInstances(terminateInstancesRequest);
            }
            return true;
        });
    }

    @Override
    public Future<String> getLaunchState() {
        return launchFuture;
    }

    @Override
    public Future<Boolean> getTerminateState() {
        return terminateFuture;
    }
}
