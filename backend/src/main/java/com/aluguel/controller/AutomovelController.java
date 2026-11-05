package com.aluguel.controller;

import com.aluguel.model.Automovel;
import com.aluguel.service.AutomovelService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automoveis")
public class AutomovelController {

    private final AutomovelService service;

    public AutomovelController(AutomovelService service) {
        this.service = service;
    }

    @GetMapping
    public List<Automovel> listar() {
        return service.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('AGENTE_EMPRESA','AGENTE_BANCO')")
    public Automovel criar(@RequestBody Automovel automovel) {
        return service.criar(automovel);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('AGENTE_EMPRESA','AGENTE_BANCO')")
    public Automovel atualizar(@PathVariable Long id, @RequestBody Automovel automovel) {
        return service.atualizar(id, automovel);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('AGENTE_EMPRESA','AGENTE_BANCO')")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
