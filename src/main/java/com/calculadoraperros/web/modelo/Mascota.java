package com.calculadoraperros.web.modelo;

import java.util.Date;
import java.sql.Timestamp;

/**
 * Clase de modelo para la entidad Mascota.
 * Representa la información de una mascota en el sistema.
 * Incluye campos para la Fase 2 (color, chipID, observaciones)
 * y nuevos campos para el cálculo de comida (objetivoPeso, estadoReproductor,
 * numCachorros, tipoAlimentoPredeterminado, kcalPor100gAlimentoPredeterminado).
 */
public class Mascota {
    private int idMascota;
    private int idUsuario;
    private String nombre;
    private String sexo;
    private Date fechaNacimiento; // java.util.Date
    private String raza;
    private double peso; // peso_kg
    private boolean esterilizado;
    private Timestamp fechaRegistro; // java.sql.Timestamp
    private String tipo; // Perro, Gato, etc.
    private String nivelActividad; // Bajo, Moderado, Alto
    private String condicionSalud; // Normal, Sobrepeso, Bajo peso, Enfermo
    private String imagen; // Ruta o nombre del archivo de imagen

    // --- Nuevos campos para Fase 2 ---
    private String color;
    private String chipID;
    private String observaciones;

    // --- Nuevos campos para cálculo de comida y estado reproductivo ---
    private String objetivoPeso; // Ej: "Mantener", "Ganar", "Perder"
    private String estadoReproductor; // Ej: "No Reproductor", "Gestante", "Lactante", "Cachorro"
    private Integer numCachorros; // Solo si es lactante, puede ser null
    private String tipoAlimentoPredeterminado; // Ej: "Pienso Seco", "Comida Húmeda", "Dieta BARF"
    private Double kcalPor100gAlimentoPredeterminado; // Kcal por 100g del alimento predeterminado, puede ser null

    /**
     * Constructor por defecto.
     */
    public Mascota() {
    }

    /**
     * Constructor para insertar una nueva mascota (sin idMascota ni fechaRegistro, que son auto-generados).
     * Incluye todos los campos de la Fase 2 y los nuevos campos de cálculo de comida.
     */
    public Mascota(int idUsuario, String nombre, String sexo, Date fechaNacimiento, String raza,
                   double peso, boolean esterilizado, String tipo, String nivelActividad,
                   String condicionSalud, String imagen, String color, String chipID, String observaciones,
                   String objetivoPeso, String estadoReproductor, Integer numCachorros,
                   String tipoAlimentoPredeterminado, Double kcalPor100gAlimentoPredeterminado) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.raza = raza;
        this.peso = peso;
        this.esterilizado = esterilizado;
        this.tipo = tipo;
        this.nivelActividad = nivelActividad;
        this.condicionSalud = condicionSalud;
        this.imagen = imagen;
        this.color = color;
        this.chipID = chipID;
        this.observaciones = observaciones;
        this.objetivoPeso = objetivoPeso;
        this.estadoReproductor = estadoReproductor;
        this.numCachorros = numCachorros;
        this.tipoAlimentoPredeterminado = tipoAlimentoPredeterminado;
        this.kcalPor100gAlimentoPredeterminado = kcalPor100gAlimentoPredeterminado;
    }

    /**
     * Constructor completo para recuperar una mascota de la base de datos.
     * Incluye todos los campos, incluyendo idMascota y fechaRegistro, Fase 2 y nuevos campos de cálculo.
     */
    public Mascota(int idMascota, int idUsuario, String nombre, String sexo, Date fechaNacimiento,
                   String raza, double peso, boolean esterilizado, Timestamp fechaRegistro, String tipo,
                   String nivelActividad, String condicionSalud, String imagen, String color,
                   String chipID, String observaciones, String objetivoPeso, String estadoReproductor,
                   Integer numCachorros, String tipoAlimentoPredeterminado, Double kcalPor100gAlimentoPredeterminado) {
        this.idMascota = idMascota;
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.sexo = sexo;
        this.fechaNacimiento = fechaNacimiento;
        this.raza = raza;
        this.peso = peso;
        this.esterilizado = esterilizado;
        this.fechaRegistro = fechaRegistro;
        this.tipo = tipo;
        this.nivelActividad = nivelActividad;
        this.condicionSalud = condicionSalud;
        this.imagen = imagen;
        this.color = color;
        this.chipID = chipID;
        this.observaciones = observaciones;
        this.objetivoPeso = objetivoPeso;
        this.estadoReproductor = estadoReproductor;
        this.numCachorros = numCachorros;
        this.tipoAlimentoPredeterminado = tipoAlimentoPredeterminado;
        this.kcalPor100gAlimentoPredeterminado = kcalPor100gAlimentoPredeterminado;
    }

    // --- Getters y Setters para todos los campos ---

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

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
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

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    // --- Getters y Setters para campos de Fase 2 ---
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getChipID() {
        return chipID;
    }

    public void setChipID(String chipID) {
        this.chipID = chipID;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // --- Getters y Setters para nuevos campos de cálculo de comida ---
    public String getObjetivoPeso() {
        return objetivoPeso;
    }

    public void setObjetivoPeso(String objetivoPeso) {
        this.objetivoPeso = objetivoPeso;
    }

    public String getEstadoReproductor() {
        return estadoReproductor;
    }

    public void setEstadoReproductor(String estadoReproductor) {
        this.estadoReproductor = estadoReproductor;
    }

    public Integer getNumCachorros() {
        return numCachorros;
    }

    public void setNumCachorros(Integer numCachorros) {
        this.numCachorros = numCachorros;
    }

    public String getTipoAlimentoPredeterminado() {
        return tipoAlimentoPredeterminado;
    }

    public void setTipoAlimentoPredeterminado(String tipoAlimentoPredeterminado) {
        this.tipoAlimentoPredeterminado = tipoAlimentoPredeterminado;
    }

    public Double getKcalPor100gAlimentoPredeterminado() {
        return kcalPor100gAlimentoPredeterminado;
    }

    public void setKcalPor100gAlimentoPredeterminado(Double kcalPor100gAlimentoPredeterminado) {
        this.kcalPor100gAlimentoPredeterminado = kcalPor100gAlimentoPredeterminado;
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
               ", peso=" + peso +
               ", esterilizado=" + esterilizado +
               ", fechaRegistro=" + fechaRegistro +
               ", tipo='" + tipo + '\'' +
               ", nivelActividad='" + nivelActividad + '\'' +
               ", condicionSalud='" + condicionSalud + '\'' +
               ", imagen='" + imagen + '\'' +
               ", color='" + color + '\'' +
               ", chipID='" + chipID + '\'' +
               ", observaciones='" + observaciones + '\'' +
               ", objetivoPeso='" + objetivoPeso + '\'' +
               ", estadoReproductor='" + estadoReproductor + '\'' +
               ", numCachorros=" + numCachorros +
               ", tipoAlimentoPredeterminado='" + tipoAlimentoPredeterminado + '\'' +
               ", kcalPor100gAlimentoPredeterminado=" + kcalPor100gAlimentoPredeterminado +
               '}';
    }
}
