package com.eventcalend.api.domain.usuario;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "cadastro")
@Entity(name = "Cadastro")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Cadastro implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loguin;
    private String senha;
    private String nome;
    private String telefone;
    private String profissao;

    public Cadastro(Long id, String loguin, String senha) {
        this(id, loguin, senha, null, "(11) 98765-4321", "Produtor(a) de eventos");
    }

    public void atualizarPerfil(String nome, String loguin, String telefone, String profissao) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome.trim();
        }
        if (loguin != null && !loguin.isBlank()) {
            this.loguin = loguin.trim();
        }
        if (telefone != null && !telefone.isBlank()) {
            this.telefone = telefone.trim();
        }
        if (profissao != null && !profissao.isBlank()) {
            this.profissao = profissao.trim();
        }
    }

    public void atualizarSenha(String senhaCriptografada) {
        if (senhaCriptografada != null && !senhaCriptografada.isBlank()) {
            this.senha = senhaCriptografada;
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return loguin;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
