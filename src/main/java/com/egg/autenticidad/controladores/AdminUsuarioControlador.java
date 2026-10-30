package com.egg.autenticidad.controladores;

import com.egg.autenticidad.entidades.enumeraciones.Rol;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.servicios.CatalogoServicio;
import com.egg.autenticidad.servicios.UsuarioServicio;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioControlador {

    private final UsuarioServicio usuarioServicio;
    private final CatalogoServicio catalogoServicio;

    public AdminUsuarioControlador(UsuarioServicio usuarioServicio, CatalogoServicio catalogoServicio) {
        this.usuarioServicio = usuarioServicio;
        this.catalogoServicio = catalogoServicio;
    }

    @GetMapping
    public String listar(ModelMap modelo) {
        modelo.put("usuarios", usuarioServicio.listar());
        modelo.put("tiendas", catalogoServicio.listarTiendas());
        modelo.put("roles", Rol.values()); // el desplegable de roles sale del enum
        return "admin/usuarios";
    }

    @PostMapping
    public String crear(@RequestParam(name = "nombre", required = false) String nombre,
                        @RequestParam(name = "email", required = false) String email,
                        @RequestParam(name = "clave", required = false) String clave,
                        @RequestParam(name = "rol", required = false) Rol rol,
                        @RequestParam(name = "idTienda", required = false) Long idTienda,
                        RedirectAttributes redirect) {
        try {
            usuarioServicio.crear(nombre, email, clave, rol, idTienda);
            redirect.addFlashAttribute("exito", "Usuario creado.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/clave")
    public String cambiarClave(@PathVariable("id") Long id,
                               @RequestParam(name = "clave", required = false) String clave,
                               RedirectAttributes redirect) {
        try {
            usuarioServicio.cambiarClave(id, clave);
            redirect.addFlashAttribute("exito", "Contraseña actualizada.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, Authentication autenticacion, RedirectAttributes redirect) {
        try {
            usuarioServicio.eliminar(id, autenticacion.getName());
            redirect.addFlashAttribute("exito", "Usuario eliminado.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }
}
