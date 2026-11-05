package com.aluguel.controller;

import com.aluguel.dto.Dtos.*;
import com.aluguel.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    /** Cadastro previo (publico). */
    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrar(req);
    }

    /** Login: o Spring Security valida o Basic Auth; este endpoint devolve o perfil. */
    @PostMapping("/auth/login")
    public UsuarioResponse login(Authentication auth) {
        return service.resposta(auth.getName());
    }

    @GetMapping("/auth/me")
    public UsuarioResponse me(Authentication auth) {
        return service.resposta(auth.getName());
    }

    @GetMapping("/bancos")
    public List<UsuarioResponse> bancos() {
        return service.bancos();
    }
}
