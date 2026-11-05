package com.aluguel.service;

import com.aluguel.dto.Dtos.DadosCliente;
import com.aluguel.exception.Excecoes.*;
import com.aluguel.model.Cliente;
import com.aluguel.model.Rendimento;
import com.aluguel.model.Usuario;
import com.aluguel.repository.ClienteRepository;
import com.aluguel.repository.PedidoRepository;
import com.aluguel.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clientes;
    private final UsuarioRepository usuarios;
    private final PedidoRepository pedidos;
    private final UsuarioService usuarioService;

    public ClienteService(ClienteRepository clientes, UsuarioRepository usuarios,
                          PedidoRepository pedidos, UsuarioService usuarioService) {
        this.clientes = clientes;
        this.usuarios = usuarios;
        this.pedidos = pedidos;
        this.usuarioService = usuarioService;
    }

    static void aplicar(Cliente c, DadosCliente d) {
        c.setRg(d.rg());
        c.setCpf(d.cpf());
        c.setNome(d.nome());
        c.setEndereco(d.endereco());
        c.setProfissao(d.profissao());
        c.getRendimentos().clear();
        if (d.rendimentos() != null) {
            if (d.rendimentos().size() > Cliente.MAX_RENDIMENTOS) {
                throw new RegraNegocioException("Maximo de 3 rendimentos");
            }
            for (Rendimento r : d.rendimentos()) {
                Rendimento novo = new Rendimento();
                novo.setEntidadeEmpregadora(r.getEntidadeEmpregadora());
                novo.setValor(r.getValor());
                c.getRendimentos().add(novo);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clientes.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente buscar(Long id, String login) {
        Cliente c = clientes.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Cliente nao encontrado"));
        verificarAcesso(c, login);
        return c;
    }

    @Transactional
    public Cliente atualizar(Long id, DadosCliente dados, String login) {
        Cliente c = buscar(id, login);
        if (clientes.existsByCpfAndIdNot(dados.cpf(), id)) {
            throw new RegraNegocioException("CPF ja cadastrado");
        }
        aplicar(c, dados);
        c.getUsuario().setNome(dados.nome());
        return clientes.save(c);
    }

    @Transactional
    public void remover(Long id, String login) {
        Cliente c = buscar(id, login);
        if (pedidos.existsByClienteId(id)) {
            throw new RegraNegocioException("Cliente possui pedidos e nao pode ser removido");
        }
        Usuario u = c.getUsuario();
        clientes.delete(c);
        usuarios.delete(u);
    }

    /** Agentes acessam qualquer cliente; cliente acessa apenas o proprio cadastro. */
    private void verificarAcesso(Cliente c, String login) {
        Usuario u = usuarioService.porLogin(login);
        if (!u.getPapel().isAgente() && !c.getUsuario().getId().equals(u.getId())) {
            throw new AcessoNegadoException("Acesso negado");
        }
    }
}
