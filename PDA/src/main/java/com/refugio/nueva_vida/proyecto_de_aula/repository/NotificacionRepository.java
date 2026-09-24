package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.Notificacion;
import com.refugio.nueva_vida.proyecto_de_aula.model.Receta;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    List<Notificacion>
    findByLeidaFalseOrderByFechaDesc();

    long countByLeidaFalse();

    // Notificaciones ligadas a un conjunto de recetas (para poder borrarlas
    // antes de borrar esas recetas, ya que Notificacion.id_receta es FK)
    List<Notificacion> findByRecetaIn(List<Receta> recetas);
}