# == 설정 ==

ARG PACKAGE=azit
ARG VERSION=0.0.1-SNAPSHOT

# == 프론트엔드 빌드 ==
FROM node:22 AS vite

WORKDIR /app

# 의존성 설치
COPY --link ./package.json .
COPY --link ./package-lock.json .
RUN npm ci

# 빌드
COPY --link ./vite.config.js .
COPY --link ./src/main/resources/ ./src/main/resources/
RUN npm run build

CMD ["npm", "run", "watch"]

# == 백엔드 빌드 ==
FROM eclipse-temurin:25-jdk-noble AS maven

WORKDIR /app

# mvnw 준비
COPY --link ./mvnw .
COPY --link ./.mvn/ ./.mvn/

# 의존성 설치
COPY --link ./pom.xml .
RUN ./mvnw dependency:go-offline -B

# Java 컴파일
COPY --link ./src/main/java ./src/main/java
COPY --link ./src/main/resources/*.properties ./src/main/resources/
RUN ./mvnw compile

# jar 패키지 빌드
COPY --link --from=vite /app/target/classes/ ./src/main/resources/
RUN ./mvnw package -DskipTests

CMD ["./mvnw", "spring-boot:run"]

# == 런타임 ==
FROM eclipse-temurin:25-alpine AS jre

ARG PACKAGE
ARG VERSION

ENV PACKAGE=${PACKAGE}
ENV VERSION=${VERSION}

WORKDIR /app

# 빌드된 JAR 가져오기
COPY --link --from=maven /app/target/${PACKAGE}-${VERSION}.jar .

# 서버 실행
CMD java -jar ./${PACKAGE}-${VERSION}.jar
EXPOSE 8080
