# ==========================================
# STAGE 1: Build JAR với Gradle Cache
# ==========================================
FROM gradle:8.5-jdk17-alpine AS build
WORKDIR /home/gradle/src

# 1. Copy config build trước để cache dependencies
COPY --chown=gradle:gradle build.gradle.kts settings.gradle.kts gradlew ./
COPY --chown=gradle:gradle gradle ./gradle
RUN gradle dependencies --no-daemon || true

# 2. Copy source code và build bootJar
COPY --chown=gradle:gradle src ./src
RUN gradle bootJar -x test --no-daemon

# ==========================================
# STAGE 2: Runtime Image nhẹ & Bảo mật
# ==========================================
FROM eclipse-temurin:17-jre-alpine

# Cài đặt múi giờ Việt Nam
ENV TZ=Asia/Ho_Chi_Minh
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# Tạo user không phải root để đảm bảo an toàn
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy file jar từ stage build
COPY --from=build --chown=spring:spring /home/gradle/src/build/libs/*[!plain].jar /app/app.jar

EXPOSE 8080

# Cấu hình tối ưu bộ nhớ JVM trong container
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "/app/app.jar"]