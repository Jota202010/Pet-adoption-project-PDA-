package com.refugio.tienda.tienda_service.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SaldoInsuficienteException.class)
    public String manejarSaldoInsuficiente(SaldoInsuficienteException e, Model model) {
        model.addAttribute("mensajeError", e.getMessage());
        return "error/saldo-insuficiente";
    }
}