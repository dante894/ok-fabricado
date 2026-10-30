package com.egg.autenticidad.repositorios;

import com.egg.autenticidad.entidades.Modelo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModeloRepositorio extends JpaRepository<Modelo, Long> {

    // Spring Data crea la consulta SOLO a partir del nombre del metodo
    List<Modelo> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
