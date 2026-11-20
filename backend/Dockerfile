FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Ubuntu 24.04 traz o Tesseract 5, exigido pelo tess4j 5.x
FROM eclipse-temurin:17-jre-noble
RUN apt-get update \
 && apt-get install -y --no-install-recommends tesseract-ocr libtesseract-dev \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY tessdata ./tessdata
RUN mkdir -p /app/arquivos
ENV KAPTU_OCR_TESSDATA=/app/tessdata KAPTU_PASTA_ARQUIVOS=/app/arquivos JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
