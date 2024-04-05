FROM openjdk:17-jdk-alpine as stage1

WORKDIR /app

# .dockerignore에 불필요 파일 추가
COPY . .

# /app/build/libs/*.jar 파일을 아래 명령어를 통해 생성
RUN ./gradlew bootJar

# 새로운 work stage 시작, 기존 스테이지는 자동으로 사라진다.
FROM openjdk:17-jdk-alpine

WORKDIR /app

# 왼쪽이 stage1, 오른쪽이 도커 두번째 stage
COPY --from=stage1 /app/build/libs/*.jar app.jar

# CMD 또는 ENTRYPOINT를 통해 컨테이너 실행
ENTRYPOINT [ "java", "-jar", "app.jar" ]