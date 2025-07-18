package com.calculadoraperros.web.modelo;

import java.util.Date; // Usar java.util.Date para la fecha de nacimiento
import java.sql.Timestamp;

/**
 * Clase de modelo para representar una Mascota.
 * Contiene información sobre la mascota.
 */
public class Mascota {
    private int idMascota;
    private int idUsuario;
    private String nombre;
    private String sexo; // Nuevo campo: Sexo
    private Date fechaNacimiento; // Cambiado a java.util.Date
    private String raza;
    private double pesoKg;
    private boolean esterilizado;
    private Timestamp fechaRegistro;
    private String tipo; // "Perro", "Gato"
    private String nivelActividad; // "Bajo", "Normal", "Activo", "Muy Activo"
    // Valores de condicionSalud: "Saludable", "Obesidad", "Bajo Peso", "Cachorro", "Gestacion Temprana",
    // "Gestacion Tardia", "Lactancia Inicial", "Lactancia Pico", "Lactancia Tardia", "Senior", "Enfermedad"
    private String condicionSalud;

    // Constructor por defecto
    public Mascota() {
    }

    // Constructor completo (sin idMascota y fechaRegistro, ya que se generan automáticamente)
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
                '}';
    }
}
