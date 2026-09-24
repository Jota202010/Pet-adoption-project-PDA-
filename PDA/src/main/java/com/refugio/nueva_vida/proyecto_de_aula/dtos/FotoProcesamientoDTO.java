package com.refugio.nueva_vida.proyecto_de_aula.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FotoProcesamientoDTO implements Serializable {

    private Integer idFoto;
    private String nombreArchivo; // ej: perro_1_b7d0a317-....jpg
}