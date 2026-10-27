FROM sbtscala/scala-sbt:eclipse-temurin-17_1.x
WORKDIR /app


RUN curl -L -o opentelemetry-javaagent.jar https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar

COPY . .


CMD sbt -J-javaagent:/app/opentelemetry-javaagent.jar run