FROM adoptopenjdk/openjdk16:jdk-16.0.1_9-alpine AS build

WORKDIR /src

COPY . .

RUN ./gradlew clean build

FROM adoptopenjdk/openjdk16:jdk-16.0.1_9-alpine AS run

WORKDIR /app

ENV PORT=8000

COPY --from=build /src/build/libs/ore2discourse-auth-gateway-*-standalone.jar /app/ore2discourse-auth-gateway.jar

EXPOSE $PORT

CMD [ "java", "-jar", "ore2discourse-auth-gateway.jar" ]

