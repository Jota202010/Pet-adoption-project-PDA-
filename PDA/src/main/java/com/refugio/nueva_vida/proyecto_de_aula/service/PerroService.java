package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.exception.PerroConCitasException;
import com.refugio.nueva_vida.proyecto_de_aula.model.Cita;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.CitaRepository;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor 
public class PerroService {

    private final PerroRepository perroRepository;
    private final CitaRepository citaRepository;
    private final FotoPerroService fotoPerroService;
    private final HistorialEstadoService historialEstadoService;
    private final BarraService barraService;
    private final RecetaService recetaService;

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

    /** Lista solo los animales con estadoPublicacion = PUBLICADO (visibles al público) */
    @Transactional(readOnly = true)
    public List<Perro> listarDisponibles() {
        return perroRepository.findByEstadoPublicacion(Perro.EstadoPublicacion.PUBLICADO);
    }

    /** Guarda un perro nuevo o actualiza uno existente */
    public Perro guardar(Perro perro) {
        return perroRepository.save(perro);
    }

    
    public void eliminar(Integer id) {
        Perro perro = perroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El perro no existe o ya fue eliminado."));

        List<Cita> citas = citaRepository.findByPerro(perro);
        if (!citas.isEmpty()) {
            throw new PerroConCitasException(perro.getNombre());
        }

        fotoPerroService.eliminarFotosDe(perro);
        historialEstadoService.eliminarPorPerro(perro);
        barraService.eliminarEventos(id);
        recetaService.eliminarPorPerro(id);

        perroRepository.delete(perro);
    }

    /** Total de perros en el refugio */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return perroRepository.count();
    }
}
