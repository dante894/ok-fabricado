package com.egg.autenticidad.controladores;

import com.egg.autenticidad.entidades.Producto;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.servicios.CatalogoServicio;
import com.egg.autenticidad.servicios.EtiquetaServicio;
import com.egg.autenticidad.servicios.ProductoServicio;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fabrica")
public class FabricaControlador {

    private final ProductoServicio productoServicio;
    private final CatalogoServicio catalogoServicio;
    private final EtiquetaServicio etiquetaServicio;

    public FabricaControlador(ProductoServicio productoServicio, CatalogoServicio catalogoServicio,
                              EtiquetaServicio etiquetaServicio) {
        this.productoServicio = productoServicio;
        this.catalogoServicio = catalogoServicio;
        this.etiquetaServicio = etiquetaServicio;
    }

    @GetMapping
    public String panel(ModelMap modelo) {
        modelo.put("modelos", catalogoServicio.listarModelos());
        modelo.put("productos", productoServicio.listarRecientes());
        return "fabrica";
    }

    @PostMapping("/lote")
    public String crearLote(@RequestParam(name = "idModelo", required = false) Long idModelo,
                            @RequestParam(name = "cantidad", required = false) Integer cantidad,
                            RedirectAttributes redirect) {
        try {
            List<Producto> lote = productoServicio.crearLote(idModelo, cantidad);
            redirect.addFlashAttribute("exito", "Se generaron " + lote.size() + " productos nuevos.");
            // Los ids viajan al panel "Lote recien creado", desde donde se imprimen sus etiquetas
            redirect.addFlashAttribute("idsNuevos", lote.stream().map(Producto::getId).toList());
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // Post/Redirect/Get: despues de un POST siempre redirigimos, asi F5 no repite la accion
        return "redirect:/fabrica";
    }

    @PostMapping("/enviar/{id}")
    public String enviar(@PathVariable("id") Long id, RedirectAttributes redirect) {
        try {
            productoServicio.enviarATienda(id);
            redirect.addFlashAttribute("exito", "Producto marcado como enviado a tienda.");
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/fabrica";
    }

    /**
     * Devuelve un PDF con las etiquetas. Se escribe directo en la respuesta (HttpServletResponse),
     * por eso el metodo devuelve null: ya no hay una vista que mostrar.
     * Si algo falla, se redirige al panel con el mensaje de error.
     */
    @PostMapping("/etiquetas")
    public String etiquetas(@RequestParam(name = "ids", required = false) List<Long> ids,
                            @RequestParam(name = "tipo", defaultValue = "qr") String tipo,
                            HttpServletResponse respuesta, RedirectAttributes redirect) throws IOException {
        try {
            EtiquetaServicio.Tipo tipoEtiqueta = "codigo".equalsIgnoreCase(tipo)
                    ? EtiquetaServicio.Tipo.CODIGO : EtiquetaServicio.Tipo.QR;
            byte[] pdf = etiquetaServicio.generarPdf(ids, tipoEtiqueta);

            respuesta.setContentType("application/pdf");
            // inline = se abre en el navegador (desde ahi puedes imprimir o guardar)
            respuesta.setHeader("Content-Disposition",
                    "inline; filename=\"etiquetas-" + tipoEtiqueta.name().toLowerCase() + ".pdf\"");
            respuesta.setContentLength(pdf.length);
            respuesta.getOutputStream().write(pdf);
            return null;
        } catch (MiExcepcion e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/fabrica";
        }
    }
}
