# 배포용 이미지 (Railway 등). backend 폴더를 빌드 컨텍스트로 쓴다.

# 1) 빌드: Gradle 로 실행 가능한 jar 를 만든다.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 캐시를 위해 Gradle 설정부터 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null

COPY src src
RUN ./gradlew bootJar -x test --no-daemon \
 && cp "$(ls build/libs/*.jar | grep -v plain)" app.jar

# 2) 실행: JRE 만 담은 가벼운 이미지
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/app.jar app.jar

# Railway 가 넣어주는 PORT 를 application.yml 이 읽는다. (기본 8080)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
