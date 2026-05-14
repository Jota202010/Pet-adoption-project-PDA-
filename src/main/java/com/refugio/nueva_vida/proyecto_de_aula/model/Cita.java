package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    public Cita() {}

    // ── Getters & Setters ────────────────────────────────────────────────────
    public Integer getIdCita() { return idCita; }
    public void setIdCita(Integer idCita) { this.idCita = idCita; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Perro getPerro() { return perro; }
    public void setPerro(Perro perro) { this.perro = perro; }
    public Usuario getAdmin() { return admin; }
    public void setAdmin(Usuario admin) { this.admin = admin; }
    public EstadoCita getEstado() { return estado; }
    public void setEstado(EstadoCita estado) { this.estado = estado; }
    public LocalDate getFechaCita() { return fechaCita; }
    public void setFechaCita(LocalDate fechaCita) { this.fechaCita = fechaCita; }
    public LocalTime getHoraCita() { return horaCita; }
    public void setHoraCita(LocalTime horaCita) { this.horaCita = horaCita; }
    public String getSede() { return sede; }
    public void setSede(String sede) { this.sede = sede; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }
    public LocalDateTime getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(LocalDateTime fechaDecision) { this.fechaDecision = fechaDecision; }
    public HorarioDisponible getHorario() { return horario; }
    public void setHorario(HorarioDisponible horario) { this.horario = horario; }
    public TipoVivienda getTipoVivienda() { return tipoVivienda; }
    public void setTipoVivienda(TipoVivienda tipoVivienda) { this.tipoVivienda = tipoVivienda; }
    public Propiedad getPropiedad() { return propiedad; }
    public void setPropiedad(Propiedad propiedad) { this.propiedad = propiedad; }
    public PermitenMascotas getPermitenMascotas() { return permitenMascotas; }
    public void setPermitenMascotas(PermitenMascotas permitenMascotas) { this.permitenMascotas = permitenMascotas; }
    public Integer getNumPersonas() { return numPersonas; }
    public void setNumPersonas(Integer numPersonas) { this.numPersonas = numPersonas; }
    public Boolean getTodosAcuerdo() { return todosAcuerdo; }
    public void setTodosAcuerdo(Boolean todosAcuerdo) { this.todosAcuerdo = todosAcuerdo; }
    public Boolean getPerrosAntes() { return perrosAntes; }
    public void setPerrosAntes(Boolean perrosAntes) { this.perrosAntes = perrosAntes; }
    public Boolean getMascotasActual() { return mascotasActual; }
    public void setMascotasActual(Boolean mascotasActual) { this.mascotasActual = mascotasActual; }
    public String getMascotasAnteriores() { return mascotasAnteriores; }
    public void setMascotasAnteriores(String mascotasAnteriores) { this.mascotasAnteriores = mascotasAnteriores; }
    public String getHorasSolo() { return horasSolo; }
    public void setHorasSolo(String horasSolo) { this.horasSolo = horasSolo; }
    public Boolean getPuedePasear() { return puedePasear; }
    public void setPuedePasear(Boolean puedePasear) { this.puedePasear = puedePasear; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public Boolean getCubreVet() { return cubreVet; }
    public void setCubreVet(Boolean cubreVet) { this.cubreVet = cubreVet; }
    public Boolean getCubreEmergencias() { return cubreEmergencias; }
    public void setCubreEmergencias(Boolean cubreEmergencias) { this.cubreEmergencias = cubreEmergencias; }
    public String getMotivacion() { return motivacion; }
    public void setMotivacion(String motivacion) { this.motivacion = motivacion; }
    public String getTipoPerroBuscado() { return tipoPerroBuscado; }
    public void setTipoPerroBuscado(String tipoPerroBuscado) { this.tipoPerroBuscado = tipoPerroBuscado; }
    public Boolean getCondNoAbandono() { return condNoAbandono; }
    public void setCondNoAbandono(Boolean condNoAbandono) { this.condNoAbandono = condNoAbandono; }
    public Boolean getCondSeguimiento() { return condSeguimiento; }
    public void setCondSeguimiento(Boolean condSeguimiento) { this.condSeguimiento = condSeguimiento; }
    public Boolean getCondEvaluacion() { return condEvaluacion; }
    public void setCondEvaluacion(Boolean condEvaluacion) { this.condEvaluacion = condEvaluacion; }
}
