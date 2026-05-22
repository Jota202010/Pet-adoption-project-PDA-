package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.HistorialEstado;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.HistorialEstadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HistorialEstadoService {

    private final HistorialEstadoRepository repo;

    public HistorialEstadoService(HistorialEstadoRepository repo) {
        this.repo = repo;
    }

    /**
     * Registra un cambio de estado solo si el estado realmente cambió.
     * Si estadoAnterior == estadoNuevo, no hace nada.
     */
    public void registrar(Perro perro,
                          Perro.EstadoPublicacion estadoAnterior,
                          Perro.EstadoPublicacion estadoNuevo,
                          HistorialEstado.Origen origen) {

        if (estadoAnterior == estadoNuevo) return; // sin cambio real, no registrar

        HistorialEstado registro = new HistorialEstado(perro, estadoAnterior, estadoNuevo, origen);
        repo.save(registro);
    }

    /** Lista el historial de un perro del más reciente al más antiguo */
    @Transactional(readOnly = true)
    public List<HistorialEstado> historialDePerro(Perro perro) {
        return repo.findByPerroOrderByFechaCambioDesc(perro);
    }
}
