package com.refugio.nueva_vida.proyecto_de_aula.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Aviso genérico que viaja por WebSocket hacia la campanita del admin.
 *
 * Antes estaba amarrado solo a citas (idCita / nombreAdoptante / nombrePerro).
 * Ahora es genérico: lleva un mensaje ya armado y a dónde ir al hacer clic,
 * así la misma campanita sirve para citas, testimonios y lo que venga después.
 */
@Data
@Builder
public class AdminNotificacionDTO implements Serializable {

    public enum Tipo {
        CITA,
        TESTIMONIO
    }

    /** Para saber de qué se trata el aviso (útil si luego se quiere filtrar) */
    private Tipo tipo;

    /** Emoji que se muestra al lado del aviso */
    private String icono;

    /** Texto ya listo para mostrar. Admite <strong> para resaltar */
    private String mensaje;

    /** Ruta a la que se navega al hacer clic. Ej: "/admin/cita/12" o "/testimonios" */
    private String enlace;
}