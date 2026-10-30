package com.egg.autenticidad.servicios;

import com.egg.autenticidad.dto.VerificacionDTO;
import com.egg.autenticidad.entidades.Escaneo;
import com.egg.autenticidad.entidades.Producto;
import com.egg.autenticidad.entidades.enumeraciones.Estado;
import com.egg.autenticidad.entidades.enumeraciones.Resultado;
import com.egg.autenticidad.repositorios.EscaneoRepositorio;
import com.egg.autenticidad.repositorios.ProductoRepositorio;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** El corazon del sistema: decide que responde la plataforma cuando alguien escanea un QR. */
@Service
public class VerificacionServicio {

    // Nuestros tokens solo tienen letras, numeros, - y _. Cualquier otra cosa es basura o un ataque.
    private static final Pattern TOKEN_VALIDO = Pattern.compile("^[A-Za-z0-9_-]{1,64}$");

    private final ProductoRepositorio productoRepositorio;
    private final EscaneoRepositorio escaneoRepositorio;

    public VerificacionServicio(ProductoRepositorio productoRepositorio, EscaneoRepositorio escaneoRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.escaneoRepositorio = escaneoRepositorio;
    }

    /** Decide el resultado Y deja constancia en la bitacora, todo en una sola transaccion. */
    @Transactional
    public VerificacionDTO verificar(String token, String ip) {
        VerificacionDTO resultado = decidir(token);
        registrar(token, ip, resultado);
        return resultado;
    }

    private VerificacionDTO decidir(String token) {
        if (token == null || !TOKEN_VALIDO.matcher(token).matches()) {
            return VerificacionDTO.sinProducto(Resultado.INEXISTENTE);
        }

        Optional<Producto> encontrado = productoRepositorio.findByToken(token);
        if (encontrado.isEmpty()) {
            return VerificacionDTO.sinProducto(Resultado.INEXISTENTE);
        }
        Producto p = encontrado.get();

        // Aun no lo vendio una tienda oficial: no hay 👌 posible
        if (p.getEstado() != Estado.VENDIDO) {
            return VerificacionDTO.de(Resultado.NO_VENDIDO, p, null);
        }

        // Caso normal de segundo escaneo: ya estaba marcado
        if (p.isVerificado()) {
            return VerificacionDTO.de(Resultado.YA_VERIFICADO, p, p.getFechaVerificacion());
        }

        // Primer escaneo: intentamos "ganar" la verificacion con el UPDATE atomico.
        LocalDateTime ahora = LocalDateTime.now();
        int filasCambiadas = productoRepositorio.marcarVerificado(token, Estado.VENDIDO, ahora);

        if (filasCambiadas == 1) {
            return VerificacionDTO.de(Resultado.AUTENTICO, p, ahora);
        }
        // 0 filas: otra persona escaneo en el mismo instante y gano ella.
        // No sabemos su hora exacta (la base mantiene una "foto" de la transaccion), por eso va null.
        return VerificacionDTO.de(Resultado.YA_VERIFICADO, p, null);
    }

    private void registrar(String token, String ip, VerificacionDTO resultado) {
        Escaneo e = new Escaneo();
        e.setFecha(LocalDateTime.now());
        boolean formatoOk = token != null && TOKEN_VALIDO.matcher(token).matches();
        e.setTokenParcial(formatoOk ? token.substring(0, Math.min(12, token.length())) : "(formato inválido)");
        e.setCodigoFabrica(resultado.codigoFabrica());
        e.setResultado(resultado.resultado());
        e.setIp(ip);
        escaneoRepositorio.save(e);
    }

    @Transactional(readOnly = true)
    public List<Escaneo> ultimosEscaneos() {
        return escaneoRepositorio.findTop200ByOrderByFechaDesc();
    }
}
