# =============================================================================
# Multi-Stage Dockerfile for SauceDemo UI Automation Framework
# =============================================================================
# Stage 1 (builder): Uses the official Maven + JDK image to download all
#   Maven dependencies offline and compile the project. Separating this into
#   its own stage means the dependency layer is cached independently of the
#   source code — rebuilds after code changes skip the slow dependency download.
#
# Stage 2 (runtime): Uses a minimal JRE image, installs Google Chrome, copies
#   the pre-built project and cached .m2 repository from Stage 1, and sets up
#   the entrypoint script for test execution.
#
# Usage:
#   docker build -t saucedemo-tests .
#   docker run --rm -v $(pwd)/target:/app/target saucedemo-tests
# =============================================================================

# ── Stage 1: Build & dependency cache ────────────────────────────────────────
# Use the official Maven image with Eclipse Temurin JDK 17 as the build base.
# This image includes both Maven and the full JDK needed for compilation.
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set the working directory inside the build container
WORKDIR /app

# Copy only pom.xml first to leverage Docker layer caching.
# If pom.xml hasn't changed, Docker reuses the cached dependency layer
# and skips the slow 'mvn dependency:go-offline' step on subsequent builds.
COPY pom.xml .

# Download all Maven dependencies into the local .m2 repository.
# --no-transfer-progress suppresses verbose download progress output.
RUN mvn dependency:go-offline --no-transfer-progress

# Copy the full source tree after dependencies are cached
COPY src ./src

# ── Stage 2: Runtime with Chrome ─────────────────────────────────────────────
# Use a minimal JRE image (no full JDK needed at runtime) based on Ubuntu Jammy.
# This keeps the final image smaller than using the full JDK image.
FROM eclipse-temurin:17-jre-jammy

# Image metadata labels
LABEL maintainer="QA Team"
LABEL description="SauceDemo UI Automation - Selenide + JUnit5 + Allure"

# Install Google Chrome stable and all required system dependencies.
# Chrome requires a large set of shared libraries for rendering, fonts,
# and graphics — these are installed via apt before adding the Chrome repo.
RUN apt-get update && apt-get install -y --no-install-recommends \
    wget \
    curl \
    gnupg \
    ca-certificates \
    fonts-liberation \
    libasound2 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libcups2 \
    libdbus-1-3 \
    libdrm2 \
    libgbm1 \
    libgtk-3-0 \
    libnspr4 \
    libnss3 \
    libx11-xcb1 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libxss1 \
    libxtst6 \
    xdg-utils \
    # Add Google's signing key and Chrome apt repository
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    # Update package list with Chrome repo and install Chrome
    && apt-get update \
    && apt-get install -y --no-install-recommends google-chrome-stable \
    # Clean up apt cache to reduce image size
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Copy Maven binary from the builder stage (avoids installing Maven separately)
COPY --from=builder /usr/share/maven /usr/share/maven

# Copy the pre-populated .m2 dependency cache from the builder stage.
# This means the runtime container doesn't need to re-download dependencies
# when running tests — they're already available locally.
COPY --from=builder /root/.m2 /root/.m2

# Add Maven to the PATH so 'mvn' commands work in the entrypoint script
ENV PATH="/usr/share/maven/bin:${PATH}"

# Set the working directory for test execution
WORKDIR /app

# Copy the compiled project (source + compiled classes) from the builder stage
COPY --from=builder /app .

# Copy the entrypoint script and make it executable
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Declare volumes for test result extraction.
# Mount these on the host to retrieve Allure results and Surefire reports
# after the container exits:
#   docker run -v $(pwd)/target:/app/target saucedemo-tests
VOLUME ["/app/target/allure-results", "/app/target/surefire-reports"]

# JVM heap size limit — prevents OOM in memory-constrained CI environments
ENV JAVA_OPTS="-Xmx1024m"

# Default browser configuration — overridable via docker run -e flags
ENV BROWSER=chrome
ENV HEADLESS=true
ENV BASE_URL=https://www.saucedemo.com

# Run the entrypoint script when the container starts
ENTRYPOINT ["/entrypoint.sh"]
