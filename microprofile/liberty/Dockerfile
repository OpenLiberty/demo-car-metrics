FROM openliberty/daily:full-java11-openj9

COPY --chown=1001:0 /src/main/liberty/config /config
COPY --chown=1001:0 openliberty.properties /opt/ol/wlp/lib/versions/openliberty.properties
COPY --chown=1001:0 bootstrap.properties /opt/ol/wlp/usr/servers/defaultServer/bootstrap.properties

RUN configure.sh
