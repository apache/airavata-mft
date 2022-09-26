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
