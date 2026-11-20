package br.fmc.kaptu.job;

import br.fmc.kaptu.domain.imovel.Contrato;
import br.fmc.kaptu.domain.imovel.StatusContrato;
import br.fmc.kaptu.repository.ContratoRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContratoVencimentoJob {

    private final ContratoRepository contratoRepository;

    public ContratoVencimentoJob(ContratoRepository contratoRepository) {
        this.contratoRepository = contratoRepository;
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Manaus")
    @Transactional
    public void marcarVencidos() {
        LocalDate hoje = LocalDate.now();

        List<Contrato> vigentes = contratoRepository
                .findByStatusContratoAndDataValidadeBefore(
                        StatusContrato.VIGENTE,
                        hoje
                );

        for (Contrato contrato : vigentes) {
            contrato.marcarVencido();
        }
    }
}
