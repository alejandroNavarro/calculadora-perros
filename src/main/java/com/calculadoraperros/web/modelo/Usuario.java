package com.calculadoraperros.web.modelo;

import java.sql.Timestamp;

/**
 * Clase de modelo (POJO) que representa un usuario en el sistema.
 * Contiene los atributos de un usuario y sus métodos getter/setter.
 */
public class Usuario {
    private int idUsuario;
    private String nombre;
    private String email;
    private String password; // En una aplicación real, esta debería ser la contraseña hasheada
    private Timestamp fechaRegistro;
    private String rol; // ¡NUEVO! Campo para el rol del usuario

    // Constructor vacío
    public Usuario() {
    }

    // Constructor con todos los campos (útil para recuperar de la BD)
    public Usuario(int idUsuario, String nombre, String email, String password, Timestamp fechaRegistro, String rol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.fechaRegistro = fechaRegistro;
        this.rol = rol; // Asigna el rol
    }

    // Constructor para crear un nuevo usuario (sin id_usuario ni fecha_registro iniciales)
    // Se asume un rol por defecto si no se especifica explícitamente al crear un nuevo usuario.
    public Usuario(String nombre, String email, String password) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = "user"; // Rol por defecto al registrar un nuevo usuario
    }

    // Constructor para crear un nuevo usuario con rol explícito
    public Usuario(String nombre, String email, String password, String rol) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
    }

    // --- Getters y Setters ---
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    // ¡NUEVO! Getter y Setter para el rol
    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" + // No mostrar la contraseña real en toString
                ", fechaRegistro=" + fechaRegistro +
                ", rol='" + rol + '\'' + // Incluye el rol en el toString
                '}';
    }
}
