package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.Testimonio;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestimonioRepository extends JpaRepository<Testimonio, Integer> {

    /** Un usuario tiene una sola calificación: si vuelve a calificar, se actualiza */
    Optional<Testimonio> findByUsuario(Usuario usuario);

    /** Los más recientes primero (para el listado público) */
    List<Testimonio> findTop10ByOrderByFechaDesc();

    /** Promedio general de estrellas. Devuelve null si aún no hay testimonios */
    @Query("SELECT AVG(t.calificacion) FROM Testimonio t")
    Double promedioGeneral();
}