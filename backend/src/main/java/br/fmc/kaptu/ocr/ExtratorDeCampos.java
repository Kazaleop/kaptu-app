
package br.fmc.kaptu.ocr;

import br.fmc.kaptu.domain.documento.TipoDocumento;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExtratorDeCampos {

    private static final Pattern CNPJ = Pattern.compile(
            "\\b\\d{2}\\.??\\d{3}\\.??\\d{3}/?\\d{4}-?\\d{2}\\b"
    );

    private static final Pattern CHAVE_ACESSO = Pattern.compile(
            "\\b\\d{44}\\b"
    );

    private static final Pattern VALOR = Pattern.compile(
            "(?:valor total|valor do aluguel|valor|total)\\s*[:\\-]?\\s*"
                    + "R?\\$?\\s*([\\d.]+,\\d{2}|\\d+\\.\\d{2})",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DATA = Pattern.compile(
            "\\b(\\d{2}/\\d{2}/\\d{4})\\b"
    );

    public Map<String, String> extrair(
            String texto,
            TipoDocumento tipo) {

        Map<String, String> campos = new HashMap<>();

        if (texto == null || texto.isBlank() || tipo == null) {
            return campos;
        }

        String normalizado = normalizar(texto);

        switch (tipo) {
            case NOTA_FISCAL -> extrairNotaFiscal(
                    texto, normalizado, campos
            );
            case CONTRATO -> extrairContrato(
                    texto, normalizado, campos
            );
            case RECIBO -> extrairRecibo(
                    texto, normalizado, campos
            );
            case CERTIDAO -> extrairCertidao(
                    texto, normalizado, campos
            );
        }

        return campos;
    }

    private void extrairNotaFiscal(
            String texto,
            String normalizado,
            Map<String, String> campos) {

        adicionarSeEncontrar(campos, "cnpjEmissor", CNPJ, texto);
        adicionarSeEncontrar(campos, "chaveAcesso",
                CHAVE_ACESSO, texto);

        String numero = encontrar(
                "(?:numero|nº|n°|no)\\s*[:\\-]?\\s*(\\d+)",
                normalizado
        );

        adicionar(campos, "numeroNota", numero);

        adicionarValor(campos, "valorTotal", texto);
    }

    private void extrairContrato(
            String texto,
            String normalizado,
            Map<String, String> campos) {

        String prazo = encontrar(
                "(\\d{1,3})\\s+meses",
                normalizado
        );

        adicionar(campos, "prazoMeses", prazo);

        String data = primeiraData(texto);
        adicionar(campos, "dataValidade", converterData(data));

        adicionarValor(campos, "valorAluguel", texto);
    }

    private void extrairRecibo(
            String texto,
            String normalizado,
            Map<String, String> campos) {

        String pagador = encontrar(
                "(?:pagador|recebi de)\\s*[:\\-]?\\s*([^\\n\\r]+)",
                normalizado
        );

        String favorecido = encontrar(
                "(?:favorecido|recebedor|para)\\s*[:\\-]?\\s*([^\\n\\r]+)",
                normalizado
        );

        String transacao = encontrar(
                "(?:transacao|id da transacao)\\s*[:\\-]?\\s*([\\w-]+)",
                normalizado
        );

        adicionar(campos, "pagador", pagador);
        adicionar(campos, "favorecido", favorecido);
        adicionar(campos, "numTransacao", transacao);

        adicionarValor(campos, "valor", texto);
    }

    private void extrairCertidao(
            String texto,
            String normalizado,
            Map<String, String> campos) {

        String data = primeiraData(texto);
        adicionar(campos, "dataValidade", converterData(data));

        if (normalizado.contains("municipal")) {
            adicionar(campos, "tipoCertidao", "MUNICIPAL");
        } else if (normalizado.contains("estadual")) {
            adicionar(campos, "tipoCertidao", "ESTADUAL");
        } else if (normalizado.contains("federal")) {
            adicionar(campos, "tipoCertidao", "FEDERAL");
        }

        String orgao = encontrar(
                "(?:orgao emissor|emitida por)\\s*[:\\-]?\\s*([^\\n\\r]+)",
                normalizado
        );

        adicionar(campos, "orgaoEmissor", orgao);
    }

    private void adicionarValor(
            Map<String, String> campos,
            String chave,
            String texto) {

        Matcher matcher = VALOR.matcher(normalizar(texto));

        if (matcher.find()) {
            String valor = matcher.group(1)
                    .replace(".", "")
                    .replace(",", ".");

            adicionar(campos, chave, valor);
        }
    }

    private void adicionarSeEncontrar(
            Map<String, String> campos,
            String chave,
            Pattern pattern,
            String texto) {

        Matcher matcher = pattern.matcher(texto);

        if (matcher.find()) {
            adicionar(campos, chave, matcher.group());
        }
    }

    private String primeiraData(String texto) {
        Matcher matcher = DATA.matcher(texto);

        return matcher.find() ? matcher.group(1) : null;
    }

    private String converterData(String data) {
        if (data == null) {
            return null;
        }

        String[] partes = data.split("/");

        return partes[2] + "-" + partes[1] + "-" + partes[0];
    }

    private String encontrar(String regex, String texto) {
        Matcher matcher = Pattern.compile(
                regex,
                Pattern.CASE_INSENSITIVE
        ).matcher(texto);

        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private void adicionar(
            Map<String, String> campos,
            String chave,
            String valor) {

        if (valor != null && !valor.isBlank()) {
            campos.put(chave, valor.trim());
        }
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(
                        texto,
                        Normalizer.Form.NFD
                ).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }
}
