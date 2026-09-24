package com.refugio.tienda.tienda_service.controller;

import com.refugio.tienda.tienda_service.model.Pedido;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.repository.NotificacionTiendaRepository;
import com.refugio.tienda.tienda_service.service.CuentaService;
import com.refugio.tienda.tienda_service.service.PedidoService;
import com.refugio.tienda.tienda_service.service.ProductoService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class TiendaWebController {

    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private final NotificacionTiendaRepository notificacionRepository;
    private final CuentaService cuentaService;

    
    // CATÁLOGO
    
    @GetMapping("/tienda")
    public String catalogo(Model model) {
        model.addAttribute("productos", productoService.listarTodos());
        model.addAttribute("totalNoLeidas", notificacionRepository.countByLeidaFalse());

    
        model.addAttribute("cuentaTienda",  cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));
        model.addAttribute("cuentaRefugio", cuentaService.obtenerCuentaPorTipo(TipoCuenta.REFUGIO));

        return "tienda/catalogo";
    }

    
    // CONFIRMACIÓN DE PEDIDO


    @GetMapping("/tienda/confirmacion/{idPedido}")
    public String confirmacion(@PathVariable Integer idPedido,
                                Model model,
                                RedirectAttributes ra) {

        Pedido pedido = pedidoService.buscarPorId(idPedido).orElse(null);
        if (pedido == null) {
            ra.addFlashAttribute("errorMsg", "Pedido no encontrado.");
            return "redirect:/tienda";
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("totalNoLeidas", notificacionRepository.countByLeidaFalse());

      
        model.addAttribute("cuentaTienda",  cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));
        model.addAttribute("cuentaRefugio", cuentaService.obtenerCuentaPorTipo(TipoCuenta.REFUGIO));

        return "tienda/confirmacion";
    }
}