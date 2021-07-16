FROM java:8

LABEL "Author"="Pavel Perepech <paul_spb@mail.ru>"
LABEL "version"="1.0-PRE_BETA"

ENV ZOO_HOST=localhost
ENV ZOO_PORT=2181
ENV ZOO_USER=
ENV ZOO_PASSWORD=

ADD ./target/zsc-jar-with-dependencies.jar /app/zsc.jar
ENTRYPOINT java -jar /app/zsc.jar --host $ZOO_HOST --port "$ZOO_PORT" --user "$ZOO_USER" --password "$ZOO_PASSWORD"

