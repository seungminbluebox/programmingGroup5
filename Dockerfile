# == 설정 ==

ARG PACKAGE=azit
ARG VERSION=0.0.1-SNAPSHOT

# == 빌드 ==
FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /app

COPY --link ./mvnw .
COPY --link ./.mvn/ ./.mvn/

COPY --link ./pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY --link ./src/ ./src/
RUN ./mvnw package -DskipTests

# == 런타임 ==
FROM eclipse-temurin:25-alpine

ARG PACKAGE
ARG VERSION

ENV PACKAGE=${PACKAGE}
ENV VERSION=${VERSION}

WORKDIR /app

COPY --link --from=build /app/target/${PACKAGE}-${VERSION}.jar .

CMD java -jar ./${PACKAGE}-${VERSION}.jar
