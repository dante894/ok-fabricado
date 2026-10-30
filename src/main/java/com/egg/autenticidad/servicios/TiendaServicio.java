package com.egg.autenticidad.servicios;

import com.egg.autenticidad.entidades.Tienda;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.repositorios.ProductoRepositorio;
import com.egg.autenticidad.repositorios.TiendaRepositorio;
import com.egg.autenticidad.repositorios.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta, edicion y baja de tiendas (equivale a EditorialServicio en biblioteca). */
@Service
public class TiendaServicio {

    private final TiendaRepositorio tiendaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

    public TiendaServicio(TiendaRepositorio tiendaRepositorio, ProductoRepositorio productoRepositorio,
                          UsuarioRepositorio usuarioRepositorio) {
        this.tiendaRepositorio = tiendaRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Transactional
    public void crear(String nombre, String ciudad) throws MiExcepcion {
        String limpio = validarNombre(nombre);
        if (tiendaRepositorio.existsByNombreIgnoreCase(limpio)) {
            throw new MiExcepcion("Ya existe una tienda con ese nombre");
        }
        Tienda t = new Tienda();
        t.setNombre(limpio);
        t.setCiudad(limpiarCiudad(ciudad));
        tiendaRepositorio.save(t);
    }

    @Transactional
    public void modificar(Long id, String nombre, String ciudad) throws MiExcepcion {
        String limpio = validarNombre(nombre);
        Tienda t = buscar(id);
        if (tiendaRepositorio.existsByNombreIgnoreCaseAndIdNot(limpio, id)) {
            throw new MiExcepcion("Ya existe otra tienda con ese nombre");
        }
        t.setNombre(limpio);
        t.setCiudad(limpiarCiudad(ciudad));
    }

    @Transactional
    public void eliminar(Long id) throws MiExcepcion {
        Tienda t = buscar(id);
        if (productoRepositorio.existsByTienda(t)) {
            throw new MiExcepcion("No se puede eliminar: la tienda ya tiene ventas registradas");
        }
        if (usuarioRepositorio.existsByTienda(t)) {
            throw new MiExcepcion("No se puede eliminar: hay usuarios asignados a esta tienda");
        }
        tiendaRepositorio.delete(t);
    }

    private Tienda buscar(Long id) throws MiExcepcion {
        return tiendaRepositorio.findById(id).orElseThrow(() -> new MiExcepcion("La tienda no existe"));
    }

    private String validarNombre(String nombre) throws MiExcepcion {
        if (nombre == null || nombre.isBlank()) {
            throw new MiExcepcion("El nombre de la tienda no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 100) {
            throw new MiExcepcion("El nombre es demasiado largo (máximo 100 caracteres)");
        }
        return limpio;
    }

    private String limpiarCiudad(String ciudad) {
        return (ciudad == null || ciudad.isBlank()) ? null : ciudad.trim();
    }
}
