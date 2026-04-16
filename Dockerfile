# == 설정 ==

ARG PACKAGE=azit
ARG VERSION=0.0.1-SNAPSHOT

# == 빌드 ==
FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /app

# mvnw 준비
COPY --link ./mvnw .
COPY --link ./.mvn/ ./.mvn/

# 의존성 설치
COPY --link ./pom.xml .
RUN ./mvnw dependency:go-offline -B

# 패키지 빌드
COPY --link ./src/ ./src/
RUN ./mvnw package -DskipTests

# == 런타임 ==
FROM eclipse-temurin:25-alpine

ARG PACKAGE
ARG VERSION

ENV PACKAGE=${PACKAGE}
ENV VERSION=${VERSION}

WORKDIR /app

# 빌드된 JAR 가져오기
COPY --link --from=build /app/target/${PACKAGE}-${VERSION}.jar .

# 서버 실행
CMD java -jar ./${PACKAGE}-${VERSION}.jar
EXPOSE 8080
