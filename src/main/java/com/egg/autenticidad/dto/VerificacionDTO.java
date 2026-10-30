package com.egg.autenticidad.dto;

import com.egg.autenticidad.entidades.Producto;
import com.egg.autenticidad.entidades.enumeraciones.Resultado;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO (Data Transfer Object): lo que viaja del servicio a la pantalla del cliente.
 * Asi no exponemos la entidad Producto completa (con su token) en la vista.
 * Un "record" es una clase inmutable compacta. Implementa Serializable porque
 * viaja en la sesion mediante flash attributes (patron Post/Redirect/Get).
 */
public record VerificacionDTO(
        Resultado resultado,
        String codigoFabrica,
        String modelo,
        String tienda,
        LocalDateTime fechaVenta,
        LocalDateTime fechaVerificacion) implements Serializable {

    public static VerificacionDTO sinProducto(Resultado resultado) {
        return new VerificacionDTO(resultado, null, null, null, null, null);
    }

    public static VerificacionDTO de(Resultado resultado, Producto p, LocalDateTime fechaVerificacion) {
        return new VerificacionDTO(
                resultado,
                p.getCodigoFabrica(),
                p.getModelo() != null ? p.getModelo().getNombre() : "-",
                p.getTienda() != null ? p.getTienda().getNombre() : "-",
                p.getFechaVenta(),
                fechaVerificacion);
    }

    /** Solo se muestran los datos del producto cuando fue vendido de forma legitima. */
    public boolean mostrarDatos() {
        return codigoFabrica != null
                && (resultado == Resultado.AUTENTICO || resultado == Resultado.YA_VERIFICADO);
    }
}
