package br.fmc.kaptu.web.dto;

import br.fmc.kaptu.domain.imovel.StatusOcupacao;
import java.math.BigDecimal;

public record ImovelDTO(
        Long id,
        String nome,
        String endereco,
        String bairro,
        int quantResidentes,
        BigDecimal valorAluguelMensal,
        StatusOcupacao statusOcupacao
) {}