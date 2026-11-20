package br.fmc.kaptu.domain.usuario;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.EnumSet;
import java.util.Set;

@Entity
@DiscriminatorValue("GESTOR_ADMINISTRATIVO")
public class GestorAdministrativo extends Usuario {

    protected GestorAdministrativo() {
    }

    public GestorAdministrativo(String nome, String email, String senhaHash) {
        super(nome, email, senhaHash);
    }

    @Override
    public Set<Permissao> getPermissoes() {
        return EnumSet.of(
                Permissao.CONSULTAR,
                Permissao.CONCILIAR,
                Permissao.VER_RELATORIOS,
                Permissao.VER_DASHBOARD
        );
    }
}