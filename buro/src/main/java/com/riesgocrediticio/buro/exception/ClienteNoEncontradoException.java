package com.riesgocrediticio.buro.exception;

public class ClienteNoEncontradoException extends RuntimeException {
    private final Integer errorCode;

    public ClienteNoEncontradoException(String message) {
        super(message);
        this.errorCode = 4041; // Puedes poner el código que desees
    }

    public Integer getErrorCode() {
        return errorCode;
    }
    
}
