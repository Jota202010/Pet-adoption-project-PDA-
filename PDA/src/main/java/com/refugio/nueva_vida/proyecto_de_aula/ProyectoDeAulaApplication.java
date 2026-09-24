package com.refugio.nueva_vida.proyecto_de_aula;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProyectoDeAulaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoDeAulaApplication.class, args);
	}

}
