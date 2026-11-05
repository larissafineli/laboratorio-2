package com.aluguel.config;

import com.aluguel.dto.Dtos.*;
import com.aluguel.model.*;
import com.aluguel.repository.AutomovelRepository;
import com.aluguel.repository.UsuarioRepository;
import com.aluguel.service.UsuarioService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Dados de demonstracao. Ative com app.seed=true (variavel APP_SEED=true). */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private final UsuarioService usuarios;
    private final UsuarioRepository usuarioRepo;
    private final AutomovelRepository automoveis;

    public DataSeeder(UsuarioService usuarios, UsuarioRepository usuarioRepo, AutomovelRepository automoveis) {
        this.usuarios = usuarios;
        this.usuarioRepo = usuarioRepo;
        this.automoveis = automoveis;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepo.count() > 0) return;

        usuarios.registrar(new RegistroRequest("empresa", "123456", Papel.AGENTE_EMPRESA, "Locadora Central", null));
        usuarios.registrar(new RegistroRequest("banco", "123456", Papel.AGENTE_BANCO, "Banco Minas", null));

        Rendimento r = new Rendimento();
        r.setEntidadeEmpregadora("Tech Ltda");
        r.setValor(new BigDecimal("6500.00"));
        usuarios.registrar(new RegistroRequest("cliente", "123456", Papel.CLIENTE, "Maria Silva",
                new DadosCliente("MG1234567", "12345678901", "Maria Silva", "Rua A, 100 - Belo Horizonte",
                        "Engenheira", List.of(r))));

        carro("MAT001", 2023, "Toyota", "Corolla", "ABC1D23", TipoProprietario.EMPRESA);
        carro("MAT002", 2022, "Honda", "Civic", "DEF4G56", TipoProprietario.BANCO);
        carro("MAT003", 2024, "Fiat", "Argo", "GHI7J89", TipoProprietario.EMPRESA);
    }

    private void carro(String mat, int ano, String marca, String modelo, String placa, TipoProprietario p) {
        Automovel a = new Automovel();
        a.setMatricula(mat); a.setAno(ano); a.setMarca(marca); a.setModelo(modelo);
        a.setPlaca(placa); a.setProprietario(p);
        automoveis.save(a);
    }
}
