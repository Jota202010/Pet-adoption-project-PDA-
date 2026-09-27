package com.refugio.nueva_vida.proyecto_de_aula.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    /*
     * Puntaje de salud del perro.
     *
     * Rango permitido: 0 - 100
     *
     * Si es menor de 60:
     * el sistema puede generar una receta.
     */
    @Column(name = "puntaje_salud", nullable = false)
    private Integer puntajeSalud = 100;

    /*
     * Barras de vida — nutrición e hidratación (0 - 100).
     * La barra de "salud" reutiliza el campo puntajeSalud de arriba,
     * mediante los métodos getSalud()/setSalud() más abajo.
     */
    @Column(name = "nutricion", nullable = false)
    private Integer nutricion = 100;

    @Column(name = "hidratacion", nullable = false)
    private Integer hidratacion = 100;

    /*
     * Barra de energía (0 - 100). Sube al jugar con el perro
     * (interacción "Jugar" con un accesorio, ej. correa) y se
     * muestra junto a nutrición, hidratación y salud.
     */
    @Column(name = "energia", nullable = false)
    private Integer energia = 100;

    @Column(name = "ultimo_juego")
    private LocalDateTime ultimoJuego;

    @Column(name = "ultimo_bano")
    private LocalDateTime ultimoBano;

    @Column(name = "ultima_comida")
    private LocalDateTime ultimaComida;

    @Column(name = "ultima_hidratacion")
    private LocalDateTime ultimaHidratacion;

    @Column(name = "ultima_vacuna")
    private LocalDateTime ultimaVacuna;

    @Column(name = "ultima_desparasitacion")
    private LocalDateTime ultimaDesparasitacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "sociabilidad", nullable = false)
    private Sociabilidad sociabilidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_publicacion", nullable = false)
    private EstadoPublicacion estadoPublicacion = EstadoPublicacion.EN_REFUGIO;

    @Column(name = "registro_medico", columnDefinition = "TEXT")
    private String registroMedico;

    @Column(name = "fecha_ingreso", nullable = false, updatable = false)
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    /**
     * Alias de la barra de "salud" — reutiliza puntajeSalud
     * en vez de duplicar la columna en la base de datos.
     */
    public Integer getSalud() {
        return this.puntajeSalud;
    }

    public void setSalud(Integer salud) {
        this.puntajeSalud = salud;
    }

    public enum Sexo {
        Macho,
        Hembra
    }

    public enum Estado {
        RESCATADO,
        ABANDONADO,
        ACOGIDO
    }

    public enum NivelSalud {
        SANO,
        ENFERMO,
        CRITICO
    }

    public enum Sociabilidad {
        ALTA,
        MEDIA,
        BAJA
    }

    /**
     * Estado de publicación del animal:
     *
     * EN_REFUGIO  → llegó al refugio, aún no está listo para publicar
     * PUBLICADO   → aparece en el listado público
     * EN_PROCESO  → cita confirmada, esperando visita física
     * ADOPTADO    → adopción completada
     * DEVUELTO    → fue devuelto, necesita evaluación
     */
    @Getter
    @AllArgsConstructor
    public enum EstadoPublicacion {

        EN_REFUGIO("En el refugio (no publicado)"),

        PUBLICADO("Publicado - listo para adoptar"),

        EN_PROCESO("En proceso de adopcion"),

        ADOPTADO("Adoptado"),

        DEVUELTO("Devuelto - en evaluacion");

        private final String label;
    }
}