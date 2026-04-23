package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "perro")
public class Perro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perro")
    private Integer idPerro;

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "edad", length = 30)
    private String edad;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", nullable = false)
    private Sexo sexo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "esterilizado", nullable = false)
    private Boolean esterilizado = false;

    @Column(name = "vacunado", nullable = false)
    private Boolean vacunado = false;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_salud", nullable = false)
    private NivelSalud nivelSalud;

    @Enumerated(EnumType.STRING)
    @Column(name = "sociabilidad", nullable = false)
    private Sociabilidad sociabilidad;

    @Column(name = "adoptado", nullable = false)
    private Boolean adoptado = false;

    @Column(name = "lista_para_adoptar", nullable = false)
    private Boolean listaParaAdoptar = false;

    @Column(name = "registro_medico", columnDefinition = "TEXT")
    private String registroMedico;

    @Column(name = "fecha_ingreso", nullable = false, updatable = false)
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    // ── Enums ────────────────────────────────────────────────────────────────

    public enum Sexo { Macho, Hembra }

    public enum Estado { RESCATADO, ABANDONADO, ACOGIDO }

    public enum NivelSalud { SANO, ENFERMO, CRITICO }

    public enum Sociabilidad { ALTA, MEDIA, BAJA }

    // ── Constructors ─────────────────────────────────────────────────────────

    public Perro() {}

    // ── Getters & Setters ────────────────────────────────────────────────────

    public Integer getIdPerro() { return idPerro; }
    public void setIdPerro(Integer idPerro) { this.idPerro = idPerro; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEdad() { return edad; }
    public void setEdad(String edad) { this.edad = edad; }

    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Boolean getEsterilizado() { return esterilizado; }
    public void setEsterilizado(Boolean esterilizado) { this.esterilizado = esterilizado; }

    public Boolean getVacunado() { return vacunado; }
    public void setVacunado(Boolean vacunado) { this.vacunado = vacunado; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public NivelSalud getNivelSalud() { return nivelSalud; }
    public void setNivelSalud(NivelSalud nivelSalud) { this.nivelSalud = nivelSalud; }

    public Sociabilidad getSociabilidad() { return sociabilidad; }
    public void setSociabilidad(Sociabilidad sociabilidad) { this.sociabilidad = sociabilidad; }

    public Boolean getAdoptado() { return adoptado; }
    public void setAdoptado(Boolean adoptado) { this.adoptado = adoptado; }

    public Boolean getListaParaAdoptar() { return listaParaAdoptar; }
    public void setListaParaAdoptar(Boolean listaParaAdoptar) { this.listaParaAdoptar = listaParaAdoptar; }

    public String getRegistroMedico() { return registroMedico; }
    public void setRegistroMedico(String registroMedico) { this.registroMedico = registroMedico; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }
}
