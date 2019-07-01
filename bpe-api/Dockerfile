FROM openjdk:8u212-jre-alpine
EXPOSE 8080
ADD target/*.jar /app.jar
CMD java -Djava.net.preferIPv4Stack=true -jar /app.jar