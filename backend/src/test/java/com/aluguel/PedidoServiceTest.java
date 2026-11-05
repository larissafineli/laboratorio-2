package com.aluguel;

import com.aluguel.dto.Dtos.*;
import com.aluguel.exception.Excecoes.*;
import com.aluguel.model.*;
import com.aluguel.repository.AutomovelRepository;
import com.aluguel.service.PedidoService;
import com.aluguel.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PedidoServiceTest {

    @Autowired UsuarioService usuarios;
    @Autowired PedidoService pedidos;
    @Autowired AutomovelRepository automoveis;

    String cliente, agente;
    Long automovelId;

    @BeforeEach
    void preparar() {
        String s = UUID.randomUUID().toString().substring(0, 8);
        cliente = "c" + s;
        agente = "a" + s;
        usuarios.registrar(new RegistroRequest(cliente, "123456", Papel.CLIENTE, "Cli",
                dados(s, List.of())));
        usuarios.registrar(new RegistroRequest(agente, "123456", Papel.AGENTE_EMPRESA, "Emp", null));
        Automovel a = new Automovel();
        a.setMatricula("M" + s); a.setPlaca("P" + s); a.setAno(2024);
        a.setMarca("Fiat"); a.setModelo("Argo"); a.setProprietario(TipoProprietario.EMPRESA);
        automovelId = automoveis.save(a).getId();
    }

    DadosCliente dados(String s, List<Rendimento> r) {
        String cpf = String.valueOf(Math.abs(s.hashCode()) % 90000000000L + 10000000000L);
        return new DadosCliente("RG" + s, cpf, "Cli", "Rua X", "Dev", r);
    }

    PedidoRequest req() {
        return new PedidoRequest(automovelId, LocalDate.now(), LocalDate.now().plusDays(3),
                new BigDecimal("100"), null, null, null);
    }

    @Test
    void pedidoNasceEmAnalise() {
        assertEquals(StatusPedido.EM_ANALISE, pedidos.criar(req(), cliente).getStatus());
    }

    @Test
    void fluxoCompletoAprovarEContratar() {
        Pedido p = pedidos.criar(req(), cliente);
        pedidos.avaliar(p.getId(), new AvaliacaoRequest(true, "ok"), agente);
        assertEquals(StatusPedido.CONTRATADO, pedidos.contratar(p.getId(), agente).getStatus());
    }

    @Test
    void naoContrataSemAprovar() {
        Pedido p = pedidos.criar(req(), cliente);
        assertThrows(RegraNegocioException.class, () -> pedidos.contratar(p.getId(), agente));
    }

    @Test
    void clienteNaoAvalia() {
        Pedido p = pedidos.criar(req(), cliente);
        assertThrows(AcessoNegadoException.class,
                () -> pedidos.avaliar(p.getId(), new AvaliacaoRequest(true, ""), cliente));
    }

    @Test
    void agenteNaoCriaPedido() {
        assertThrows(AcessoNegadoException.class, () -> pedidos.criar(req(), agente));
    }

    @Test
    void cancelarEImpedirModificacao() {
        Pedido p = pedidos.criar(req(), cliente);
        pedidos.cancelar(p.getId(), cliente);
        assertThrows(RegraNegocioException.class, () -> pedidos.modificar(p.getId(), req(), cliente));
    }

    @Test
    void agenteModificaPedidoEVoltaParaAnalise() {
        Pedido p = pedidos.criar(req(), cliente);
        pedidos.avaliar(p.getId(), new AvaliacaoRequest(true, ""), agente);
        assertEquals(StatusPedido.EM_ANALISE, pedidos.modificar(p.getId(), req(), agente).getStatus());
    }

    @Test
    void dataFinalAnteriorEhRejeitada() {
        PedidoRequest ruim = new PedidoRequest(automovelId, LocalDate.now().plusDays(5), LocalDate.now(),
                new BigDecimal("100"), null, null, null);
        assertThrows(RegraNegocioException.class, () -> pedidos.criar(ruim, cliente));
    }

    @Test
    void maximoTresRendimentos() {
        List<Rendimento> quatro = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Rendimento r = new Rendimento();
            r.setEntidadeEmpregadora("E" + i);
            r.setValor(BigDecimal.TEN);
            quatro.add(r);
        }
        assertThrows(RegraNegocioException.class, () -> usuarios.registrar(
                new RegistroRequest("x" + UUID.randomUUID().toString().substring(0, 6), "123456",
                        Papel.CLIENTE, "X", dados("zzz", quatro))));
    }

    @Test
    void loginDuplicadoEhRejeitado() {
        assertThrows(RegraNegocioException.class,
                () -> usuarios.registrar(new RegistroRequest(agente, "123456", Papel.AGENTE_BANCO, "B", null)));
    }
}
