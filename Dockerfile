FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY build/libs/money-talk-analytics-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]