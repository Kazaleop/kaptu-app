
package br.fmc.kaptu.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class SupabaseStorageService {

    private final HttpClient httpClient;
    private final String supabaseUrl;
    private final String secretKey;
    private final String bucket;

    public SupabaseStorageService(
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.secret-key:}") String secretKey,
            @Value("${supabase.storage.bucket:Documentos}") String bucket) {

        this.httpClient = HttpClient.newHttpClient();
        this.supabaseUrl = supabaseUrl;
        this.secretKey = secretKey;
        this.bucket = bucket;
    }

    public String enviarArquivo(Path arquivo, String caminhoNoBucket)
            throws IOException {

        if (supabaseUrl == null || supabaseUrl.isBlank()
                || secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "Configure SUPABASE_URL e SUPABASE_SECRET_KEY."
            );
        }

        if (!Files.exists(arquivo)) {
            throw new IOException("Arquivo não encontrado: " + arquivo);
        }

        String tipoConteudo = Files.probeContentType(arquivo);

        if (tipoConteudo == null) {
            tipoConteudo = "application/octet-stream";
        }

        String url = supabaseUrl.replaceAll("/+$", "")
                + "/storage/v1/object/"
                + codificar(bucket)
                + "/"
                + codificarCaminho(caminhoNoBucket);

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", secretKey)
                .header("Content-Type", tipoConteudo)
                .header("x-upsert", "false")
                .POST(HttpRequest.BodyPublishers.ofFile(arquivo))
                .build();

        try {
            HttpResponse<String> resposta = httpClient.send(
                    requisicao,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (resposta.statusCode() != 200
                    && resposta.statusCode() != 201) {
                throw new IOException(
                        "Falha ao enviar arquivo para o Supabase. HTTP "
                                + resposta.statusCode()
                                + ": " + resposta.body()
                );
            }

            return caminhoNoBucket;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Envio para o Supabase foi interrompido.", e
            );
        }
    }

    public void excluirArquivo(String caminhoNoBucket) throws IOException {
        String url = supabaseUrl.replaceAll("/+$", "")
                + "/storage/v1/object/"
                + codificar(bucket)
                + "/"
                + codificarCaminho(caminhoNoBucket);

        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", secretKey)
                .DELETE()
                .build();

        try {
            HttpResponse<String> resposta = httpClient.send(
                    requisicao,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (resposta.statusCode() != 200
                    && resposta.statusCode() != 204) {
                throw new IOException(
                        "Falha ao excluir arquivo do Supabase. HTTP "
                                + resposta.statusCode()
                                + ": " + resposta.body()
                );
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(
                    "Exclusão no Supabase foi interrompida.", e
            );
        }
    }

    private String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private String codificarCaminho(String caminho) {
        return java.util.Arrays.stream(caminho.split("/"))
                .map(this::codificar)
                .collect(java.util.stream.Collectors.joining("/"));
    }
}
