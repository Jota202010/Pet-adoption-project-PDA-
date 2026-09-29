package com.refugio.tienda.tienda_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor
public class VentanaLiquidacionDTO {

    /** false si la restricción por fecha está apagada en la configuración. */
    private boolean restringida;

    /** true si HOY se puede liquidar. */
    private boolean habilitada;

    /** true si HOY ya se hizo una liquidación (no se permite otra el mismo día). */
    private boolean liquidadaHoy;

    /** Días que faltan para que abra (si está cerrada) o cierre (si está abierta). */
    private long dias;

    /** Inicio de la ventana, ej. "28 de septiembre". */
    private String desde;

    /** Fin de la ventana, ej. "5 de octubre". */
    private String hasta;

    /** Texto bajo el contador, ej. "días para abrir". */
    private String etiqueta;

    private String titulo;

    private String detalle;
}
