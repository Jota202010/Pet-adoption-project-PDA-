package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter  
@Setter 
@NoArgsConstructor 
@Entity
@Table(name = "cita")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Integer idCita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perro", nullable = false)
    private Perro perro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_admin")
    private Usuario admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoCita estado = EstadoCita.en_espera;

    // Fecha y hora — se llenan SOLO cuando el usuario elige horario (pre_aprobada → confirmada)
    @Column(name = "fecha_cita")
    private LocalDate fechaCita;

    @Column(name = "hora_cita")
    private LocalTime horaCita;

    @Column(name = "sede", length = 150)
    private String sede;

    @Column(name = "fecha_solicitud", nullable = false, updatable = false)
    private LocalDateTime fechaSolicitud = LocalDateTime.now();

    @Column(name = "fecha_decision")
    private LocalDateTime fechaDecision;

    // Referencia al horario que el usuario eligió
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario")
    private HorarioDisponible horario;

    // Información del hogar
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vivienda")
    private TipoVivienda tipoVivienda;

    @Enumerated(EnumType.STRING)
    @Column(name = "propiedad")
    private Propiedad propiedad;

    @Enumerated(EnumType.STRING)
    @Column(name = "permiten_mascotas")
    private PermitenMascotas permitenMascotas;

    @Column(name = "num_personas")
    private Integer numPersonas;

    @Column(name = "todos_acuerdo")
    private Boolean todosAcuerdo;

    @Column(name = "perros_antes")
    private Boolean perrosAntes;

    @Column(name = "mascotas_actual")
    private Boolean mascotasActual;

    @Column(name = "mascotas_anteriores", columnDefinition = "TEXT")
    private String mascotasAnteriores;

    @Column(name = "horas_solo", length = 30)
    private String horasSolo;

    @Column(name = "puede_pasear")
    private Boolean puedePasear;

    @Column(name = "responsable", length = 100)
    private String responsable;

    @Column(name = "cubre_vet")
    private Boolean cubreVet;

    @Column(name = "cubre_emergencias")
    private Boolean cubreEmergencias;

    @Column(name = "motivacion", columnDefinition = "TEXT")
    private String motivacion;

    @Column(name = "tipo_perro_buscado", columnDefinition = "TEXT")
    private String tipoPerroBuscado;

    @Column(name = "cond_no_abandono", nullable = false)
    private Boolean condNoAbandono = false;

    @Column(name = "cond_seguimiento", nullable = false)
    private Boolean condSeguimiento = false;

    @Column(name = "cond_evaluacion", nullable = false)
    private Boolean condEvaluacion = false;

    // ── Enums ────────────────────────────────────────────────────────────────
    public enum EstadoCita {
        en_espera,      // usuario envió solicitud
        pre_aprobada,   // admin revisó y pre-aprobó
        confirmada,     // usuario eligió horario → cita fija
        rechazada       // admin rechazó
    }

    public enum TipoVivienda { casa, apartamento }
    public enum Propiedad { propia, alquilada }
    public enum PermitenMascotas { si, no, no_aplica }
}
