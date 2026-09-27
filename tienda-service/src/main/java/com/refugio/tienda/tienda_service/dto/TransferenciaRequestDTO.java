package com.refugio.tienda.tienda_service.dto;

import com.refugio.tienda.tienda_service.model.MetodoPago;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferenciaRequestDTO {

    private MetodoPago metodoPago;
    private String referenciaComprobante;
}