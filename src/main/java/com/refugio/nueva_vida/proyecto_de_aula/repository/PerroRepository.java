package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerroRepository extends JpaRepository<Perro, Integer> {

    // Buscar perros no adoptados (disponibles para adopción)
    List<Perro> findByAdoptadoFalse();

    // Buscar por estado
    List<Perro> findByEstado(Perro.Estado estado);

    // Buscar disponibles para adoptar
    List<Perro> findByListaParaAdoptarTrue();

    // Buscar por nombre (case-insensitive)
    List<Perro> findByNombreContainingIgnoreCase(String nombre);
}
