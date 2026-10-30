package com.egg.autenticidad.controladores;

import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.servicios.CatalogoServicio;
import com.egg.autenticidad.servicios.TiendaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tiendas")
public class AdminTiendaControlador {

    private final TiendaServicio tiendaServicio;
    private final CatalogoServicio catalogoServicio;

    public AdminTiendaControlador(TiendaServicio tiendaServicio, CatalogoServicio catalogoServicio) {
        this.tiendaServicio = tiendaServicio;
        this.catalogoServicio = catalogoServicio;
    }

    @GetMapping
    public String listar(ModelMap modelo) {
        modelo.put("tiendas", catalogoServicio.listarTiendas());
        return "admin/tiendas";
    }

    @PostMapping
    public String crear(@RequestParam(name = "nombre", required = false) String nombre,
                        @RequestParam(name = "ciudad", required = false) String ciudad, RedirectAttributes redirect) {
        try {
            tiendaServicio.crear(nombre, ciudad);
            redirect.addFlashAttribute("exito", "Tienda creada.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tiendas";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id,
                         @RequestParam(name = "nombre", required = false) String nombre,
                         @RequestParam(name = "ciudad", required = false) String ciudad, RedirectAttributes redirect) {
        try {
            tiendaServicio.modificar(id, nombre, ciudad);
            redirect.addFlashAttribute("exito", "Tienda actualizada.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tiendas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            tiendaServicio.eliminar(id);
            redirect.addFlashAttribute("exito", "Tienda eliminada.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/tiendas";
    }
}
