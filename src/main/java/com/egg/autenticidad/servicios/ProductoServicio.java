package com.egg.autenticidad.servicios;

import com.egg.autenticidad.entidades.Modelo;
import com.egg.autenticidad.entidades.Producto;
import com.egg.autenticidad.entidades.Tienda;
import com.egg.autenticidad.entidades.enumeraciones.Estado;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.repositorios.ModeloRepositorio;
import com.egg.autenticidad.repositorios.ProductoRepositorio;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Ciclo de vida del producto: fabricar -> enviar a tienda -> vender. */
@Service
public class ProductoServicio {

    private static final String PREFIJO = "BR";
    private static final int MAX_LOTE = 500;

    // SecureRandom es criptograficamente seguro: Random normal seria predecible
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private final ProductoRepositorio productoRepositorio;
    private final ModeloRepositorio modeloRepositorio;

    public ProductoServicio(ProductoRepositorio productoRepositorio, ModeloRepositorio modeloRepositorio) {
        this.productoRepositorio = productoRepositorio;
        this.modeloRepositorio = modeloRepositorio;
    }

    /** FABRICA: crea 'cantidad' productos nuevos, cada uno con su codigo y su token. */
    @Transactional
    public List<Producto> crearLote(Long idModelo, Integer cantidad) throws MiExcepcion {
        if (idModelo == null) {
            throw new MiExcepcion("Elige un modelo");
        }
        if (cantidad == null || cantidad < 1 || cantidad > MAX_LOTE) {
            throw new MiExcepcion("La cantidad debe estar entre 1 y " + MAX_LOTE);
        }
        Modelo modelo = modeloRepositorio.findById(idModelo)
                .orElseThrow(() -> new MiExcepcion("El modelo elegido no existe"));

        List<Producto> lote = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            Producto p = new Producto();
            p.setModelo(modelo);
            p.setToken(generarToken());
            p.setEstado(Estado.FABRICADO);
            p.setFechaFabricacion(LocalDateTime.now());
            p.setVerificado(false);

            // saveAndFlush guarda ya mismo para que la base nos asigne el id...
            p = productoRepositorio.saveAndFlush(p);
            // ...y con el id armamos el codigo: BR-2026-000123.
            // Como p esta "gestionado" por JPA, este cambio se guarda solo al terminar la transaccion
            // (dirty checking): no hace falta llamar a save() otra vez.
            p.setCodigoFabrica(String.format("%s-%d-%06d", PREFIJO, Year.now().getValue(), p.getId()));
            lote.add(p);
        }
        return lote;
    }

    /** 24 bytes aleatorios -> 32 caracteres seguros para URL. Imposible de adivinar. */
    private String generarToken() {
        byte[] bytes = new byte[24];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Transactional(readOnly = true)
    public List<Producto> listarRecientes() {
        return productoRepositorio.findTop100ByOrderByIdDesc();
    }

    /** FABRICA: marca que el producto salio hacia las tiendas. */
    @Transactional
    public void enviarATienda(Long id) throws MiExcepcion {
        Producto p = productoRepositorio.findById(id)
                .orElseThrow(() -> new MiExcepcion("El producto no existe"));
        if (p.getEstado() != Estado.FABRICADO) {
            throw new MiExcepcion("Solo se pueden enviar productos en estado FABRICADO");
        }
        p.setEstado(Estado.ENVIADO);
    }

    /** CAJA: el cajero escribe (o escanea) el codigo de fabrica de la etiqueta y lo marca como vendido. */
    @Transactional
    public Producto vender(String codigoFabrica, Tienda tienda) throws MiExcepcion {
        if (tienda == null) {
            throw new MiExcepcion("Tu usuario no tiene una tienda asignada");
        }
        if (codigoFabrica == null || codigoFabrica.isBlank()) {
            throw new MiExcepcion("Ingresa el código de fábrica");
        }
        Producto p = productoRepositorio.findByCodigoFabrica(codigoFabrica.trim().toUpperCase())
                .orElseThrow(() -> new MiExcepcion("No existe un producto con ese código"));

        if (p.getEstado() == Estado.VENDIDO) {
            throw new MiExcepcion("Este producto ya fue marcado como vendido");
        }
        if (p.getEstado() == Estado.FABRICADO) {
            throw new MiExcepcion("Este producto aún no figura como enviado a tienda: revisa la mercancía recibida");
        }
        p.setEstado(Estado.VENDIDO);
        p.setTienda(tienda);
        p.setFechaVenta(LocalDateTime.now());
        return p;
    }

    @Transactional(readOnly = true)
    public List<Producto> ultimasVentas(Tienda tienda) {
        return productoRepositorio.findTop20ByEstadoAndTiendaOrderByFechaVentaDesc(Estado.VENDIDO, tienda);
    }
}
