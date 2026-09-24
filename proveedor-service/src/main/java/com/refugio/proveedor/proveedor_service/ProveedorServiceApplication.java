package com.refugio.proveedor.proveedor_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProveedorServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProveedorServiceApplication.class, args);
    }
}
