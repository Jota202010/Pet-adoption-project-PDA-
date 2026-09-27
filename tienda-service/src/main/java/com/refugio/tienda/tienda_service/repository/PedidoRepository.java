package com.refugio.tienda.tienda_service.repository;

import com.refugio.tienda.tienda_service.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByFechaPedidoBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );
}