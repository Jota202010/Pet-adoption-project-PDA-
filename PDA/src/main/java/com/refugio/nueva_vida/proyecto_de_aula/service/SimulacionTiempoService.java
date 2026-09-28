package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulacionTiempoService {

    private final PerroRepository perroRepository;

    private final BarraService barraService;


    @Transactional
    public void simularPaso(
            Integer idPerro,
            int dias) {

        Perro perro =
                perroRepository
                        .findById(idPerro)
                        .orElseThrow();

        retrocederReferencias(perro, dias);

        barraService
                .aplicarDesgastePorTiempo(perro);
    }


    @Transactional
    public void simularPasoParaTodos(
            int dias) {

        List<Perro> perros =
                perroRepository.findAll();

        for (Perro perro : perros) {

            retrocederReferencias(perro, dias);

            barraService
                    .aplicarDesgastePorTiempo(perro);
        }
    }

    /**
     * Adelanta artificialmente el "reloj" del perro retrocediendo sus
     * fechas de referencia (última comida, hidratación, vacuna, juego)
     * la cantidad de días indicada, para que
     * BarraService.aplicarDesgastePorTiempo calcule y aplique el
     * desgaste correspondiente como si ese tiempo hubiera pasado
     * de verdad. Si el perro nunca tuvo esa acción registrada (campo
     * en null), se toma "ahora" como punto de partida para que la
     * simulación funcione igual con perros recién creados.
     */
    private void retrocederReferencias(Perro perro, int dias) {
        long horas = (long) dias * 24;
        LocalDateTime ahora = LocalDateTime.now();

        LocalDateTime comida = perro.getUltimaComida() != null ? perro.getUltimaComida() : ahora;
        perro.setUltimaComida(comida.minusHours(horas));

        LocalDateTime hidratacion = perro.getUltimaHidratacion() != null ? perro.getUltimaHidratacion() : ahora;
        perro.setUltimaHidratacion(hidratacion.minusHours(horas));

        LocalDateTime vacuna = perro.getUltimaVacuna() != null ? perro.getUltimaVacuna() : ahora;
        perro.setUltimaVacuna(vacuna.minusHours(horas));

        LocalDateTime juego = perro.getUltimoJuego() != null ? perro.getUltimoJuego() : ahora;
        perro.setUltimoJuego(juego.minusHours(horas));
    }
}
