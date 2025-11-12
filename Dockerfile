# Start from a Java 17 base image
FROM openjdk:17-slim

# Set environment variables
ENV JAVA_HOME=/usr/local/openjdk-17
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# Verify the installation
RUN java -version && javac -version

COPY requirements.txt ./

RUN apt-get update && \
    apt-get install -y --no-install-recommends maven make j2cli curl procps python3 python3-pip && \
    rm -rf /var/lib/apt/lists/* && \
    pip3 install --upgrade pip && \
    pip3 install -r ./requirements.txt

COPY ./lib /tmp/lib

RUN mvn install:install-file -Dfile=/tmp/lib/japi.jar -DgroupId=uk.ac.ebi.uniprot -DartifactId=japi -Dversion=1.3.0 -Dpackaging=jar \
 && mvn install:install-file -Dfile=/tmp/lib/xml.jar -DgroupId=uk.ac.ebi.uniprot -DartifactId=xml -Dversion=2024.6-SNAPSHOT -Dpackaging=jar
 # Install Apache Jena
ENV JENA_VERSION=5.5.0
ENV JENA_HOME=/opt/jena
ENV PATH="${JENA_HOME}/bin:${PATH}"

RUN curl -L -o /tmp/apache-jena.tar.gz \
      https://archive.apache.org/dist/jena/binaries/apache-jena-${JENA_VERSION}.tar.gz \
 && mkdir -p ${JENA_HOME} \
 && tar -xzf /tmp/apache-jena.tar.gz -C /opt \
 && mv /opt/apache-jena-${JENA_VERSION}/* ${JENA_HOME}/ \
 && rm -rf /tmp/apache-jena.tar.gz

RUN tdbloader --version && arq --version