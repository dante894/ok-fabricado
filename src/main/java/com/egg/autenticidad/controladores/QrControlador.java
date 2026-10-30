package com.egg.autenticidad.controladores;

import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.servicios.QrServicio;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Devuelve el QR como imagen PNG (misma idea que ImagenControlador en biblioteca). Solo personal autorizado. */
@Controller
public class QrControlador {

    private final QrServicio qrServicio;

    public QrControlador(QrServicio qrServicio) {
        this.qrServicio = qrServicio;
    }

    @GetMapping("/qr/{token}")
    public ResponseEntity<byte[]> qr(@PathVariable("token") String token) {
        try {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(qrServicio.generarPng(token));
        } catch (MiExcepcion e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
