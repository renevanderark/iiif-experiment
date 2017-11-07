FROM ubuntu:16.04


RUN apt-get update && \
	apt-get install -y openjdk-8-jdk && \
	apt-get install -y ant && \
	apt-get clean && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

RUN apt-get update && \
	apt-get install -y ca-certificates-java && \
	apt-get clean && \
	update-ca-certificates -f && \
	rm -rf /var/lib/apt/lists/* && \
	rm -rf /var/cache/oracle-jdk8-installer;

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

RUN apt-get update && apt-get install -y git-core cmake g++ maven && apt-get clean && rm -rf /var/lib/apt/lists/*


RUN git clone https://github.com/uclouvain/openjpeg.git

RUN cd openjpeg
WORKDIR "/openjpeg"
RUN git checkout tags/v2.3.0

RUN cmake . -DCMAKE_BUILD_TYPE=Release -DBUILD_SHARED_LIBS:bool=on -DCMAKE_CXX_COMPILER=/usr/bin/cc
RUN make
RUN make install
RUN make clean
RUN ldconfig

RUN mkdir "/cache"
WORKDIR "/"
RUN git clone https://github.com/renevanderark/iiif-experiment
RUN git checkout tags/v1.0

WORKDIR "/iiif-experiment"
RUN mvn clean package -Dopenjpeg.version=2.3

CMD PORT=8080 java -jar target/iiif-jp2-1.0-SNAPSHOT.jar server config.yaml


