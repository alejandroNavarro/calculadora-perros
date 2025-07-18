package com.calculadoraperros.web.modelo;

import java.util.Date; // Usar java.util.Date para el modelo
import java.sql.Timestamp;

public class Dosis {
    private int idDosis;
    private int idMascota;
    private String tipoMedicamento; // Renombrado de nombreVacuna
    private double cantidad; // Añadido: Cantidad de dosis
    private String unidad;   // Añadido: Unidad de dosis (ml, mg, etc.)
    private String frecuencia; // Añadido: Frecuencia de administración
    private Date fechaAdministracion; // Cambiado a java.util.Date
    private String notas; // Renombrado de observaciones
    private Timestamp fechaRegistro;

    // Constructor vacío
    public Dosis() {
    }

    // Constructor con todos los campos (útil para recuperar de la DB)
    public Dosis(int idDosis, int idMascota, String tipoMedicamento, double cantidad, String unidad, String frecuencia, Date fechaAdministracion, String notas, Timestamp fechaRegistro) {
        this.idDosis = idDosis;
        this.idMascota = idMascota;
        this.tipoMedicamento = tipoMedicamento;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.frecuencia = frecuencia;
        this.fechaAdministracion = fechaAdministracion;
        this.notas = notas;
        this.fechaRegistro = fechaRegistro;
    }

    // Constructor sin idDosis y fechaRegistro (útil para crear nuevas dosis desde el formulario)
    public Dosis(int idMascota, String tipoMedicamento, double cantidad, String unidad, String frecuencia, Date fechaAdministracion, String notas) {
        this.idMascota = idMascota;
        this.tipoMedicamento = tipoMedicamento;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.frecuencia = frecuencia;
        this.fechaAdministracion = fechaAdministracion;
        this.notas = notas;
        // fechaRegistro no se inicializa aquí, se podría asignar en el DAO o dejar que la DB lo genere
    }

    // Getters y Setters

    public int getIdDosis() {
        return idDosis;
    }

    public void setIdDosis(int idDosis) {
        this.idDosis = idDosis;
    }

    public int getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(int idMascota) {
        this.idMascota = idMascota;
    }

    public String getTipoMedicamento() {
        return tipoMedicamento;
    }

    public void setTipoMedicamento(String tipoMedicamento) {
        this.tipoMedicamento = tipoMedicamento;
    }

    // Getters y Setters para cantidad, unidad y frecuencia
    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Date getFechaAdministracion() {
        return fechaAdministracion;
    }

    public void setFechaAdministracion(Date fechaAdministracion) {
        this.fechaAdministracion = fechaAdministracion;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Timestamp getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Timestamp fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public String toString() {
        return "Dosis{" +
                "idDosis=" + idDosis +
                ", idMascota=" + idMascota +
                ", tipoMedicamento='" + tipoMedicamento + '\'' +
                ", cantidad=" + cantidad +
                ", unidad='" + unidad + '\'' +
                ", frecuencia='" + frecuencia + '\'' +
                ", fechaAdministracion=" + fechaAdministracion +
                ", notas='" + notas + '\'' +
                ", fechaRegistro=" + fechaRegistro +
                '}';
    }
}
