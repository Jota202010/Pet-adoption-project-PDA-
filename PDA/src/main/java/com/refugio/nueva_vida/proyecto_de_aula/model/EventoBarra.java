package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_barra")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoBarra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento")
    private Long idEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perro", nullable = false)
    private Perro perro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_barra", nullable = false)
    private TipoBarra tipoBarra;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 30)
    private TipoEvento tipoEvento;

    @Column(name = "valor_cambio", nullable = false)
    private Integer valorCambio;

    @Column(name = "valor_resultante", nullable = false)
    private Integer valorResultante;

    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;
}