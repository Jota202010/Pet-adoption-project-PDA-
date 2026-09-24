package com.refugio.tienda.tienda_service.dto;

import com.refugio.tienda.tienda_service.model.Cuenta;
import com.refugio.tienda.tienda_service.model.TipoCuenta;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CuentaDTO {

    private Integer idCuenta;
    private String titular;
    private BigDecimal saldo;
    private TipoCuenta tipo;

    public CuentaDTO(Cuenta cuenta) {
        this.idCuenta = cuenta.getIdCuenta();
        this.titular  = cuenta.getTitular();
        this.saldo    = cuenta.getSaldo();
        this.tipo     = cuenta.getTipo();
    }
}