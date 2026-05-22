package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.HistorialEstado;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Integer> {

    // Todos los cambios de un perro, del más reciente al más antiguo
    List<HistorialEstado> findByPerroOrderByFechaCambioDesc(Perro perro);
}
