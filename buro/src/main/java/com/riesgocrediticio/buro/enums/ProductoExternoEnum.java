package com.riesgocrediticio.buro.enums;

public enum ProductoExternoEnum {
    TARJETA_DE_CREDITO("TARJETA DE CREDITO"),
    PRESTAMO("PRESTAMO");

    private final String valor;

    ProductoExternoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
