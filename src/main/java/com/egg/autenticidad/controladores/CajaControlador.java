package com.egg.autenticidad.controladores;

import com.egg.autenticidad.entidades.Producto;
import com.egg.autenticidad.entidades.Usuario;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.servicios.ProductoServicio;
import com.egg.autenticidad.servicios.UsuarioServicio;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/caja")
public class CajaControlador {

    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    public CajaControlador(ProductoServicio productoServicio, UsuarioServicio usuarioServicio) {
        this.productoServicio = productoServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public String caja(Authentication autenticacion, ModelMap modelo) {
        // Authentication la inyecta Spring: dice quien esta logueado (getName() = su email)
        Usuario usuario = usuarioServicio.buscarPorEmail(autenticacion.getName());
        modelo.put("tienda", usuario.getTienda());
        if (usuario.getTienda() != null) {
            modelo.put("ventas", productoServicio.ultimasVentas(usuario.getTienda()));
        }
        return "caja";
    }

    @PostMapping("/vender")
    public String vender(@RequestParam(name = "codigo", required = false) String codigo,
                         Authentication autenticacion, RedirectAttributes redirect) {
        try {
            Usuario usuario = usuarioServicio.buscarPorEmail(autenticacion.getName());
            Producto p = productoServicio.vender(codigo, usuario.getTienda());
            redirect.addFlashAttribute("exito", p.getCodigoFabrica() + " marcado como vendido.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/caja";
    }
}
