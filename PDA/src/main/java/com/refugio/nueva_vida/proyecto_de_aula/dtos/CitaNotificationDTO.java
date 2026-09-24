package com.refugio.nueva_vida.proyecto_de_aula.dtos;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class CitaNotificationDTO implements Serializable {

    public enum TipoEvento {
        SOLICITUD_RECIBIDA,
        PRE_APROBADA,
        RECHAZADA,
        CONFIRMADA
    }

    private Integer idCita;
    private String nombreAdoptante;
    private String emailAdoptante;
    private String nombrePerro;
    private TipoEvento tipoEvento;
}