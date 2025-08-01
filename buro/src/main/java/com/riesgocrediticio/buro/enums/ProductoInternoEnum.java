package com.riesgocrediticio.buro.enums;

public enum ProductoInternoEnum {
    TARJETA_DE_CREDITO("TARJETA DE CREDITO"),
    PRESTAMO("PRESTAMO");

    private final String valor;

    ProductoInternoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
