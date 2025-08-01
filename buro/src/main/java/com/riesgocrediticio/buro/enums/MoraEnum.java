package com.riesgocrediticio.buro.enums;

public enum MoraEnum {
    SI("SI"),
    NO("NO");

    private final String valor;

    MoraEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}