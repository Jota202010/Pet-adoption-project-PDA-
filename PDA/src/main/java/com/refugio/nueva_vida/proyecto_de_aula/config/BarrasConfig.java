package com.refugio.nueva_vida.proyecto_de_aula.config;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Valores configurables de las barras de vida y las recetas.
 *
 * Los tiempos se escriben en application.properties con sufijo:
 *   30m = 30 minutos   |   12h = 12 horas   |   7d = 7 días
 */
@Configuration
@Getter
public class BarrasConfig {

    // ---- Cada cuánto baja cada barra y cuánto baja ----

    @Value("${refugio.barras.nutricion.desgaste:12h}")
    private Duration nutricionDesgaste;

    @Value("${refugio.barras.nutricion.desgaste-porcentaje:2}")
    private int nutricionDesgastePorcentaje;


    @Value("${refugio.barras.hidratacion.desgaste:8h}")
    private Duration hidratacionDesgaste;

    @Value("${refugio.barras.hidratacion.desgaste-porcentaje:3}")
    private int hidratacionDesgastePorcentaje;


    @Value("${refugio.barras.salud.desgaste:7d}")
    private Duration saludDesgaste;

    @Value("${refugio.barras.salud.desgaste-porcentaje:1}")
    private int saludDesgastePorcentaje;


    @Value("${refugio.barras.energia.desgaste:6h}")
    private Duration energiaDesgaste;

    @Value("${refugio.barras.energia.desgaste-porcentaje:4}")
    private int energiaDesgastePorcentaje;


    // ---- Umbrales para generar recetas ----

    @Value("${refugio.recetas.umbral-nutricion:50}")
    private int umbralNutricion;

    @Value("${refugio.recetas.umbral-hidratacion:40}")
    private int umbralHidratacion;

    @Value("${refugio.recetas.umbral-salud:60}")
    private int umbralSalud;
}