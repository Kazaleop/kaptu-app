
package br.fmc.kaptu.ocr;

import br.fmc.kaptu.domain.documento.Certidao;
import br.fmc.kaptu.domain.documento.Documento;
import br.fmc.kaptu.domain.documento.DocumentoContrato;
import br.fmc.kaptu.domain.documento.NotaFiscal;
import br.fmc.kaptu.domain.documento.Recibo;
import br.fmc.kaptu.domain.documento.TipoDocumento;
import br.fmc.kaptu.domain.excecao.RegraDeNegocioExcecao;
import org.springframework.stereotype.Component;

@Component
public class DocumentoFactory {

    public Documento criar(TipoDocumento tipo) {
        if (tipo == null) {
            throw new RegraDeNegocioExcecao(
                    "O tipo do documento é obrigatório."
            );
        }

        return switch (tipo) {
            case NOTA_FISCAL -> new NotaFiscal();
            case CONTRATO -> new DocumentoContrato();
            case RECIBO -> new Recibo();
            case CERTIDAO -> new Certidao();
        };
    }
}
