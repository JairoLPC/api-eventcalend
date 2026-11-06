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

    public void atualizarPerfil(String nome, String loguin, String telefone, String profissao) {
        this.nome = textoOuAtual(nome, this.nome);
        this.loguin = textoOuAtual(loguin, this.loguin);
        this.telefone = textoOuAtual(telefone, this.telefone);
        this.profissao = textoOuAtual(profissao, this.profissao);
    }

    public void atualizarSenha(String senhaCriptografada) {
        this.senha = textoOuAtual(senhaCriptografada, this.senha);
    }

    private static String textoOuAtual(String novoValor, String valorAtual) {
        return (novoValor != null && !novoValor.isBlank()) ? novoValor.trim() : valorAtual;
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
