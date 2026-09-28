package com.refugio.nueva_vida.proyecto_de_aula.scheduler;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import com.refugio.nueva_vida.proyecto_de_aula.service.BarraService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BarraScheduler {

    private final PerroRepository perroRepository;
    private final BarraService barraService;


    @Scheduled(cron = "0 0 * * * *")
    public void actualizarTodasLasBarras() {

        List<Perro> perros =
                perroRepository.findAll();

        for (Perro perro : perros) {

            // Cada barra usa su propia fecha de referencia
            // internamente (ver BarraService.aplicarDesgastePorTiempo),
            // así que aquí solo hace falta delegar, perro por perro.
            barraService.aplicarDesgastePorTiempo(perro);
        }

        log.info(
                "Actualización automática de barras ejecutada."
        );
    }
}
