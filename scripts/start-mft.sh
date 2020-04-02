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

./../build/API-Service-0.01/bin/api-service-daemon.sh start
./../build/MFT-Controller-0.01/bin/controller-daemon.sh start
./../build/Resource-Service-0.01/bin/resource-service-daemon.sh start
./../build/Secret-Service-0.01/bin/secret-service-daemon.sh start
./../build/MFT-Agent-0.01/bin/agent-daemon.sh start