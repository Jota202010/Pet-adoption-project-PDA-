package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.EstadoReceta;
import com.refugio.nueva_vida.proyecto_de_aula.model.Receta;
import com.refugio.nueva_vida.proyecto_de_aula.model.TipoNecesidad;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecetaRepository
        extends JpaRepository<Receta, Long> {

    List<Receta>
    findByEstado(EstadoReceta estado);

    List<Receta>
    findByPerroIdPerro(Integer idPerro);

    boolean existsByPerroIdPerroAndTipoNecesidadAndEstado(
            Integer idPerro,
            TipoNecesidad tipoNecesidad,
            EstadoReceta estado
    );
}