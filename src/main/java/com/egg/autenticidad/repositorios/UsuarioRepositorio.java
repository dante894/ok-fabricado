package com.egg.autenticidad.repositorios;

import com.egg.autenticidad.entidades.Tienda;
import com.egg.autenticidad.entidades.Usuario;
import com.egg.autenticidad.entidades.enumeraciones.Rol;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    // Optional obliga a pensar en el caso "no existe" (adios NullPointerException)
    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByTienda(Tienda tienda);

    long countByRol(Rol rol);

    List<Usuario> findAllByOrderByNombreAsc();
}
