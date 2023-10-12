#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
import typer
import airavata_mft_cli.operations as operations
import airavata_mft_cli.bootstrap as bootstrap
from airavata_mft_cli.util import exception_handler

app = typer.Typer()

@app.command("ls")
def list(storage_path):
  try:
    operations.list(storage_path)
  except Exception as e:
    exception_handler(e)

@app.command("cp")
def copy(source, destination):
  try:
    operations.copy(source, destination)
  except Exception as e:
    exception_handler(e)

@app.command("cp-list")
def copy_list(source_storage_id, dest_storage_id, list_file):
  try:
    operations.copy_list(source_storage_id, dest_storage_id, list_file)
  except Exception as e:
    exception_handler(e)

@app.command("describe")
def describe(storage_name):
  try:
    operations.describe_storage(storage_name)
  except Exception as e:
    exception_handler(e)
@app.command("init")
def init_mft():
  bootstrap.start_mft()

@app.command("stop")
def init_mft():
  bootstrap.stop_mft()

@app.command("update")
def init_mft():
  bootstrap.update_mft()

@app.command("log")
def init_mft():
  bootstrap.print_log()