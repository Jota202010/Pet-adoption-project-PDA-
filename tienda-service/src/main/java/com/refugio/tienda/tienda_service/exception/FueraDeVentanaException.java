package com.refugio.tienda.tienda_service.exception;

/**
 * Se lanza cuando se intenta registrar una liquidación
 * fuera de los días permitidos.
 */
public class FueraDeVentanaException extends RuntimeException {

    public FueraDeVentanaException(String mensaje) {
        super(mensaje);
    }
}
