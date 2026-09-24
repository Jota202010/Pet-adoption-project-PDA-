package com.refugio.nueva_vida.proyecto_de_aula.exception;

public class PerroConCitasException extends RuntimeException {

    public PerroConCitasException(String nombrePerro) {
        super("No se puede eliminar a \"" + nombrePerro + "\" porque tiene citas registradas. " +
              "Elimina primero las citas asociadas.");
    }
}
