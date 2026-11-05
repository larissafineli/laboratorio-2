package com.aluguel.service;

import com.aluguel.dto.Dtos.*;
import com.aluguel.exception.Excecoes.*;
import com.aluguel.model.*;
import com.aluguel.repository.ClienteRepository;
import com.aluguel.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarios;
    private final ClienteRepository clientes;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, ClienteRepository clientes, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.clientes = clientes;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario u = usuarios.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException(login));
        return org.springframework.security.core.userdetails.User.withUsername(u.getLogin())
                .password(u.getSenhaHash())
                .roles(u.getPapel().name())
                .build();
    }

    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        if (usuarios.existsByLogin(req.login())) {
            throw new RegraNegocioException("Login ja cadastrado");
        }
        if (req.papel() == Papel.CLIENTE) {
            if (req.cliente() == null) {
                throw new RegraNegocioException("Dados do cliente sao obrigatorios");
            }
            if (clientes.existsByCpf(req.cliente().cpf())) {
                throw new RegraNegocioException("CPF ja cadastrado");
            }
        }

        Usuario u = new Usuario();
        u.setLogin(req.login());
        u.setSenhaHash(encoder.encode(req.senha()));
        u.setNome(req.nome());
        u.setPapel(req.papel());
        usuarios.save(u);

        Long clienteId = null;
        if (req.papel() == Papel.CLIENTE) {
            Cliente c = new Cliente();
            c.setUsuario(u);
            ClienteService.aplicar(c, req.cliente());
            clienteId = clientes.save(c).getId();
        }
        return new UsuarioResponse(u.getId(), u.getLogin(), u.getNome(), u.getPapel(), clienteId);
    }

    @Transactional(readOnly = true)
    public Usuario porLogin(String login) {
        return usuarios.findByLogin(login)
                .orElseThrow(() -> new NaoEncontradoException("Usuario nao encontrado"));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse resposta(String login) {
        Usuario u = porLogin(login);
        Long clienteId = clientes.findByUsuarioId(u.getId()).map(Cliente::getId).orElse(null);
        return new UsuarioResponse(u.getId(), u.getLogin(), u.getNome(), u.getPapel(), clienteId);
    }

    @Transactional(readOnly = true)
    public java.util.List<UsuarioResponse> bancos() {
        return usuarios.findAll().stream()
                .filter(u -> u.getPapel() == Papel.AGENTE_BANCO)
                .map(u -> new UsuarioResponse(u.getId(), u.getLogin(), u.getNome(), u.getPapel(), null))
                .toList();
    }
}
