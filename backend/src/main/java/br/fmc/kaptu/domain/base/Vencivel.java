package br.fmc.kaptu.domain.base;

import br.fmc.kaptu.domain.alerta.TipoAlerta;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public interface Vencivel {

    Long getId();

    LocalDate getDataVencimento();

    TipoAlerta getTipoAlerta();

    String getDescricaoAlerta();

    default long diasParaVencer(LocalDate hoje) {
        return ChronoUnit.DAYS.between(
                hoje,
                getDataVencimento()
        );
    }
}