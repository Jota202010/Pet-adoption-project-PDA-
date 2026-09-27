package com.refugio.nueva_vida.proyecto_de_aula.config;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class BarrasConfig {

    @Value("${refugio.barras.nutricion.decaimiento-horas:12}")
    private long nutricionDecaimientoHoras;

    @Value("${refugio.barras.nutricion.decaimiento-porcentaje:2}")
    private int nutricionDecaimientoPorcentaje;


    @Value("${refugio.barras.hidratacion.decaimiento-horas:8}")
    private long hidratacionDecaimientoHoras;

    @Value("${refugio.barras.hidratacion.decaimiento-porcentaje:3}")
    private int hidratacionDecaimientoPorcentaje;


    @Value("${refugio.barras.salud.decaimiento-dias:7}")
    private long saludDecaimientoDias;

    @Value("${refugio.barras.salud.decaimiento-porcentaje:1}")
    private int saludDecaimientoPorcentaje;


    @Value("${refugio.barras.energia.decaimiento-horas:6}")
    private long energiaDecaimientoHoras;

    @Value("${refugio.barras.energia.decaimiento-porcentaje:4}")
    private int energiaDecaimientoPorcentaje;


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