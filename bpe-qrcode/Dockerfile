FROM openjdk:8u212-jre-alpine
RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*
ENV JAEGER_SERVICE_NAME=bpe-qrcode\
  JAEGER_SAMPLER_TYPE=const\
  JAEGER_SAMPLER_PARAM=1
EXPOSE 8080 8778 9779
ADD target/*.jar /app.jar
CMD java -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5006 -Djava.net.preferIPv4Stack=true -jar /app.jar