FROM openjdk:jdk-bullseye

LABEL author="Olivier Filangi"
LABEL mail="olivier.filangi@inrae.fr"
ENV RDF4J_VERSION="3.7.4"
ENV URL_RDF4J="https://www.eclipse.org/downloads/download.php?file=/rdf4j/eclipse-rdf4j-${RDF4J_VERSION}-onejar.jar&r=1"

ARG BRANCH=develop
ARG REPOSITORY_URL=https://github.com/p2m2/service-rdf-database-deployment.git

# install sbt:https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html

RUN apt update &&\
    apt install -y git scala apt-transport-https curl gnupg -yqq &&\
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list &&\
    echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list &&\
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" \
      | gpg --no-default-keyring --keyring gnupg-ring:/etc/apt/trusted.gpg.d/scalasbt-release.gpg --import && \
    chmod 644 /etc/apt/trusted.gpg.d/scalasbt-release.gpg && \
    apt-get update && apt-get install sbt

RUN git clone ${REPOSITORY_URL} /service

WORKDIR /service

RUN git checkout ${BRANCH} && sbt assembly

RUN wget $URL_RDF4J -O assembly/rdf4j.jar

COPY service /usr/bin/service-rdf-database-deployment

RUN apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

