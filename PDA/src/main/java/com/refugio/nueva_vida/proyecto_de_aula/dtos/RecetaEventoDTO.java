package com.refugio.nueva_vida.proyecto_de_aula.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecetaEventoDTO {

    private Long idReceta;

    private Integer idPerro;

    private String nombrePerro;

    private String tipoNecesidad;

    private String descripcion;

    private String productosSugeridos;

    private String prioridad;
}