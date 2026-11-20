package br.fmc.kaptu.domain.usuario;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.EnumSet;
import java.util.Set;

@Entity
@DiscriminatorValue("FUNCIONARIO")
public class Funcionario extends Usuario {

    protected Funcionario() {
    }

    public Funcionario(String nome, String email, String senhaHash) {
        super(nome, email, senhaHash);
    }

    @Override
    public Set<Permissao> getPermissoes() {
        return EnumSet.of(
                Permissao.CADASTRAR,
                Permissao.ENVIAR_DOCUMENTOS,
                Permissao.LANCAR_TRANSACOES,
                Permissao.CONSULTAR
        );
    }
}