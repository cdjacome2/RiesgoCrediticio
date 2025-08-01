package com.riesgocrediticio.buro.enums;

public enum MoraTresMesesEnum {
    SI("SI"),
    NO("NO");

    private final String valor;

    MoraTresMesesEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
