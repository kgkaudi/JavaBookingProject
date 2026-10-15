# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

# the cache mount keeps downloaded dependencies between builds (much faster rebuilds)
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -Dmaven.test.skip=true package

# Fail the BUILD (not the running container) if the jar is not a Spring Boot executable jar.
# That happens when spring-boot-maven-plugin is missing from pom.xml: "mvn package" then only
# produces a plain library jar, and the container would crash with "no main manifest attribute".
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN jar tf target/*.jar | grep -c '^BOOT-INF/classes/' > /dev/null \
    || { echo "ERROR: target/*.jar is not an executable Spring Boot jar - add spring-boot-maven-plugin to the <plugins> in pom.xml" >&2; exit 1; }

# ---------- runtime stage ----------
FROM eclipse-temurin:21-jre

# never run the app as root
RUN useradd --system --uid 10001 --no-create-home app
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

USER 10001
EXPOSE 8080

# sensible container defaults; override with JAVA_OPTS if needed
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# healthy once something is listening on 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
    CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
