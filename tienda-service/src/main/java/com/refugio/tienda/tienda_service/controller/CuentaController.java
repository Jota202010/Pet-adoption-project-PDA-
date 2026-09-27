package com.refugio.tienda.tienda_service.controller;

import com.refugio.tienda.tienda_service.dto.TransferenciaRequestDTO;
import com.refugio.tienda.tienda_service.exception.ComprobanteInvalidoException;
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

        model.addAttribute(
                "cuentaRefugio",
                cuentaService.obtenerCuentaPorTipo(
                        TipoCuenta.REFUGIO
                )
        );

        model.addAttribute(
                "cuentaTienda",
                cuentaService.obtenerCuentaPorTipo(
                        TipoCuenta.TIENDA
                )
        );

        model.addAttribute(
                "transferencias",
                cuentaService.listarTransferencias()
        );

        model.addAttribute(
                "alertaSaldoBajo",
                cuentaService.saldoBajo()
        );

        return "admin/cuenta";
    }


    @GetMapping("/transferir")
    public String formularioTransferir(Model model) {

        cargarDatosFormulario(model, new TransferenciaRequestDTO());

        return "admin/transferir";
    }


    /**
     * Genera un comprobante nuevo y vuelve a mostrar el MISMO
     * formulario (la misma página HTML) ya con el campo
     * completado. No es un endpoint REST ni devuelve JSON:
     * es un Controller normal que renderiza de nuevo la
     * plantilla "admin/transferir", como cualquier otra
     * pantalla del sitio. El usuario nunca ve una respuesta
     * "cruda"; solo ve la página recargada con el comprobante
     * ya escrito en el campo.
     */
    @PostMapping("/transferir/generar-comprobante")
    public String generarComprobante(
            @ModelAttribute TransferenciaRequestDTO dto,
            Model model) {

        dto.setReferenciaComprobante(
                cuentaService.generarComprobante()
        );

        cargarDatosFormulario(model, dto);

        model.addAttribute("comprobanteGenerado", true);

        return "admin/transferir";
    }


    @PostMapping("/transferir")
    public String registrarIngreso(
            @ModelAttribute TransferenciaRequestDTO dto,
            RedirectAttributes ra) {

        try {

            cuentaService.registrarIngresoRefugio(
                    dto.getMetodoPago(),
                    dto.getReferenciaComprobante()
            );

            ra.addFlashAttribute(
                    "mensajeExito",
                    "Ingreso registrado correctamente. " +
                    "El saldo del Refugio aumentó y el saldo de la Tienda disminuyó."
            );

            return "redirect:/admin/cuenta";

        } catch (ComprobanteInvalidoException e) {

            // El comprobante no existe, está incompleto o fue
            // alterado: se devuelve al formulario de transferencia
            // para que el usuario lo corrija o genere uno nuevo.
            ra.addFlashAttribute(
                    "mensajeError",
                    "🧾 " + e.getMessage()
            );

            return "redirect:/admin/cuenta/transferir";

        } catch (SaldoInsuficienteException e) {

            ra.addFlashAttribute(
                    "mensajeError",
                    "⚠️ " + e.getMessage()
            );

            return "redirect:/admin/cuenta";

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "mensajeError",
                    "❌ " + e.getMessage()
            );

            return "redirect:/admin/cuenta";
        }
    }


    /**
     * Evita repetir siempre los mismos atributos que
     * necesita la pantalla "admin/transferir".
     */
    private void cargarDatosFormulario(
            Model model,
            TransferenciaRequestDTO dto) {

        model.addAttribute(
                "cuentaRefugio",
                cuentaService.obtenerCuentaPorTipo(
                        TipoCuenta.REFUGIO
                )
        );

        model.addAttribute(
                "cuentaTienda",
                cuentaService.obtenerCuentaPorTipo(
                        TipoCuenta.TIENDA
                )
        );

        model.addAttribute(
                "liquidacion",
                cuentaService.obtenerLiquidacionActual()
        );

        model.addAttribute("dto", dto);
    }
}
