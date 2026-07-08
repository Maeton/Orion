package com.orion.mediaservice.Exceptions;

public class StorageException extends RuntimeException {
    public StorageException(String mensaje) {
        super(mensaje);
    }

    public StorageException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
