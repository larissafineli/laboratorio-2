package com.aluguel.service;

import com.aluguel.dto.Dtos.*;
import com.aluguel.exception.Excecoes.*;
import com.aluguel.model.*;
import com.aluguel.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final AutomovelRepository automoveis;
    private final UsuarioRepository usuarios;
    private final UsuarioService usuarioService;

    public PedidoService(PedidoRepository pedidos, ClienteRepository clientes, AutomovelRepository automoveis,
                         UsuarioRepository usuarios, UsuarioService usuarioService) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.automoveis = automoveis;
        this.usuarios = usuarios;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listar(String login) {
        Usuario u = usuarioService.porLogin(login);
        if (u.getPapel().isAgente()) return pedidos.findAllByOrderByIdDesc();
        return clientes.findByUsuarioId(u.getId())
                .map(c -> pedidos.findByClienteIdOrderByIdDesc(c.getId()))
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public Pedido buscar(Long id, String login) {
        Pedido p = pedidos.findById(id).orElseThrow(() -> new NaoEncontradoException("Pedido nao encontrado"));
        verificarAcesso(p, usuarioService.porLogin(login));
        return p;
    }

    @Transactional
    public Pedido criar(PedidoRequest req, String login) {
        Usuario u = usuarioService.porLogin(login);
        if (u.getPapel().isAgente()) throw new AcessoNegadoException("Apenas clientes introduzem pedidos");
        Cliente cliente = clientes.findByUsuarioId(u.getId())
                .orElseThrow(() -> new NaoEncontradoException("Cliente nao encontrado"));
        if (req.dataInicio().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("A data de inicio nao pode ser anterior a hoje");
        }
        Pedido p = new Pedido();
        p.setCliente(cliente);
        p.setStatus(StatusPedido.EM_ANALISE);
        preencher(p, req);
        return pedidos.save(p);
    }

    @Transactional
    public Pedido modificar(Long id, PedidoRequest req, String login) {
        Usuario u = usuarioService.porLogin(login);
        Pedido p = pedidos.findById(id).orElseThrow(() -> new NaoEncontradoException("Pedido nao encontrado"));
        verificarAcesso(p, u);
        if (p.getStatus() == StatusPedido.CANCELADO || p.getStatus() == StatusPedido.CONTRATADO) {
            throw new RegraNegocioException("Pedido " + p.getStatus() + " nao pode ser modificado");
        }
        preencher(p, req);
        // Apos modificacao o pedido volta a ser analisado.
        p.setStatus(StatusPedido.EM_ANALISE);
        p.setParecer(null);
        return pedidos.save(p);
    }

    @Transactional
    public Pedido cancelar(Long id, String login) {
        Usuario u = usuarioService.porLogin(login);
        Pedido p = pedidos.findById(id).orElseThrow(() -> new NaoEncontradoException("Pedido nao encontrado"));
        if (u.getPapel().isAgente()) throw new AcessoNegadoException("Apenas clientes cancelam pedidos");
        verificarAcesso(p, u);
        if (p.getStatus() == StatusPedido.CONTRATADO) {
            throw new RegraNegocioException("Pedido ja contratado nao pode ser cancelado");
        }
        p.setStatus(StatusPedido.CANCELADO);
        return pedidos.save(p);
    }

    @Transactional
    public Pedido avaliar(Long id, AvaliacaoRequest req, String login) {
        Usuario u = exigirAgente(login);
        Pedido p = pedidos.findById(id).orElseThrow(() -> new NaoEncontradoException("Pedido nao encontrado"));
        if (p.getStatus() != StatusPedido.EM_ANALISE) {
            throw new RegraNegocioException("Apenas pedidos em analise podem ser avaliados");
        }
        p.setStatus(req.aprovado() ? StatusPedido.APROVADO : StatusPedido.REPROVADO);
        p.setParecer((req.parecer() == null ? "" : req.parecer() + " ") + "(" + u.getNome() + ")");
        return pedidos.save(p);
    }

    @Transactional
    public Pedido contratar(Long id, String login) {
        exigirAgente(login);
        Pedido p = pedidos.findById(id).orElseThrow(() -> new NaoEncontradoException("Pedido nao encontrado"));
        if (p.getStatus() != StatusPedido.APROVADO) {
            throw new RegraNegocioException("Apenas pedidos aprovados podem virar contrato");
        }
        p.setStatus(StatusPedido.CONTRATADO);
        return pedidos.save(p);
    }

    private void preencher(Pedido p, PedidoRequest req) {
        if (req.dataFim().isBefore(req.dataInicio())) {
            throw new RegraNegocioException("Data final anterior a data inicial");
        }
        p.setAutomovel(automoveis.findById(req.automovelId())
                .orElseThrow(() -> new NaoEncontradoException("Automovel nao encontrado")));
        p.setDataInicio(req.dataInicio());
        p.setDataFim(req.dataFim());
        p.setValorDiaria(req.valorDiaria());

        if (req.bancoId() != null) {
            if (req.valorCredito() == null || req.taxaJurosMensal() == null) {
                throw new RegraNegocioException("Informe valor e taxa do credito");
            }
            Usuario banco = usuarios.findById(req.bancoId())
                    .filter(b -> b.getPapel() == Papel.AGENTE_BANCO)
                    .orElseThrow(() -> new RegraNegocioException("Banco invalido"));
            ContratoCredito cc = p.getContratoCredito() != null ? p.getContratoCredito() : new ContratoCredito();
            cc.setBanco(banco);
            cc.setValor(req.valorCredito());
            cc.setTaxaJurosMensal(req.taxaJurosMensal());
            p.setContratoCredito(cc);
        } else {
            p.setContratoCredito(null);
        }
    }

    private Usuario exigirAgente(String login) {
        Usuario u = usuarioService.porLogin(login);
        if (!u.getPapel().isAgente()) throw new AcessoNegadoException("Apenas agentes");
        return u;
    }

    private void verificarAcesso(Pedido p, Usuario u) {
        if (!u.getPapel().isAgente() && !p.getCliente().getUsuario().getId().equals(u.getId())) {
            throw new AcessoNegadoException("Acesso negado");
        }
    }
}
