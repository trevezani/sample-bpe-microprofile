FROM openjdk:8u212-jre-alpine
RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*
ENV AB_OFF=true
ENV JAEGER_SERVICE_NAME=bpe-chave\
  JAEGER_ENDPOINT=http://jaeger-collector.istio-system.svc:14268/api/traces\
  JAEGER_PROPAGATION=b3\
  JAEGER_SAMPLER_TYPE=const\
  JAEGER_SAMPLER_PARAM=1
EXPOSE 8080 8778 9779 5006
ADD target/*.jar /app.jar
CMD java -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006 -Djava.net.preferIPv4Stack=true -jar /app.jar