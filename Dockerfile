# This will be where nldi-py releases are specified for prod builds of
# the nldi-services image.  
ARG IMAGE_VERSION=2.1.5

FROM ghcr.io/internetofwater/nldi-py:${IMAGE_VERSION}

COPY nldi.server.yml nldi.source.yml /tmp/

RUN cat /tmp/nldi.server.yml /tmp/nldi.source.yml > /nldi/local.source.yml
