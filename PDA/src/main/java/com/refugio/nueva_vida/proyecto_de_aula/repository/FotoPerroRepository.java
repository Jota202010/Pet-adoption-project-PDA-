package com.refugio.nueva_vida.proyecto_de_aula.repository;

import com.refugio.nueva_vida.proyecto_de_aula.model.FotoPerro;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FotoPerroRepository extends JpaRepository<FotoPerro, Integer> {
    List<FotoPerro> findByPerroOrderByOrdenAsc(Perro perro);
    Optional<FotoPerro> findByPerroAndEsPerfilTrue(Perro perro);
}
