package com.refugio.tienda.tienda_service.repository;

import com.refugio.tienda.tienda_service.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findByCategoria(
            Producto.Categoria categoria
    );

    Optional<Producto>
    findFirstByNombreContainingIgnoreCaseAndCategoria(
            String nombre,
            Producto.Categoria categoria
    );

    Optional<Producto>
    findFirstByNombreIgnoreCase(
            String nombre
    );
}