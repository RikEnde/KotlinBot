FROM --platform=linux/arm64 eclipse-temurin:22.0.1_8-jdk
ENV DISCORD_TOKEN="${DISCORD_TOKEN}"
ENV OPENAI_KEY="${OPENAI_KEY}"

EXPOSE 8080

ARG JAR_FILE=target/*.jar

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]