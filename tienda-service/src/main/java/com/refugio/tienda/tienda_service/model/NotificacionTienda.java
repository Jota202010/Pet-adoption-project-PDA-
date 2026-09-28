package com.refugio.tienda.tienda_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notificacion_tienda")
public class NotificacionTienda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    // Id de la receta en PDA que originó esta notificación (null en las antiguas)
    @Column(name = "id_receta")
    private Long idReceta;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(length = 500)
    private String mensaje;

    @Column(name = "productos_sugeridos", length = 500)
    private String productosSugeridos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad = Prioridad.MEDIA;

    @Column(nullable = false)
    private boolean leida = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public enum Prioridad {
        ALTA,
        MEDIA,
        BAJA
    }
}