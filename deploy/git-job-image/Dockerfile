FROM registry.access.redhat.com/ubi9-minimal:9.3

RUN microdnf install -y git

COPY scripts /scripts

ENV WORKDIR=/work
ENV REPO_PATH=/repo

WORKDIR /work

CMD "/bin/sh"