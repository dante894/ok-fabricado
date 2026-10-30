package com.egg.autenticidad.repositorios;

import com.egg.autenticidad.entidades.Escaneo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EscaneoRepositorio extends JpaRepository<Escaneo, Long> {

    // "Top200" limita a 200 filas; "OrderByFechaDesc" ordena del mas reciente al mas antiguo
    List<Escaneo> findTop200ByOrderByFechaDesc();
}
