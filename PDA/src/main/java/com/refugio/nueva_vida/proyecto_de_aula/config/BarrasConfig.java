package com.refugio.nueva_vida.proyecto_de_aula.config;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BarrasConfig {

    @Value("${refugio.barras.nutricion.desgaste-horas:12}")
    private long nutricionDesgasteHoras;

    @Value("${refugio.barras.nutricion.desgaste-porcentaje:2}")
    private int nutricionDesgastePorcentaje;


    @Value("${refugio.barras.hidratacion.desgaste-horas:8}")
    private long hidratacionDesgasteHoras;

    @Value("${refugio.barras.hidratacion.desgaste-porcentaje:3}")
    private int hidratacionDesgastePorcentaje;


    @Value("${refugio.barras.salud.desgaste-dias:7}")
    private long saludDesgasteDias;

    @Value("${refugio.barras.salud.desgaste-porcentaje:1}")
    private int saludDesgastePorcentaje;


    @Value("${refugio.barras.energia.desgaste-horas:6}")
    private long energiaDesgasteHoras;

    @Value("${refugio.barras.energia.desgaste-porcentaje:4}")
    private int energiaDesgastePorcentaje;


    @Value("${refugio.recetas.umbral-nutricion:50}")
    private int umbralNutricion;

    @Value("${refugio.recetas.umbral-hidratacion:40}")
    private int umbralHidratacion;

    @Value("${refugio.recetas.umbral-salud:60}")
    private int umbralSalud;


    @Value("${refugio.demo.acelerar-tiempo:false}")
    private boolean acelerarTiempo;

    @Value("${refugio.demo.factor-aceleracion:100}")
    private int factorAceleracion;
}