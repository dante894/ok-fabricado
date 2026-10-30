package com.egg.autenticidad.entidades;

import com.egg.autenticidad.entidades.enumeraciones.Resultado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/**
 * Bitacora: cada intento de verificacion queda registrado (bueno o sospechoso).
 * De aqui sale la pantalla de alertas de la marca.
 */
@Entity
public class Escaneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fecha;

    // Solo los primeros caracteres: no guardamos el token completo en la bitacora
    @Column(length = 24)
    private String tokenParcial;

    // null si el token no existia
    private String codigoFabrica;

    @Enumerated(EnumType.STRING)
    private Resultado resultado;

    @Column(length = 64)
    private String ip;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }

    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getTokenParcial() { return tokenParcial; }

    public void setTokenParcial(String tokenParcial) { this.tokenParcial = tokenParcial; }

    public String getCodigoFabrica() { return codigoFabrica; }

    public void setCodigoFabrica(String codigoFabrica) { this.codigoFabrica = codigoFabrica; }

    public Resultado getResultado() { return resultado; }

    public void setResultado(Resultado resultado) { this.resultado = resultado; }

    public String getIp() { return ip; }

    public void setIp(String ip) { this.ip = ip; }

}
