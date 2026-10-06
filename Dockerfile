FROM eclipse-temurin:24-jdk
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

COPY target/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "--add-modules", "jdk.incubator.vector", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]