# =============================================================================
# STAGE 12: DOCKER - PACKAGING TASKFLOW AS A CONTAINER IMAGE
# =============================================================================
#
# WHAT IS DOCKER, AND WHY DO WE USE IT?
# -----------------------------------------------------------------------
# A "container" packages our application together with everything it
# needs to run (a specific Java runtime, our compiled classes, our
# dependencies) into one self-contained unit. Without Docker, "it works
# on my machine" problems happen because a teammate's computer, a CI
# server, and a production server can each have a slightly different Java
# version, different environment variables, or missing libraries. A
# Docker IMAGE is a read-only template built from the instructions below;
# a Docker CONTAINER is a running instance of that image - the same
# relationship a Java CLASS has to an OBJECT created from it.
#
# WHY A MULTI-STAGE BUILD (TWO "FROM" LINES)?
# -----------------------------------------------------------------------
# Building our application requires the full JDK plus Maven - tools we
# only need WHILE compiling. Running the already-compiled application only
# needs a much smaller JRE (Java Runtime Environment, explained in
# TaskFlowApplication's Javadoc) and our final .jar file. A multi-stage
# build uses one temporary container (the "build" stage) to compile the
# project, then copies ONLY the finished .jar file into a second, much
# smaller final image - so our shipped image does not carry Maven, our
# source code, or the full JDK around with it, which keeps the image
# smaller and reduces what an attacker could find inside it.

# ---- Stage 1: build the application with Maven and the full JDK ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy just the pom.xml first and download dependencies before copying our
# source code. Docker caches each instruction as a separate "layer"; as
# long as pom.xml does not change, this dependency-download layer can be
# REUSED on the next build instead of re-downloading everything, which
# makes repeated builds during development much faster.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

# Now copy the actual source code and build the final .jar. Tests are
# skipped here because they should already have been run separately (for
# example, in a CI pipeline) before we ever build a deployable image -
# baking a slow test run into every single image build would waste time
# for no benefit, since the image build step is not where we want to
# discover a failing test.
COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Stage 2: the small runtime image that actually gets shipped ----
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy ONLY the built .jar from the previous stage - none of Maven, the
# JDK, or our raw source code exist in this final image at all.
COPY --from=build /build/target/*.jar app.jar

# Documents which network port the application listens on. This line is
# informational for humans and tools reading the Dockerfile; it does not
# by itself publish the port - docker-compose.yml's "ports:" section (or
# "docker run -p") is what actually makes it reachable from outside the
# container.
EXPOSE 8080

# The command that runs when a container starts from this image. Using
# the "exec form" (a JSON array, not a plain shell string) means Java
# runs directly as the container's main process, so it correctly receives
# OS shutdown signals (like the one "docker stop" sends) instead of a
# shell process sitting in between.
ENTRYPOINT ["java", "-jar", "app.jar"]
