package com.aluguel.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/** Contrato de credito concedido por um banco agente, associado a um pedido de aluguel. */
@Entity
@Table(name = "contratos_credito")
public class ContratoCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "banco_id")
    private Usuario banco;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private BigDecimal taxaJurosMensal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getBanco() { return banco; }
    public void setBanco(Usuario banco) { this.banco = banco; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public BigDecimal getTaxaJurosMensal() { return taxaJurosMensal; }
    public void setTaxaJurosMensal(BigDecimal t) { this.taxaJurosMensal = t; }
}
