FROM amazoncorretto:17-alpine AS build

WORKDIR /app

# 의존성 설치 및 빌드
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

# Gradle 빌드 및 로그 출력
RUN ./gradlew clean build --no-daemon && \
    echo "Gradle build completed successfully" && \
    ls -la /app/build/libs

# 빌드 결과물을 실행 이미지로 복사
FROM amazoncorretto:17-alpine

WORKDIR /app

# 빌드 스테이지에서 JAR 파일 복사
COPY --from=build /app/build/libs/backend-0.0.1-SNAPSHOT.jar /app/singlebungle.jar

CMD ["java", "-jar", "/app/singlebungle.jar"]