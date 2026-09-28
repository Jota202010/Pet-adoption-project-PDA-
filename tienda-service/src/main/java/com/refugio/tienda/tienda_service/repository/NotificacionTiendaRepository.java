package com.refugio.tienda.tienda_service.repository;

import com.refugio.tienda.tienda_service.model.NotificacionTienda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionTiendaRepository
        extends JpaRepository<NotificacionTienda, Integer> {

    List<NotificacionTienda> findByLeidaFalseOrderByFechaDesc();

    List<NotificacionTienda> findAllByOrderByFechaDesc();

    long countByLeidaFalse();

    // Notificaciones creadas a partir de una receta concreta de PDA
    List<NotificacionTienda> findByIdReceta(Long idReceta);

    // Respaldo para notificaciones antiguas (sin idReceta): se buscan por título
    List<NotificacionTienda> findByIdRecetaIsNullAndLeidaFalseAndTitulo(String titulo);
}