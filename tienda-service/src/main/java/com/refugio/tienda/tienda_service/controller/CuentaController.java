package com.refugio.tienda.tienda_service.controller;

import com.refugio.tienda.tienda_service.dto.TransferenciaRequestDTO;
import com.refugio.tienda.tienda_service.exception.SaldoInsuficienteException;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.service.CuentaService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/cuenta")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping
    public String verCuenta(Model model) {
        model.addAttribute("cuentaRefugio", cuentaService.obtenerCuentaPorTipo(TipoCuenta.REFUGIO));
        model.addAttribute("cuentaTienda",  cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));
        model.addAttribute("transferencias", cuentaService.listarTransferencias());
        model.addAttribute("alertaSaldoBajo", cuentaService.saldoBajo());
        return "admin/cuenta";
    }

    @GetMapping("/transferir")
    public String formularioTransferir(Model model) {
        model.addAttribute("cuentaRefugio", cuentaService.obtenerCuentaPorTipo(TipoCuenta.REFUGIO));
        model.addAttribute("cuentaTienda",  cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));
        model.addAttribute("dto", new TransferenciaRequestDTO());
        return "admin/transferir";
    }

    @PostMapping("/transferir")
    public String procesarTransferencia(
            @ModelAttribute TransferenciaRequestDTO dto,
            RedirectAttributes ra) {

        try {
            cuentaService.transferir(
                    TipoCuenta.REFUGIO,
                    TipoCuenta.TIENDA,
                    dto.getMonto(),
                    (dto.getDescripcion() == null || dto.getDescripcion().isBlank())
                            ? "Transferencia manual"
                            : dto.getDescripcion()
            );
            ra.addFlashAttribute("mensajeExito",
                "✅ Transferencia de $" + dto.getMonto() + " realizada correctamente.");
        } catch (SaldoInsuficienteException e) {
            ra.addFlashAttribute("mensajeError", "⚠️ " + e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("mensajeError", "❌ Error: " + e.getMessage());
        }
        return "redirect:/admin/cuenta";
    }
}