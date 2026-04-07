# ── Stage 1: Build & dependency cache ────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom first to leverage Docker layer cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress

# Copy source
COPY src ./src

# ── Stage 2: Runtime with Chrome ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

LABEL maintainer="QA Team"
LABEL description="SauceDemo UI Automation - Selenide + JUnit5 + Allure"

# Install Chrome and dependencies
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
    && wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" \
       > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends google-chrome-stable \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Install Maven (needed to run tests in runtime stage)
COPY --from=builder /usr/share/maven /usr/share/maven
COPY --from=builder /root/.m2 /root/.m2
ENV PATH="/usr/share/maven/bin:${PATH}"

WORKDIR /app

# Copy built project
COPY --from=builder /app .

# Copy entrypoint
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Results volume — mount this to extract reports from the host
VOLUME ["/app/target/allure-results", "/app/target/surefire-reports"]

# Chrome flags for containerized headless execution
ENV JAVA_OPTS="-Xmx1024m"
ENV BROWSER=chrome
ENV HEADLESS=true
ENV BASE_URL=https://www.saucedemo.com

ENTRYPOINT ["/entrypoint.sh"]
