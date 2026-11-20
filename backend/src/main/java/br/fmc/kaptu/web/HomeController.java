package br.fmc.kaptu.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> inicio() {
        return Map.of(
                "aplicacao", "Kaptu API",
                "status", "online",
                "mensagem", "Backend funcionando corretamente."
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP"
        );
    }
}