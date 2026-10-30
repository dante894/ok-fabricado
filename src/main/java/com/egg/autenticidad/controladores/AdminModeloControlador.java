package com.egg.autenticidad.controladores;

import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.servicios.CatalogoServicio;
import com.egg.autenticidad.servicios.ModeloServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/modelos")
public class AdminModeloControlador {

    private final ModeloServicio modeloServicio;
    private final CatalogoServicio catalogoServicio;

    public AdminModeloControlador(ModeloServicio modeloServicio, CatalogoServicio catalogoServicio) {
        this.modeloServicio = modeloServicio;
        this.catalogoServicio = catalogoServicio;
    }

    @GetMapping
    public String listar(ModelMap modelo) {
        modelo.put("modelos", catalogoServicio.listarModelos());
        return "admin/modelos";
    }

    @PostMapping
    public String crear(@RequestParam(name = "nombre", required = false) String nombre, RedirectAttributes redirect) {
        try {
            modeloServicio.crear(nombre);
            redirect.addFlashAttribute("exito", "Modelo creado.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/modelos";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id,
                         @RequestParam(name = "nombre", required = false) String nombre, RedirectAttributes redirect) {
        try {
            modeloServicio.modificar(id, nombre);
            redirect.addFlashAttribute("exito", "Modelo actualizado.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/modelos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            modeloServicio.eliminar(id);
            redirect.addFlashAttribute("exito", "Modelo eliminado.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/modelos";
    }
}
