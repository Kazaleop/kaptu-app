package br.fmc.kaptu.service;

import br.fmc.kaptu.domain.usuario.Usuario;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class TokenService {

    private static final String ALGORITMO = "HmacSHA256";

    private final String segredo;
    private final long expiracaoSegundos;
    private final boolean habilitado;

    public TokenService(
            @Value("${kaptu.auth.secret:}") String segredo,
            @Value("${kaptu.auth.expiracao-horas:8}") long expiracaoHoras,
            @Value("${kaptu.auth.enabled:true}") boolean habilitado) {

        this.segredo = segredo == null ? "" : segredo;
        this.expiracaoSegundos = expiracaoHoras * 3600L;
        this.habilitado = habilitado;

        if (habilitado
                && (this.segredo.isBlank()
                || this.segredo.length() < 32)) {

            throw new IllegalStateException(
                    "KAPTU_AUTH_SECRET deve ter pelo menos 32 caracteres "
                            + "quando a autenticação estiver ligada."
            );
        }
    }

    public String gerarToken(Usuario usuario) {
        try {
            long expiracao = Instant.now().getEpochSecond()
                    + expiracaoSegundos;

            String payloadJson = "{\"sub\":" + usuario.getId()
                    + ",\"exp\":" + expiracao + "}";

            String payloadBase64 = base64Url(
                    payloadJson.getBytes(StandardCharsets.UTF_8)
            );

            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(
                    segredo.getBytes(StandardCharsets.UTF_8),
                    ALGORITMO
            ));

            byte[] assinatura = mac.doFinal(
                    payloadBase64.getBytes(StandardCharsets.US_ASCII)
            );

            return payloadBase64 + "."
                    + base64Url(assinatura);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível gerar o token.", e
            );
        }
    }

    public Long validarEExtrairUsuarioId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        String[] partes = token.split("\\.", -1);

        if (partes.length != 2) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(
                    segredo.getBytes(StandardCharsets.UTF_8),
                    ALGORITMO
            ));

            byte[] assinaturaCalculada = mac.doFinal(
                    partes[0].getBytes(StandardCharsets.US_ASCII)
            );

            byte[] assinaturaRecebida = Base64.getUrlDecoder()
                    .decode(adicionarPadding(partes[1]));

            if (!MessageDigest.isEqual(
                    assinaturaCalculada,
                    assinaturaRecebida)) {
                return null;
            }

            String payload = new String(
                    Base64.getUrlDecoder().decode(
                            adicionarPadding(partes[0])
                    ),
                    StandardCharsets.UTF_8
            );

            Long sub = extrairNumero(payload, "sub");
            Long exp = extrairNumero(payload, "exp");

            if (sub == null || exp == null) {
                return null;
            }

            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }

            return sub;

        } catch (Exception e) {
            return null;
        }
    }

    public long getExpiracaoSegundos() {
        return expiracaoSegundos;
    }

    private static String base64Url(byte[] dados) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(dados);
    }

    private static String adicionarPadding(String valor) {
        int resto = valor.length() % 4;

        if (resto == 0) {
            return valor;
        }

        return valor + "=".repeat(4 - resto);
    }

    private static Long extrairNumero(String json, String chave) {
        int indice = json.indexOf("\"" + chave + "\"");

        if (indice < 0) {
            return null;
        }

        int doisPontos = json.indexOf(':', indice);

        if (doisPontos < 0) {
            return null;
        }

        int inicio = doisPontos + 1;

        while (inicio < json.length()
                && Character.isWhitespace(json.charAt(inicio))) {
            inicio++;
        }

        int fim = inicio;

        while (fim < json.length()
                && (Character.isDigit(json.charAt(fim))
                || json.charAt(fim) == '-')) {
            fim++;
        }

        if (fim == inicio) {
            return null;
        }

        try {
            return Long.parseLong(json.substring(inicio, fim));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
