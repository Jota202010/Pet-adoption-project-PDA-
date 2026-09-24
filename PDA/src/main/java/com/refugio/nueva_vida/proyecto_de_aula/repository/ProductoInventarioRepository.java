package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.ProductoInventario;
import com.refugio.nueva_vida.proyecto_de_aula.model.TipoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoInventarioRepository extends JpaRepository<ProductoInventario, Integer> {

    List<ProductoInventario> findAllByOrderByTipoAscNombreAsc();

    Optional<ProductoInventario> findByNombreIgnoreCaseAndTipo(String nombre, TipoProducto tipo);
}
