package com.refugio.tienda.tienda_service.controller;

import com.refugio.tienda.tienda_service.dto.TransferenciaRequestDTO;
import com.refugio.tienda.tienda_service.exception.ComprobanteInvalidoException;
import com.refugio.tienda.tienda_service.exception.FueraDeVentanaException;
import com.refugio.tienda.tienda_service.exception.SaldoInsuficienteException;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.model.Transferencia;
import com.refugio.tienda.tienda_service.service.CuentaService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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

        model.addAttribute(
                "comision",
                cuentaService.obtenerResumenComision()
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

            Transferencia t = cuentaService.registrarIngresoRefugio(
                    dto.getMetodoPago(),
                    dto.getReferenciaComprobante()
            );

            ra.addFlashAttribute(
                    "mensajeExito",
                    "Ingreso registrado correctamente. " +
                    "Ventas: $" + pesos(t.getMontoBruto()) +
                    " · Comisión (" + t.getPorcentajeComision().stripTrailingZeros().toPlainString() +
                    "%): $" + pesos(t.getMontoComision()) +
                    " · Neto transferido al Refugio: $" + pesos(t.getMonto())
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

        } catch (FueraDeVentanaException e) {

            ra.addFlashAttribute(
                    "mensajeError",
                    "📅 " + e.getMessage()
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


    @PostMapping("/historial/borrar")
    public String borrarHistorial(RedirectAttributes ra) {

        int cantidad = cuentaService.borrarHistorial();

        ra.addFlashAttribute(
                "mensajeExito",
                "Historial borrado (" + cantidad + " registros). " +
                "Los saldos no se modificaron."
        );

        return "redirect:/admin/cuenta";
    }


    /**
     * Da formato de pesos colombianos: 67200.00 -> 67.200
     */
    private static String pesos(BigDecimal valor) {

        DecimalFormatSymbols simbolos =
                new DecimalFormatSymbols(Locale.US);
        simbolos.setGroupingSeparator('.');

        return new DecimalFormat("#,##0", simbolos).format(valor);
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

        model.addAttribute(
                "ventana",
                cuentaService.obtenerVentanaLiquidacion()
        );

        model.addAttribute("dto", dto);
    }
}