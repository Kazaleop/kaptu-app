
package br.fmc.kaptu.ocr;

import br.fmc.kaptu.domain.documento.TipoDocumento;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class ClassificadorDeDocumento {

    public TipoDocumento classificar(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new RegraDeNegocioExcecao(
                    "Não foi possível identificar o conteúdo do documento."
            );
        }

        String conteudo = Normalizer.normalize(
                        texto,
                        Normalizer.Form.NFD
                ).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        if (contemAlgum(conteudo, "nota fiscal", "danfe", "nfe")) {
            return TipoDocumento.NOTA_FISCAL;
        }

        if (contemAlgum(conteudo,
                "recibo",
                "comprovante de pagamento",
                "recebi de")) {
            return TipoDocumento.RECIBO;
        }

        if (contemAlgum(conteudo,
                "certidao",
                "regularidade fiscal",
                "certifico que")) {
            return TipoDocumento.CERTIDAO;
        }

        if (contemAlgum(conteudo,
                "contrato de locacao",
                "contrato de aluguel",
                "locador",
                "locatario")) {
            return TipoDocumento.CONTRATO;
        }

        throw new RegraDeNegocioExcecao(
                "O tipo do documento não pôde ser identificado."
        );
    }

    private boolean contemAlgum(String texto, String... termos) {
        for (String termo : termos) {
            if (texto.contains(termo)) {
                return true;
            }
        }

        return false;
    }
}
