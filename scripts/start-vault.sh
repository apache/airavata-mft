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

scriptdir="$(dirname "$0")"
cd "$scriptdir"

PID_PATH_NAME="../airavata-mft/vault/vault-pid"
LOG_FILE="../airavata-mft/vault/vault.log"
VAULT_PATH_NAME="../airavata-mft/vault/vault"

URL=""
ZIPFILE=""

if [[ $OSTYPE == *"darwin"* ]];
then
  URL="https://releases.hashicorp.com/vault/1.14.1/vault_1.14.1_darwin_amd64.zip"
  ZIPFILE="vault_1.14.1_darwin_amd64.zip"
elif [[ $OSTYPE == *"linux"* ]];
then
  URL="https://releases.hashicorp.com/vault/1.14.1/vault_1.14.1_linux_amd64.zip"
  ZIPFILE="vault_1.14.1_linux_amd64.zip"
else
  echo "As of now, airavata-mft only supports linux and mac"
  exit 0
fi

if [ ! -f $PID_PATH_NAME ];
then
  mkdir -p ../airavata-mft/vault/keys
elif pgrep -x "vault" > /dev/null
then
  # This is the condition where vault-pid file exists and
  # vault is actually running
  # Then this block will be executed

  # Reference: https://askubuntu.com/questions/157779/how-to-determine-whether-a-process-is-running-or-not-and-make-use-it-to-make-a-c
  echo "Vault is already running ..."
  exit 0
fi

# if vault-pid file exists or not but the vault executable itself does not exist
# then the following code will be executed
if [ ! -f $VAULT_PATH_NAME ]
then
  curl -O $URL
  unzip -o $ZIPFILE -d ../airavata-mft/vault
  rm $ZIPFILE
fi

# if the control structure reaches here, we have the vault executable ready to run
nohup ../airavata-mft/vault/vault server -config=./vault-config.hcl > $LOG_FILE 2>&1 &
echo $! > $PID_PATH_NAME   # $! contains the pid of the recently started background process
echo "Vault started"


# Reference:
# https://stackoverflow.com/a/3232433


#while True; do
#  if [ -f $LOG_FILE ]; then
#    lineCount=$(wc -l < $LOG_FILE | tr -d ' ' | tr -d '\n')
#    lineCount="$(echo -e "${lineCount}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
#    if [[ $lineCount -gt 4 ]]; then
#      echo "Log file is being updated";
#      break;
#    fi
#  fi
#done
#
#while IFS=':' read -r Key Value;
#do
#  Key="$(echo -e "${Key}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
#  Value="$(echo -e "${Value}" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
#  if [ "$Key" == "Api Address" ]; then
#    echo "cat $LOG_FILE"
#    echo "export VAULT_ADDR='$Value'"
#  fi
#done < $LOG_FILE

