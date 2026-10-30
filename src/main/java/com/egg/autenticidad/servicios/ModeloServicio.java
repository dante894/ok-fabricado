package com.egg.autenticidad.servicios;

import com.egg.autenticidad.entidades.Modelo;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.repositorios.ModeloRepositorio;
import com.egg.autenticidad.repositorios.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Alta, edicion y baja de modelos (equivale a AutorServicio en biblioteca). */
@Service
public class ModeloServicio {

    private final ModeloRepositorio modeloRepositorio;
    private final ProductoRepositorio productoRepositorio;

    public ModeloServicio(ModeloRepositorio modeloRepositorio, ProductoRepositorio productoRepositorio) {
        this.modeloRepositorio = modeloRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    @Transactional
    public void crear(String nombre) throws MiExcepcion {
        String limpio = validarNombre(nombre);
        if (modeloRepositorio.existsByNombreIgnoreCase(limpio)) {
            throw new MiExcepcion("Ya existe un modelo con ese nombre");
        }
        Modelo m = new Modelo();
        m.setNombre(limpio);
        modeloRepositorio.save(m);
    }

    @Transactional
    public void modificar(Long id, String nombre) throws MiExcepcion {
        String limpio = validarNombre(nombre);
        Modelo m = buscar(id);
        if (modeloRepositorio.existsByNombreIgnoreCaseAndIdNot(limpio, id)) {
            throw new MiExcepcion("Ya existe otro modelo con ese nombre");
        }
        m.setNombre(limpio); // dirty checking: se guarda solo al terminar la transaccion
    }

    @Transactional
    public void eliminar(Long id) throws MiExcepcion {
        Modelo m = buscar(id);
        if (productoRepositorio.existsByModelo(m)) {
            throw new MiExcepcion("No se puede eliminar: ya hay productos fabricados con este modelo");
        }
        modeloRepositorio.delete(m);
    }

    private Modelo buscar(Long id) throws MiExcepcion {
        return modeloRepositorio.findById(id).orElseThrow(() -> new MiExcepcion("El modelo no existe"));
    }

    private String validarNombre(String nombre) throws MiExcepcion {
        if (nombre == null || nombre.isBlank()) {
            throw new MiExcepcion("El nombre del modelo no puede estar vacío");
        }
        String limpio = nombre.trim();
        if (limpio.length() > 100) {
            throw new MiExcepcion("El nombre es demasiado largo (máximo 100 caracteres)");
        }
        return limpio;
    }
}
