package br.fmc.kaptu.domain.usuario;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.EnumSet;
import java.util.Set;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {

    protected Administrador() {
    }

    public Administrador(String nome, String email, String senhaHash) {
        super(nome, email, senhaHash);
    }

    @Override
    public Set<Permissao> getPermissoes() {
        return EnumSet.allOf(Permissao.class);
    }
}