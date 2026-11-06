package com.aluguel.dto;

import com.aluguel.model.Papel;
import com.aluguel.model.Rendimento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record DadosCliente(
            @NotBlank String rg,
            @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 digitos") String cpf,
            @NotBlank String nome,
            @NotBlank String endereco,
            String profissao,
            @Size(max = 3, message = "maximo de 3 rendimentos") @Valid List<Rendimento> rendimentos) {}

    public record RegistroRequest(
            @NotBlank @Size(min = 3) String login,
            @NotBlank @Size(min = 6, message = "senha com no minimo 6 caracteres") String senha,
            @NotNull Papel papel,
            @NotBlank String nome,
            @Valid DadosCliente cliente) {}

    public record UsuarioResponse(Long id, String login, String nome, Papel papel, Long clienteId) {}

    public record PedidoRequest(
            @NotNull Long automovelId,
            @NotNull LocalDate dataInicio,
            @NotNull LocalDate dataFim,
            @NotNull @DecimalMin("0.01") BigDecimal valorDiaria,
            Long bancoId,
            @DecimalMin("0.01") BigDecimal valorCredito,
            @DecimalMin("0.0") BigDecimal taxaJurosMensal) {}

    public record AvaliacaoRequest(@NotNull Boolean aprovado, String parecer) {}
}
