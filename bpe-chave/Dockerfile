FROM openjdk:8u212-jre-alpine
RUN apk add --update \
    curl \
    && rm -rf /var/cache/apk/*
EXPOSE 8080
ADD target/*.jar /app.jar
CMD java -Djava.net.preferIPv4Stack=true -jar /app.jar