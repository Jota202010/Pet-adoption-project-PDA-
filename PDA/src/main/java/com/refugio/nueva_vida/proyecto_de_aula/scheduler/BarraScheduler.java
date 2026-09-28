package com.refugio.nueva_vida.proyecto_de_aula.scheduler;

import com.refugio.nueva_vida.proyecto_de_aula.config.BarrasConfig;
import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;
import com.refugio.nueva_vida.proyecto_de_aula.service.BarraService;
import com.refugio.nueva_vida.proyecto_de_aula.service.RecetaService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BarraScheduler {

    private final PerroRepository perroRepository;
    private final BarraService barraService;
    private final RecetaService recetaService;
    private final BarrasConfig config;


    @Scheduled(cron = "0 0 * * * *")
    public void actualizarTodasLasBarras() {

        List<Perro> perros =
                perroRepository.findAll();


        LocalDateTime ahora =
                LocalDateTime.now();


        for (Perro perro : perros) {

            long horasNutricion =
                    calcularHoras(
                            perro.getUltimaComida(),
                            ahora
                    );


            long horasHidratacion =
                    calcularHoras(
                            perro.getUltimaHidratacion(),
                            ahora
                    );


            long horasSalud =
                    calcularHoras(
                            perro.getUltimaVacuna(),
                            ahora
                    );


            long horasMaximas =
                    Math.max(
                            horasNutricion,
                            Math.max(
                                    horasHidratacion,
                                    horasSalud
                            )
                    );


            if (horasMaximas > 0) {

                // Modo demo: si está activo, el tiempo transcurrido se
                // multiplica por el factor de aceleración, así el
                // decaimiento de las barras se simula mucho más rápido
                // (útil para pruebas/demostraciones sin esperar horas reales).
                long horasEfectivas =
                        config.isAcelerarTiempo()
                                ? horasMaximas * config.getFactorAceleracion()
                                : horasMaximas;

                barraService
                        .aplicarDecaimientoPorTiempo(
                                perro,
                                horasEfectivas
                        );
            }


            recetaService
                    .generarRecetaSiNecesario(
                            perro
                    );
        }


        log.info(
                "Actualización automática de barras ejecutada."
        );
    }


    private long calcularHoras(
            LocalDateTime inicio,
            LocalDateTime fin) {

        if (inicio == null) {

            return 0;
        }


        return ChronoUnit.HOURS.between(
                inicio,
                fin
        );
    }
}
