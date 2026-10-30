package com.egg.autenticidad.controladores;

import com.egg.autenticidad.servicios.VerificacionServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AlertasControlador {

    private final VerificacionServicio verificacionServicio;

    public AlertasControlador(VerificacionServicio verificacionServicio) {
        this.verificacionServicio = verificacionServicio;
    }

    @GetMapping("/alertas")
    public String alertas(ModelMap modelo) {
        modelo.put("escaneos", verificacionServicio.ultimosEscaneos());
        return "alertas";
    }
}
