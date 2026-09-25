FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "--add-modules", "jdk.incubator.vector", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]