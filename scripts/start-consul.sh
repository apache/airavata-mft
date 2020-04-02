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

PID_PATH_NAME="../build/consul/service-pid"
LOG_FILE="../build/consul/consul.log"

case $1 in
    mac)
        if [ ! -f $PID_PATH_NAME ]; then
            mkdir -p ../build/consul
            curl -O https://releases.hashicorp.com/consul/1.7.1/consul_1.7.1_darwin_amd64.zip
            unzip -o consul_1.7.1_darwin_amd64.zip -d ../build/consul
            rm consul_1.7.1_darwin_amd64.zip
            nohup ../build/consul/consul agent -dev > $LOG_FILE 2>&1 &
            echo $! > $PID_PATH_NAME
            echo "Consul started"
        else
            echo "Consul is already running ..."
        fi
    ;;
    -h)
        echo "Usage: start-consul.sh"

        echo "command options:"
        echo "  mac                  Start for mac"
        echo "  -h                   Display this help and exit"
        shift
        exit 0
    ;;
esac