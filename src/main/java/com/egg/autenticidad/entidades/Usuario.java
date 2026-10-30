package com.egg.autenticidad.entidades;

import com.egg.autenticidad.entidades.enumeraciones.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Persona que entra al sistema (admin, fabrica o cajero de tienda).
 * El cliente que verifica un producto NO necesita usuario.
 */
@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(nullable = false, unique = true)
    private String email;

    // Se guarda cifrada con BCrypt, nunca en texto plano
    private String password;

    // STRING guarda "ADMIN" y no el numero 0 (mas seguro si cambias el orden del enum)
    @Enumerated(EnumType.STRING)
    private Rol rol;

    // Solo el cajero tiene tienda asignada; el resto queda en null
    @ManyToOne
    private Tienda tienda;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public Rol getRol() { return rol; }

    public void setRol(Rol rol) { this.rol = rol; }

    public Tienda getTienda() { return tienda; }

    public void setTienda(Tienda tienda) { this.tienda = tienda; }

}
