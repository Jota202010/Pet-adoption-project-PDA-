package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Testimonio;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import com.refugio.nueva_vida.proyecto_de_aula.repository.TestimonioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class TestimonioService {

    private final TestimonioRepository repo;

    /**
     * Guarda la calificación del usuario. Si ya había calificado antes,
     * actualiza la que tenía en vez de crear una nueva.
     */
    public Testimonio calificar(Usuario usuario, Integer calificacion, String comentario) {

        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas.");
        }

        String texto = (comentario == null || comentario.isBlank()) ? null : comentario.trim();
        if (texto != null && texto.length() > 500) {
            texto = texto.substring(0, 500);
        }

        Testimonio testimonio = repo.findByUsuario(usuario).orElseGet(() -> new Testimonio());
        testimonio.setUsuario(usuario);
        testimonio.setCalificacion(calificacion);
        testimonio.setComentario(texto);
        testimonio.setFecha(LocalDateTime.now());

        return repo.save(testimonio);
    }

    @Transactional(readOnly = true)
    public Optional<Testimonio> testimonioDe(Usuario usuario) {
        return repo.findByUsuario(usuario);
    }

    /** Últimos testimonios publicados (los más recientes primero) */
    @Transactional(readOnly = true)
    public List<Testimonio> ultimos() {
        return repo.findTop10ByOrderByFechaDesc();
    }

    /** Promedio general redondeado a 1 decimal. 0.0 si todavía no hay ninguno */
    @Transactional(readOnly = true)
    public double promedio() {
        Double promedio = repo.promedioGeneral();
        if (promedio == null) return 0.0;
        return Math.round(promedio * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public long total() {
        return repo.count();
    }
}