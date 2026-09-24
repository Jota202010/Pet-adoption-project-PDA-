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
}