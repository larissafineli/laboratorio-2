package com.aluguel.service;

import com.aluguel.exception.Excecoes.*;
import com.aluguel.model.Automovel;
import com.aluguel.repository.AutomovelRepository;
import com.aluguel.repository.PedidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AutomovelService {

    private final AutomovelRepository automoveis;
    private final PedidoRepository pedidos;

    public AutomovelService(AutomovelRepository automoveis, PedidoRepository pedidos) {
        this.automoveis = automoveis;
        this.pedidos = pedidos;
    }

    public List<Automovel> listar() {
        return automoveis.findAll();
    }

    public Automovel criar(Automovel a) {
        if (automoveis.existsByPlaca(a.getPlaca())) throw new RegraNegocioException("Placa ja cadastrada");
        if (automoveis.existsByMatricula(a.getMatricula())) throw new RegraNegocioException("Matricula ja cadastrada");
        a.setId(null);
        return automoveis.save(a);
    }

    public Automovel atualizar(Long id, Automovel dados) {
        Automovel a = automoveis.findById(id).orElseThrow(() -> new NaoEncontradoException("Automovel nao encontrado"));
        if (automoveis.existsByPlacaAndIdNot(dados.getPlaca(), id)) throw new RegraNegocioException("Placa ja cadastrada");
        if (automoveis.existsByMatriculaAndIdNot(dados.getMatricula(), id)) throw new RegraNegocioException("Matricula ja cadastrada");
        a.setMatricula(dados.getMatricula());
        a.setAno(dados.getAno());
        a.setMarca(dados.getMarca());
        a.setModelo(dados.getModelo());
        a.setPlaca(dados.getPlaca());
        a.setProprietario(dados.getProprietario());
        return automoveis.save(a);
    }

    public void remover(Long id) {
        if (!automoveis.existsById(id)) throw new NaoEncontradoException("Automovel nao encontrado");
        if (pedidos.existsByAutomovelId(id)) throw new RegraNegocioException("Automovel possui pedidos e nao pode ser removido");
        automoveis.deleteById(id);
    }
}
