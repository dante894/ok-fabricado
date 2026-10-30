package com.egg.autenticidad.entidades;

import com.egg.autenticidad.entidades.enumeraciones.Estado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;

/**
 * Cada unidad fisica (un bolso, una cartera, unas botas).
 * Es la entidad central: guarda el codigo de fabrica, el token del QR,
 * en que etapa esta (estado) y si ya se uso su verificacion unica.
 */
@Entity
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ej: BR-2026-000123. Se calcula despues de guardar, a partir del id
    @Column(unique = true)
    private String codigoFabrica;

    // Texto aleatorio imposible de adivinar que va dentro del QR. unique = true crea un indice: la busqueda es rapida
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne
    private Modelo modelo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    // Tienda donde se vendio (null hasta que se vende)
    @ManyToOne
    private Tienda tienda;

    private LocalDateTime fechaFabricacion;

    private LocalDateTime fechaVenta;

    // true cuando el cliente ya uso su verificacion unica
    @Column(nullable = false)
    private boolean verificado;

    private LocalDateTime fechaVerificacion;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getCodigoFabrica() { return codigoFabrica; }

    public void setCodigoFabrica(String codigoFabrica) { this.codigoFabrica = codigoFabrica; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public Modelo getModelo() { return modelo; }

    public void setModelo(Modelo modelo) { this.modelo = modelo; }

    public Estado getEstado() { return estado; }

    public void setEstado(Estado estado) { this.estado = estado; }

    public Tienda getTienda() { return tienda; }

    public void setTienda(Tienda tienda) { this.tienda = tienda; }

    public LocalDateTime getFechaFabricacion() { return fechaFabricacion; }

    public void setFechaFabricacion(LocalDateTime fechaFabricacion) { this.fechaFabricacion = fechaFabricacion; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }

    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public boolean isVerificado() { return verificado; }

    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public LocalDateTime getFechaVerificacion() { return fechaVerificacion; }

    public void setFechaVerificacion(LocalDateTime fechaVerificacion) { this.fechaVerificacion = fechaVerificacion; }

}
