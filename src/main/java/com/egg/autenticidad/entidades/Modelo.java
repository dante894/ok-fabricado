package com.egg.autenticidad.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Tipo de producto: "Bolso Aurora", "Cartera Milano"...
 * @Entity le dice a JPA/Hibernate que esta clase es una tabla (igual que Autor en biblioteca).
 */
@Entity
public class Modelo {

    // IDENTITY: la base de datos asigna 1, 2, 3...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

}
