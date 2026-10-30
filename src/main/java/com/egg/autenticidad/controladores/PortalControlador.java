package com.egg.autenticidad.controladores;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PortalControlador {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(name = "error", required = false) String error,
                        @RequestParam(name = "logout", required = false) String logout,
                        ModelMap modelo) {
        if (error != null) {
            modelo.put("error", "Email o contraseña incorrectos");
        }
        if (logout != null) {
            modelo.put("exito", "Cerraste sesión correctamente");
        }
        return "login";
    }

    @GetMapping("/panel")
    public String panel() {
        return "panel";
    }
}
