# Bước 1: Build source code bằng Gradle
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build -x test --no-daemon

# Bước 2: Chạy ứng dụng với JRE siêu nhẹ
FROM eclipse-temurin:17-jre-alpine
EXPOSE 8080
RUN mkdir /app
# Lấy file .jar đã build từ Bước 1 sang
COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar
ENTRYPOINT ["java", "-jar", "/app/spring-boot-application.jar"]