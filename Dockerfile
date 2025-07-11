
FROM ghcr.io/internetofwater/nldi-py:2.1.0

COPY nldi.server.yml nldi.source.yml /tmp/

RUN cat /tmp/nldi.server.yml /tmp/nldi.source.yml > /nldi/local.source.yml
