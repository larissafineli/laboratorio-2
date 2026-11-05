package com.aluguel.controller;

import com.aluguel.dto.Dtos.*;
import com.aluguel.model.Pedido;
import com.aluguel.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Pedido> listar(Authentication auth) {
        return service.listar(auth.getName());
    }

    @GetMapping("/{id}")
    public Pedido buscar(@PathVariable Long id, Authentication auth) {
        return service.buscar(id, auth.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido criar(@Valid @RequestBody PedidoRequest req, Authentication auth) {
        return service.criar(req, auth.getName());
    }

    @PutMapping("/{id}")
    public Pedido modificar(@PathVariable Long id, @Valid @RequestBody PedidoRequest req, Authentication auth) {
        return service.modificar(id, req, auth.getName());
    }

    @PostMapping("/{id}/cancelar")
    public Pedido cancelar(@PathVariable Long id, Authentication auth) {
        return service.cancelar(id, auth.getName());
    }

    @PostMapping("/{id}/avaliar")
    public Pedido avaliar(@PathVariable Long id, @Valid @RequestBody AvaliacaoRequest req, Authentication auth) {
        return service.avaliar(id, req, auth.getName());
    }

    @PostMapping("/{id}/contratar")
    public Pedido contratar(@PathVariable Long id, Authentication auth) {
        return service.contratar(id, auth.getName());
    }
}
