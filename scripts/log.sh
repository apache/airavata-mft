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

case $1 in
    agent)
        tail -100f ../build/MFT-Agent-0.01/logs/airavata.log
    ;;
    resource)
        tail -100f ../build/Resource-Service-0.01/logs/airavata.log
    ;;
    secret)
        tail -100f ../build/Secret-Service-0.01/logs/airavata.log
    ;;
    api)
        tail -100f ../build/API-Service-0.01/logs/airavata.log
    ;;
    controller)
        tail -100f ../build/MFT-Controller-0.01/logs/airavata.log
    ;;
    consul)
        tail -100f ../build/consul/consul.log
    ;;
    -h)
        echo "Usage: log.sh"

        echo "command options:"
        echo "  agent                View logs of MFT Agent"
        echo "  resource             View logs of MFT Resource Service"
        echo "  secret               View logs of MFT Secret Service"
        echo "  api                  View logs of MFT API Service"
        echo "  controller           View logs of MFT Controller"
        echo "  -h                   Display this help and exit"
        shift
        exit 0
    ;;
esac