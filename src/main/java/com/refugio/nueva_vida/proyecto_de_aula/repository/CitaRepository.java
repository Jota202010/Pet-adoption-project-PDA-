package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {
    List<Cita> findByUsuario(Usuario usuario);
    List<Cita> findByPerro(Perro perro);
    List<Cita> findByEstado(Cita.EstadoCita estado);
    long countByEstado(Cita.EstadoCita estado);
}
