FROM adoptopenjdk/openjdk16:jdk-16.0.1_9-alpine AS build

WORKDIR /src

COPY . .

RUN ./gradlew clean installDist

FROM adoptopenjdk/openjdk16:jdk-16.0.1_9-alpine AS run

WORKDIR /app

ENV PORT=8000

COPY --from=build /src/build/install/ore2discourse-auth-gateway/ /app/ore2discourse-auth-gateway/

WORKDIR /app/ore2discourse-auth-gateway/bin

EXPOSE $PORT

CMD [ "./ore2discourse-auth-gateway" ]
