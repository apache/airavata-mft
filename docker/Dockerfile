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

FROM maven:3.8.6-jdk-11

RUN apt-get update -y
RUN apt-get install git -y

RUN mkdir airavata
WORKDIR /airavata

RUN git clone https://github.com/apache/airavata-mft.git

WORKDIR /airavata/airavata-mft/
RUN mvn install

WORKDIR /airavata/airavata-mft/scripts
RUN ["./build.sh"]

RUN touch ../airavata-mft/MFT-Agent-0.01/logs/airavata.log
RUN printf '#!/bin/bash\n_term() {\n./stop-consul.sh\n./stop-mft.sh\n}\ntrap _term SIGTERM\nif [[ "$1"  == consul ]];then\n  ./start-consul.sh linux\nfi\n./start-mft.sh\n./log.sh agent &\nchild=$!\nwait "$child"' > run.sh
RUN chmod u+x run.sh

ENTRYPOINT ["./run.sh"]
CMD ["noconsul"]
