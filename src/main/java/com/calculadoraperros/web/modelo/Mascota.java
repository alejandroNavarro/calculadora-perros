package com.calculadoraperros.web.modelo;

import java.util.Date;
import java.sql.Timestamp;

/**
 * Clase de modelo para representar una Mascota.
 * Contiene información sobre la mascota.
 */
public class Mascota {
    private int idMascota;
    private int idUsuario;
    private String nombre;
    private String sexo;
    private Date fechaNacimiento;
    private String raza;
    private double pesoKg;
    private boolean esterilizado;
    private Timestamp fechaRegistro;
    private String tipo;
    private String nivelActividad;
    private String condicionSalud;
    private String imagen; // Campo para almacenar el nombre del archivo de la imagen

    // Constructor por defecto
    public Mascota() {
    }

    // Constructor completo (sin idMascota y fechaRegistro)
    public Mascota(int idUsuario, String nombre, String sexo, Date fechaNacimiento, String raza, double pesoKg, boolean esterilizado, String tipo, String nivelActividad, String condicionSalud) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.raza = raza;
        this.pesoKg = pesoKg;
        this.esterilizado = esterilizado;
        this.tipo = tipo;
        this.nivelActividad = nivelActividad;
        this.condicionSalud = condicionSalud;
    }

    // Constructor completo con campo imagen
    public Mascota(int idUsuario, String nombre, String sexo, Date fechaNacimiento, String raza, double pesoKg, boolean esterilizado, String tipo, String nivelActividad, String condicionSalud, String imagen) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.raza = raza;
        this.pesoKg = pesoKg;
        this.esterilizado = esterilizado;
        this.tipo = tipo;
        this.nivelActividad = nivelActividad;
        this.condicionSalud = condicionSalud;
        this.imagen = imagen;
    }

    // --- Getters y Setters ---

    public int getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(int idMascota) {
        this.idMascota = idMascota;
    }

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

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public boolean isEsterilizado() {
        return esterilizado;
    }

    public void setEsterilizado(boolean esterilizado) {
        this.esterilizado = esterilizado;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNivelActividad() {
        return nivelActividad;
    }

    public void setNivelActividad(String nivelActividad) {
        this.nivelActividad = nivelActividad;
    }

    public String getCondicionSalud() {
        return condicionSalud;
    }

    public void setCondicionSalud(String condicionSalud) {
        this.condicionSalud = condicionSalud;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) { // Renombrado de 'foto' a 'imagen' para consistencia
        this.imagen = imagen;
    }

    @Override
    public String toString() {
        return "Mascota{" +
                "idMascota=" + idMascota +
                ", idUsuario=" + idUsuario +
                ", nombre='" + nombre + '\'' +
                ", sexo='" + sexo + '\'' +
                ", fechaNacimiento=" + fechaNacimiento +
                ", raza='" + raza + '\'' +
                ", pesoKg=" + pesoKg +
                ", esterilizado=" + esterilizado +
                ", fechaRegistro=" + fechaRegistro +
                ", tipo='" + tipo + '\'' +
                ", nivelActividad='" + nivelActividad + '\'' +
                ", condicionSalud='" + condicionSalud + '\'' +
                ", imagen='" + imagen + '\'' + // Actualizado para reflejar 'imagen'
                '}';
    }
}
