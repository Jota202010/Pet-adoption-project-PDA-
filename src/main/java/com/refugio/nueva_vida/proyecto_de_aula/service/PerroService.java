package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PerroService {

    private final PerroRepository perroRepository;

    public PerroService(PerroRepository perroRepository) {
        this.perroRepository = perroRepository;
    }

    /** Retorna todos los perros registrados */
    @Transactional(readOnly = true)
    public List<Perro> listarTodos() {
        return perroRepository.findAll();
    }

    /** Retorna un perro por ID, o vacío si no existe */
    @Transactional(readOnly = true)
    public Optional<Perro> buscarPorId(Integer id) {
        return perroRepository.findById(id);
    }

    /** Lista los perros que aún no han sido adoptados */
    @Transactional(readOnly = true)
    public List<Perro> listarDisponibles() {
        return perroRepository.findByAdoptadoFalse();
    }

    /** Guarda un perro nuevo o actualiza uno existente */
    public Perro guardar(Perro perro) {
        return perroRepository.save(perro);
    }

    /** Elimina un perro por ID */
    public void eliminar(Integer id) {
        perroRepository.deleteById(id);
    }

    /** Total de perros en el refugio */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return perroRepository.count();
    }
}
