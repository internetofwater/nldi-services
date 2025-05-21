FROM internetofwater/nldi-py:latest

COPY nldi.server.yml nldi.source.yml /tmp/

RUN cat /tmp/nldi.server.yml /tmp/nldi.source.yml > /nldi/local.source.yml
