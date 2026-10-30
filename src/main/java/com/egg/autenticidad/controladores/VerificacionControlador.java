package com.egg.autenticidad.controladores;

import com.egg.autenticidad.servicios.VerificacionServicio;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

/**
 * Pantalla publica del cliente (la que abre el QR). Tres pasos:
 *
 *  1) GET  /v/{token}            -> solo muestra un boton "Verificar". NO gasta la verificacion.
 *  2) POST /v/{token}            -> aqui SI se verifica (una sola vez) y se redirige
 *  3) GET  /v/{token}/resultado  -> muestra el resultado (si recargas, no se vuelve a gastar)
 *
 * Por que el boton: WhatsApp, antivirus y vistas previas de enlaces abren las URL solos con GET.
 * Si el GET gastara la verificacion, un robot se la quitaria al comprador real.
 */
@Controller
@RequestMapping("/v")
public class VerificacionControlador {

    private final VerificacionServicio verificacionServicio;

    public VerificacionControlador(VerificacionServicio verificacionServicio) {
        this.verificacionServicio = verificacionServicio;
    }

    @GetMapping("/{token}")
    public String pantalla(@PathVariable("token") String token, ModelMap modelo) {
        modelo.put("token", token);
        return "verificar";
    }

    @PostMapping("/{token}")
    public String verificar(@PathVariable("token") String token,
                            HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("verificacion",
                verificacionServicio.verificar(token, request.getRemoteAddr()));
        // {token} se rellena y se codifica de forma segura con este atributo
        redirect.addAttribute("token", token);
        return "redirect:/v/{token}/resultado";
    }

    @GetMapping("/{token}/resultado")
    public String resultado(@PathVariable("token") String token, ModelMap modelo) {
        // Sin resultado en memoria (recarga o link directo): volver al boton, sin gastar nada
        if (!modelo.containsAttribute("verificacion")) {
            return "redirect:/v/" + UriUtils.encodePathSegment(token, StandardCharsets.UTF_8);
        }
        return "resultado";
    }
}
