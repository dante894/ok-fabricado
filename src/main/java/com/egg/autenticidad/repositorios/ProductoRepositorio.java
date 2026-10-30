package com.egg.autenticidad.repositorios;

import com.egg.autenticidad.entidades.Modelo;
import com.egg.autenticidad.entidades.Producto;
import com.egg.autenticidad.entidades.Tienda;
import com.egg.autenticidad.entidades.enumeraciones.Estado;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    Optional<Producto> findByToken(String token);

    // Sirven para no borrar un modelo o una tienda que ya tiene historial
    boolean existsByModelo(Modelo modelo);

    boolean existsByTienda(Tienda tienda);

    Optional<Producto> findByCodigoFabrica(String codigoFabrica);

    List<Producto> findTop100ByOrderByIdDesc();

    List<Producto> findTop20ByEstadoAndTiendaOrderByFechaVentaDesc(Estado estado, Tienda tienda);

    /**
     * LA CLAVE DEL "SOLO UNA VEZ".
     *
     * Es un UPDATE condicional: solo cambia la fila si TODAVIA esta vendida y sin verificar.
     * La base de datos ejecuta esto de forma atomica, asi que si dos personas escanean
     * al mismo tiempo, una sola obtiene 1 fila modificada y la otra obtiene 0.
     *
     * Con "buscar, revisar y guardar" en Java, los dos podrian pasar a la vez.
     *
     * Devuelve cuantas filas cambio: 1 = ganaste la verificacion, 0 = ya estaba usada.
     * clearAutomatically limpia la memoria de JPA para que no quede un Producto desactualizado.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Producto p SET p.verificado = true, p.fechaVerificacion = :ahora "
            + "WHERE p.token = :token AND p.estado = :estado AND p.verificado = false")
    int marcarVerificado(@Param("token") String token,
                         @Param("estado") Estado estado,
                         @Param("ahora") LocalDateTime ahora);
}
