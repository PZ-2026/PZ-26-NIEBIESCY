FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY libs libs

# Make mvnw executable
RUN chmod +x mvnw

# Install custom library into local maven repo
RUN ./mvnw install:install-file -Dfile=libs/edulink-pdf-1.0.0.jar -DgroupId=com.vectorpeaks -DartifactId=edulink-pdf -Dversion=1.0.0 -Dpackaging=jar

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests — they use H2 and may not need Docker DB)
RUN ./mvnw package -DskipTests -B

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
