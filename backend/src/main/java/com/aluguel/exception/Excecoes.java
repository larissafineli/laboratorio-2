package com.aluguel.exception;

public final class Excecoes {
    private Excecoes() {}

    public static class RegraNegocioException extends RuntimeException {
        public RegraNegocioException(String msg) { super(msg); }
    }

    public static class NaoEncontradoException extends RuntimeException {
        public NaoEncontradoException(String msg) { super(msg); }
    }

    public static class AcessoNegadoException extends RuntimeException {
        public AcessoNegadoException(String msg) { super(msg); }
    }
}
