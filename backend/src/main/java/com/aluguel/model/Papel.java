package com.aluguel.model;

public enum Papel {
    CLIENTE,
    AGENTE_EMPRESA,
    AGENTE_BANCO;

    public boolean isAgente() {
        return this != CLIENTE;
    }
}
