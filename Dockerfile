FROM eclipse-temurin:17-jdk-jammy
RUN apt-get update && apt-get install -y iputils-ping
COPY target/admin.jar admin.jar
ENTRYPOINT ["java", "-jar", "admin.jar"]