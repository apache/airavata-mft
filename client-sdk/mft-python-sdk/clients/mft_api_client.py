#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#


import logging
import grpc

from mft.MFTApi_pb2_grpc import MFTApiServiceStub

logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

stub_dict = {}


class MFTClient:

    @staticmethod
    def build_client(hostname, port) -> MFTApiServiceStub:
        if hostname + str(port) in stub_dict:
            return stub_dict[hostname + port]

        channel = grpc.insecure_channel(hostname + ":" + str(port))
        stub = MFTApiServiceStub(channel)
        stub_dict[hostname + str(port)] = stub
        return stub
