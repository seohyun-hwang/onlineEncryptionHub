FROM maven:3.9.9-eclipse-temurin-24-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/onlineEncryptionHub-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "--add-modules", "jdk.incubator.vector", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]