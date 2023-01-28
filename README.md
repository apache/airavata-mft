<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

[![Build Status](https://travis-ci.org/apache/airavata-mft.svg?branch=master)](https://travis-ci.org/apache/airavata-mft)


# Airavata Managed File Transfers (MFT)

Airavata MFT is a high-performance, multi-protocol data transfer engine developed for orchestrating data movement and operations across most of the cloud and legacy storage endpoints. Through Airavata MFT, our vision is to provide simple but highly performing tools for users to access any cloud or on-premise storage endpoint just as they access local files in their workstations. 

Airavata MFT abstracts out the complexity of each storage type and provides a unified and simple interface for users to access and move data across any endpoint seamlessly. To gain the maximum throughput between storage endpoints, Airavata MFT utilizes Agents installed between the optimum network path with additional multi-channel, parallel data paths to further optimize the transfer performance. In addition to that, MFT can utilize parallel Agents to transfer data between endpoints to gain the advantage of multiple network links. 

# Try Airavata MFT
You need to have Java 11+ and python3.10+ installed to install Airavata MFT in your environment. We currently only support Linux and MacOS operating systems and plan to support Windows in future. 

### Download and Install
```
pip3 install airavata-mft-cli
mft init
```

Above commands will download Airavata MFT into your machine and start the MFT service. To stop MFT after using

```
mft stop
```


### Registering Storages

First you need to register your storage endpoints into MFT in order to access them. Registering storage is an interactive process and you can easily register those without prior knowledge 

```
mft storage add
```

This will ask the type of storage you need and credentials to access those storages. To list already added storages, you can run

```
mft storage list
```
### Accessing Data in Storages

In Airavata MFT, we provide a unified interface to access the data in any storage. Users can access data in storages just as they access data in their computers. MFT converts user queries into storage specific data representations (POSIX, Block, Objects, ..) internally

```
mft ls <storage name>
mft ls <storage name>/<resource path>
```

### Moving Data between Storages

Copying data between storages are simple as copying data between directories of local machine for users. MFT takes care of network path optimizations, parallel data path selections and selections or creations of suitable transfer agents. 
 
 ```
 mft cp <source storage name>/<path> <destination storage name>/<path> 
 ```
MFT is capable of auto detecting directory copying and file copying based on the path given.

### Troubleshooting and Issue Reporting

This is our very first attempt release Airavata MFT for community usage and there might be lots of corner cases that we have not noticed. All the logs of MFT service are available in ```~/.mft/Standalone-Service-0.01/logs/airavata.log```. If you see any error while using MFT, please report that in our Github issue page and we will respond as soon as possible. We really appreciate your contribution as it will greatly help to improve the stability of the product.
