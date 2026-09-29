package com.refugio.tienda.tienda_service.repository;

import com.refugio.tienda.tienda_service.model.Producto;
import com.refugio.tienda.tienda_service.model.StockGratisUsado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockGratisUsadoRepository
        extends JpaRepository<StockGratisUsado, Integer> {

    /** true si esa categoría ya gastó su stock inicial gratis. */
    boolean existsByCategoria(Producto.Categoria categoria);
}