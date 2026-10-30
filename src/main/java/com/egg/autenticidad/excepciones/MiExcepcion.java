package com.egg.autenticidad.excepciones;

/** Error de negocio con un mensaje que se le puede mostrar al usuario (igual que en biblioteca). */
public class MiExcepcion extends Exception {

    public MiExcepcion(String mensaje) {
        super(mensaje);
    }
}
