package com.aluguel.controller;

import com.aluguel.dto.Dtos.DadosCliente;
import com.aluguel.model.Cliente;
import com.aluguel.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** CRUD de cliente (o "C" acontece no cadastro em /api/auth/register). */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('AGENTE_EMPRESA','AGENTE_BANCO')")
    public List<Cliente> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id, Authentication auth) {
        return service.buscar(id, auth.getName());
    }

    @PutMapping("/{id}")
    public Cliente atualizar(@PathVariable Long id, @Valid @RequestBody DadosCliente dados, Authentication auth) {
        return service.atualizar(id, dados, auth.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id, Authentication auth) {
        service.remover(id, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
