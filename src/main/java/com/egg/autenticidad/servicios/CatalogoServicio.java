package com.egg.autenticidad.servicios;

import com.egg.autenticidad.entidades.Modelo;
import com.egg.autenticidad.entidades.Tienda;
import com.egg.autenticidad.repositorios.ModeloRepositorio;
import com.egg.autenticidad.repositorios.TiendaRepositorio;
import java.util.List;
import org.springframework.stereotype.Service;

/** Listas de apoyo (modelos y tiendas) para los desplegables de las pantallas. */
@Service
public class CatalogoServicio {

    private final ModeloRepositorio modeloRepositorio;
    private final TiendaRepositorio tiendaRepositorio;

    public CatalogoServicio(ModeloRepositorio modeloRepositorio, TiendaRepositorio tiendaRepositorio) {
        this.modeloRepositorio = modeloRepositorio;
        this.tiendaRepositorio = tiendaRepositorio;
    }

    public List<Modelo> listarModelos() {
        return modeloRepositorio.findAllByOrderByNombreAsc();
    }

    public List<Tienda> listarTiendas() {
        return tiendaRepositorio.findAllByOrderByNombreAsc();
    }
}
