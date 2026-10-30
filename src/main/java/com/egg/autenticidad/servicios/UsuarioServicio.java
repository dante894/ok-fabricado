package com.egg.autenticidad.servicios;

import com.egg.autenticidad.entidades.Tienda;
import com.egg.autenticidad.entidades.Usuario;
import com.egg.autenticidad.entidades.enumeraciones.Rol;
import com.egg.autenticidad.excepciones.MiExcepcion;
import com.egg.autenticidad.repositorios.TiendaRepositorio;
import com.egg.autenticidad.repositorios.UsuarioRepositorio;
import java.util.List;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa UserDetailsService: es el "puente" que Spring Security usa para
 * preguntarnos "dame el usuario con este email" cuando alguien hace login.
 * Ademas contiene la administracion de usuarios (alta, cambio de clave y baja).
 */
@Service
public class UsuarioServicio implements UserDetailsService {

    private static final int CLAVE_MINIMA = 6;

    private final UsuarioRepositorio usuarioRepositorio;
    private final TiendaRepositorio tiendaRepositorio;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, TiendaRepositorio tiendaRepositorio,
                           PasswordEncoder passwordEncoder) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tiendaRepositorio = tiendaRepositorio;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el email: " + email));

        // roles("ADMIN") le agrega el prefijo ROLE_ automaticamente
        return User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el email: " + email));
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepositorio.findAllByOrderByNombreAsc();
    }

    @Transactional
    public void crear(String nombre, String email, String clave, Rol rol, Long idTienda) throws MiExcepcion {
        if (nombre == null || nombre.isBlank()) {
            throw new MiExcepcion("El nombre no puede estar vacío");
        }
        if (email == null || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new MiExcepcion("Ingresa un email válido");
        }
        String emailLimpio = email.trim().toLowerCase();
        if (usuarioRepositorio.existsByEmail(emailLimpio)) {
            throw new MiExcepcion("Ya existe un usuario con ese email");
        }
        validarClave(clave);
        if (rol == null) {
            throw new MiExcepcion("Elige un rol");
        }

        Tienda tienda = null;
        if (rol == Rol.TIENDA) {
            if (idTienda == null) {
                throw new MiExcepcion("Un cajero necesita una tienda asignada");
            }
            tienda = tiendaRepositorio.findById(idTienda)
                    .orElseThrow(() -> new MiExcepcion("La tienda elegida no existe"));
        }

        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setEmail(emailLimpio);
        u.setPassword(passwordEncoder.encode(clave)); // BCrypt: nunca se guarda la clave real
        u.setRol(rol);
        u.setTienda(tienda); // los demas roles no llevan tienda
        usuarioRepositorio.save(u);
    }

    @Transactional
    public void cambiarClave(Long id, String clave) throws MiExcepcion {
        validarClave(clave);
        Usuario u = usuarioRepositorio.findById(id).orElseThrow(() -> new MiExcepcion("El usuario no existe"));
        u.setPassword(passwordEncoder.encode(clave));
    }

    @Transactional
    public void eliminar(Long id, String emailQuienElimina) throws MiExcepcion {
        Usuario u = usuarioRepositorio.findById(id).orElseThrow(() -> new MiExcepcion("El usuario no existe"));
        if (u.getEmail().equalsIgnoreCase(emailQuienElimina)) {
            throw new MiExcepcion("No puedes eliminar tu propio usuario");
        }
        if (u.getRol() == Rol.ADMIN && usuarioRepositorio.countByRol(Rol.ADMIN) <= 1) {
            throw new MiExcepcion("No se puede eliminar al último administrador");
        }
        usuarioRepositorio.delete(u);
    }

    private void validarClave(String clave) throws MiExcepcion {
        if (clave == null || clave.length() < CLAVE_MINIMA) {
            throw new MiExcepcion("La contraseña debe tener al menos " + CLAVE_MINIMA + " caracteres");
        }
    }
}
