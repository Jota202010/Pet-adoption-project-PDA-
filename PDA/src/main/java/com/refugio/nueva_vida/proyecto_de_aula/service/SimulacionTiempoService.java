package com.refugio.nueva_vida.proyecto_de_aula.service;

import com.refugio.nueva_vida.proyecto_de_aula.model.Perro;
import com.refugio.nueva_vida.proyecto_de_aula.repository.PerroRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


        long horas =
                (long) dias * 24;


        barraService
                .aplicarDecaimientoPorTiempo(
                        perro,
                        horas
                );
    }


    @Transactional
    public void simularPasoParaTodos(
            int dias) {

        List<Perro> perros =
                perroRepository.findAll();


        long horas =
                (long) dias * 24;


        for (Perro perro : perros) {

            barraService
                    .aplicarDecaimientoPorTiempo(
                            perro,
                            horas
                    );
        }
    }
}