# --- Build -------------------------------------------------------------
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -B -DskipTests dependency:go-offline

COPY src ./src

RUN ./mvnw -B -DskipTests package

# --- Runtime -----------------------------------------------------------
FROM eclipse-temurin:17-jre-jammy

# Tesseract nativo usado pelo Tess4J no OCR de imagens/PDF escaneado.
RUN apt-get update \
    && apt-get install -y --no-install-recommends tesseract-ocr \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY tessdata ./tessdata

ENV KAPTU_OCR_TESSDATA=./tessdata
ENV KAPTU_PASTA_ARQUIVOS=./arquivos

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
