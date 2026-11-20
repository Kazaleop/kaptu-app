package br.fmc.kaptu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Libera o frontend (localhost no desenvolvimento e Vercel em produção)
 * para chamar a API /api/v1/**.
 *
 * ONDE COLOCAR: src/main/java/br/fmc/kaptu/config/CorsConfig.java
 * (criar a pasta "config" se não existir).
 *
 * As origens permitidas vêm da propriedade "app.cors.allowed-origins"
 * (application.properties) ou da variável de ambiente APP_CORS_ALLOWEDORIGINS
 * (regra do Spring Boot: pontos viram "_" e hífens são REMOVIDOS; por isso
 * "allowed-origins" vira "ALLOWEDORIGINS", sem "_" no meio).
 *
 * Obs.: o projeto não usa Spring Security hoje. Se um dia for adicionado,
 * também será preciso chamar http.cors(Customizer.withDefaults()) no SecurityFilterChain.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // allowedOriginPatterns aceita curingas, ex.: https://kaptu-*.vercel.app
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
