package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "receta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Long idReceta;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_perro", nullable = false)
    private Perro perro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_necesidad", nullable = false)
    private TipoNecesidad tipoNecesidad;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "productos_sugeridos", columnDefinition = "TEXT")
    private String productosSugeridos;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadReceta prioridad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReceta estado = EstadoReceta.PENDIENTE;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion = LocalDateTime.now();

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    // ═══════════════════════════════════════════════════════
    // 🆕 Campo transient (no va a la BD) para la vista
    // ═══════════════════════════════════════════════════════

    @Transient
    private List<Map<String, Object>> productosParseados;
}