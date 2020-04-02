#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

cd ../
mvn clean install
mkdir -p build
cp agent/target/MFT-Agent-0.01-bin.zip build/
cp controller/target/MFT-Controller-0.01-bin.zip build/
cp services/resource-service/server/target/Resource-Service-0.01-bin.zip build/
cp services/secret-service/server/target/Secret-Service-0.01-bin.zip build/
cp api/service/target/API-Service-0.01-bin.zip build/

unzip -o build/MFT-Agent-0.01-bin.zip -d build/
unzip -o build/MFT-Controller-0.01-bin.zip -d build/
unzip -o build/Resource-Service-0.01-bin.zip -d build/
unzip -o build/Secret-Service-0.01-bin.zip -d build/
unzip -o build/API-Service-0.01-bin.zip -d build/