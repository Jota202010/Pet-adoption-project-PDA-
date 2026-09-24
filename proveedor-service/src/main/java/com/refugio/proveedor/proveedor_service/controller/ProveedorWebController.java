package com.refugio.proveedor.proveedor_service.controller;

import com.refugio.proveedor.proveedor_service.model.Envio;
import com.refugio.proveedor.proveedor_service.service.EnvioService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/proveedor")
@RequiredArgsConstructor
public class ProveedorWebController {

    private final EnvioService envioService;



    @GetMapping
    public String inicio() {

        return "proveedor/inicio";
    }


   
    @GetMapping("/admin")
    public String panelAdmin(Model model) {

        model.addAttribute(
                "envios",
                envioService.listarTodos()
        );

        return "proveedor/admin";
    }


    @GetMapping("/seguimiento/{idPedido}")
    public String seguimiento(
            @PathVariable Integer idPedido,
            Model model
    ) {

        Envio envio =
                envioService.buscarPorPedido(idPedido);

        if (envio == null) {

            model.addAttribute(
                    "error",
                    "No existe un envío para el pedido #" + idPedido
            );

            return "proveedor/seguimiento";
        }

        model.addAttribute("envio", envio);

        return "proveedor/seguimiento";
    }
}