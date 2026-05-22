package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerroRepository extends JpaRepository<Perro, Integer> {

    // Animales visibles en el listado público (reemplaza findByAdoptadoFalseAndListaParaAdoptarTrue)
    List<Perro> findByEstadoPublicacion(Perro.EstadoPublicacion estadoPublicacion);

    // Buscar por estado de origen
    List<Perro> findByEstado(Perro.Estado estado);

    // Buscar por nombre (case-insensitive)
    List<Perro> findByNombreContainingIgnoreCase(String nombre);
}
