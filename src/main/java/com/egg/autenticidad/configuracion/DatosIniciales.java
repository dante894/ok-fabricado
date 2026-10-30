package com.egg.autenticidad.configuracion;

import com.egg.autenticidad.entidades.Modelo;
import com.egg.autenticidad.entidades.Tienda;
import com.egg.autenticidad.entidades.Usuario;
import com.egg.autenticidad.entidades.enumeraciones.Rol;
import com.egg.autenticidad.repositorios.ModeloRepositorio;
import com.egg.autenticidad.repositorios.TiendaRepositorio;
import com.egg.autenticidad.repositorios.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Se ejecuta UNA vez al arrancar la aplicacion y crea datos de prueba si la base esta vacia.
 * CommandLineRunner = "corre este codigo cuando Spring termine de iniciar".
 * IMPORTANTE: estas claves son solo para desarrollo. Borra esta clase antes de publicar.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepositorio usuarioRepositorio;
    private final TiendaRepositorio tiendaRepositorio;
    private final ModeloRepositorio modeloRepositorio;
    private final PasswordEncoder passwordEncoder;

    public DatosIniciales(UsuarioRepositorio usuarioRepositorio, TiendaRepositorio tiendaRepositorio,
                          ModeloRepositorio modeloRepositorio, PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tiendaRepositorio = tiendaRepositorio;
        this.modeloRepositorio = modeloRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepositorio.count() > 0) {
            return;
        }
        for (String nombre : new String[]{"Bolso Aurora", "Cartera Milano", "Bota Sierra"}) {
            Modelo m = new Modelo();
            m.setNombre(nombre);
            modeloRepositorio.save(m);
        }
        Tienda centro = crearTienda("Tienda Centro", "Ciudad");
        crearTienda("Tienda Plaza Norte", "Ciudad");

        crearUsuario("Administrador", "admin@demo.com", "admin123", Rol.ADMIN, null);
        crearUsuario("Operario de fábrica", "fabrica@demo.com", "fabrica123", Rol.FABRICA, null);
        crearUsuario("Cajero Centro", "caja@demo.com", "caja123", Rol.TIENDA, centro);

        log.info("Datos de prueba creados. Usuarios: admin@demo.com / fabrica@demo.com / caja@demo.com");
    }

    private Tienda crearTienda(String nombre, String ciudad) {
        Tienda t = new Tienda();
        t.setNombre(nombre);
        t.setCiudad(ciudad);
        return tiendaRepositorio.save(t);
    }

    private void crearUsuario(String nombre, String email, String clave, Rol rol, Tienda tienda) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(clave)); // se guarda cifrada
        u.setRol(rol);
        u.setTienda(tienda);
        usuarioRepositorio.save(u);
    }
}
