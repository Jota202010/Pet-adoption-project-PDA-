package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "seguimiento_envio")
public class SeguimientoEnvio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguimiento")
    private Integer idSeguimiento;

    @Column(name = "id_pedido", nullable = false)
    private Integer idPedido;

    @Column(name = "id_perro")
    private Integer idPerro;

    @Column(nullable = false, length = 50)
    private String estado;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}