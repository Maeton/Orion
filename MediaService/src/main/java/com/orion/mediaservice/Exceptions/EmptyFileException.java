package com.orion.mediaservice.Exceptions;

public class EmptyFileException extends RuntimeException {
    public EmptyFileException(String mensaje) {
        super(mensaje);
    }
}
