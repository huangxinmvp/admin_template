ARG BACKEND_BASE_IMAGE=docker.m.daocloud.io/library/maven:3.9.9-eclipse-temurin-21
FROM ${BACKEND_BASE_IMAGE}

WORKDIR /app

COPY web_backend/target/admin-template-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8081

CMD ["java", "-jar", "/app/app.jar"]
