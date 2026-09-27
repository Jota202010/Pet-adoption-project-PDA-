package com.refugio.nueva_vida.proyecto_de_aula.controller;

import com.refugio.nueva_vida.proyecto_de_aula.model.SeguimientoEnvio;
import com.refugio.nueva_vida.proyecto_de_aula.repository.SeguimientoEnvioRepository;
import com.refugio.nueva_vida.proyecto_de_aula.service.PaqueteRecepcionService;
import com.refugio.nueva_vida.proyecto_de_aula.service.RecetaService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PedidoController {

    private final SeguimientoEnvioRepository seguimientoRepository;

    private final PaqueteRecepcionService paqueteRecepcionService;

    private final RecetaService recetaService;


    @GetMapping("/admin/pedidos")
    public String verPedidos(Model model) {

        List<SeguimientoEnvio> seguimientos =
                seguimientoRepository
                        .findAllByOrderByFechaDesc();

        model.addAttribute(
                "seguimientos",
                seguimientos
        );

        model.addAttribute(
                "paquetes",
                paqueteRecepcionService.listarPaquetes()
        );

        model.addAttribute(
                "totalNoLeidas",
                recetaService.contarNoLeidas()
        );

        return "privilegiado/pedidos-adminview";
    }


    @PostMapping("/admin/paquetes/{id}/abrir")
    public String abrirPaquete(
            @PathVariable Integer id,
            RedirectAttributes ra) {

        try {

            paqueteRecepcionService.abrirPaquete(id);

            ra.addFlashAttribute(
                    "mensajeExito",
                    "📦 Paquete abierto. Ahora puedes revisar su contenido."
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "errorMsg",
                    "❌ " + e.getMessage()
            );
        }

        return "redirect:/admin/pedidos";
    }


    @PostMapping("/admin/paquetes/{id}/inventario")
    public String enviarAlInventario(
            @PathVariable Integer id,
            RedirectAttributes ra) {

        try {

            paqueteRecepcionService.enviarAlInventario(id);

            ra.addFlashAttribute(
                    "mensajeExito",
                    "✅ Todos los productos del paquete fueron enviados al inventario."
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "errorMsg",
                    "❌ " + e.getMessage()
            );
        }

        return "redirect:/admin/pedidos";
    }


    @PostMapping("/admin/pedidos/eliminar")
    public String eliminarHistorialPedidos(
            RedirectAttributes ra) {

        try {

            seguimientoRepository.deleteAll();

            ra.addFlashAttribute(
                    "mensajeExito",
                    "Historial de pedidos recibidos eliminado."
            );

        } catch (Exception e) {

            ra.addFlashAttribute(
                    "errorMsg",
                    "No se pudo eliminar el historial: "
                            + e.getMessage()
            );
        }

        return "redirect:/admin/pedidos";
    }
}