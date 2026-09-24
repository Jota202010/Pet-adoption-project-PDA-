package com.refugio.tienda.tienda_service.cart;

import com.refugio.tienda.tienda_service.exception.SaldoInsuficienteException;
import com.refugio.tienda.tienda_service.model.Pedido;
import com.refugio.tienda.tienda_service.model.Producto;
import com.refugio.tienda.tienda_service.model.TipoCuenta;
import com.refugio.tienda.tienda_service.service.CuentaService;
import com.refugio.tienda.tienda_service.service.PedidoService;
import com.refugio.tienda.tienda_service.service.ProductoService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/tienda/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private static final String SESION_CARRITO = "carrito";

    private final ProductoService productoService;
    private final PedidoService pedidoService;
    private final CuentaService cuentaService;

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> obtenerCarrito(HttpSession session) {
        Map<Integer, Integer> carrito = (Map<Integer, Integer>) session.getAttribute(SESION_CARRITO);
        if (carrito == null) {
            carrito = new LinkedHashMap<>();
            session.setAttribute(SESION_CARRITO, carrito);
        }
        return carrito;
    }


    // AGREGAR AL CARRITO (con validación real de stock)


    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer idProducto,
                          @RequestParam(defaultValue = "1") Integer cantidad,
                          HttpSession session,
                          RedirectAttributes ra) {

        if (cantidad == null || cantidad < 1) {
            ra.addFlashAttribute("errorMsg", "🚫 La cantidad debe ser de al menos 1 unidad.");
            return "redirect:/tienda";
        }

        Producto producto = productoService.buscarPorId(idProducto).orElse(null);
        if (producto == null) {
            ra.addFlashAttribute("errorMsg", "🚫 Ese producto ya no está disponible.");
            return "redirect:/tienda";
        }

        Map<Integer, Integer> carrito = obtenerCarrito(session);
        int yaEnCarrito = carrito.getOrDefault(idProducto, 0);
        int totalDeseado = yaEnCarrito + cantidad;

        // 🔒 Validación real: no se puede llevar más de lo que hay en stock,
        // sumando lo que ya tenía en el carrito + lo que está agregando ahora.
        if (totalDeseado > producto.getStock()) {
            int disponibleReal = producto.getStock() - yaEnCarrito;

            String detalle = disponibleReal > 0
                    ? "Solo puedes agregar " + disponibleReal + " unidad(es) más."
                    : "Ya tienes en el carrito todo el stock disponible de este producto.";

            ra.addFlashAttribute("errorMsg",
                    "📦 Stock insuficiente de \"" + producto.getNombre() + "\": quedan " +
                    producto.getStock() + " unidad(es) y estás pidiendo " + totalDeseado + ". " + detalle);
            return "redirect:/tienda";
        }

        carrito.merge(idProducto, cantidad, Integer::sum);
        ra.addFlashAttribute("mensajeExito",
                cantidad + " x \"" + producto.getNombre() + "\" agregado(s) al carrito.");
        return "redirect:/tienda";
    }


    // VER CARRITO


    @GetMapping
    public String verCarrito(HttpSession session, Model model) {
        Map<Integer, Integer> carrito = obtenerCarrito(session);

        var items = new java.util.ArrayList<Map<String, Object>>();
        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entrada : carrito.entrySet()) {
            Producto producto = productoService.buscarPorId(entrada.getKey()).orElse(null);
            if (producto == null) continue;

            int cantidad = entrada.getValue();
            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
            total = total.add(subtotal);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("producto", producto);
            item.put("cantidad", cantidad);
            item.put("subtotal", subtotal);
            items.add(item);
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);

        // 🆕 Saldos para la vista
        model.addAttribute("cuentaRefugio", cuentaService.obtenerCuentaPorTipo(TipoCuenta.REFUGIO));
        model.addAttribute("cuentaTienda",  cuentaService.obtenerCuentaPorTipo(TipoCuenta.TIENDA));

        return "tienda/carrito";
    }


    // ELIMINAR DEL CARRITO

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Integer idProducto, HttpSession session) {
        obtenerCarrito(session).remove(idProducto);
        return "redirect:/tienda/carrito";
    }

    // CONFIRMAR COMPRA


    @PostMapping("/confirmar")
    public String confirmar(HttpSession session, RedirectAttributes ra) {
        Map<Integer, Integer> carrito = obtenerCarrito(session);
        try {
            Pedido pedido = pedidoService.confirmarPedido(carrito);
            session.removeAttribute(SESION_CARRITO); // vaciar el carrito
            return "redirect:/tienda/confirmacion/" + pedido.getIdPedido();

        } catch (SaldoInsuficienteException e) {
            // 🆕 Saldo insuficiente → redirige a transferir con mensaje
            ra.addFlashAttribute("mensajeError", "🚫 " + e.getMessage());
            return "redirect:/admin/cuenta/transferir";

        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/tienda/carrito";
        }
    }
}