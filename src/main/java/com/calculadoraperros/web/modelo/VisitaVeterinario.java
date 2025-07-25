package com.calculadoraperros.web.modelo;

import java.math.BigDecimal; // Para manejar el costo con precisión
import java.util.Date;     // Para la fecha de la visita

/**
 * Clase de modelo que representa una visita al veterinario.
 * Contiene los atributos de una visita, junto con sus constructores,
 * getters y setters.
 */
public class VisitaVeterinario {
    private int idVisita;
    private int idMascota;
    private Date fechaVisita;
    private String motivo;
    private String diagnostico;
    private String tratamiento;
    private String medicamentosRecetados;
    private BigDecimal costo; // Usamos BigDecimal para manejar dinero con precisión
    private String observaciones;

    // Constructor vacío
    public VisitaVeterinario() {
    }

    // Constructor con todos los campos (útil para recuperar de DB)
    public VisitaVeterinario(int idVisita, int idMascota, Date fechaVisita, String motivo,
                             String diagnostico, String tratamiento, String medicamentosRecetados,
                             BigDecimal costo, String observaciones) {
        this.idVisita = idVisita;
        this.idMascota = idMascota;
        this.fechaVisita = fechaVisita;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.medicamentosRecetados = medicamentosRecetados;
        this.costo = costo;
        this.observaciones = observaciones;
    }

    // Constructor sin idVisita (útil para insertar nuevas visitas)
    public VisitaVeterinario(int idMascota, Date fechaVisita, String motivo,
                             String diagnostico, String tratamiento, String medicamentosRecetados,
                             BigDecimal costo, String observaciones) {
        this.idMascota = idMascota;
        this.fechaVisita = fechaVisita;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.medicamentosRecetados = medicamentosRecetados;
        this.costo = costo;
        this.observaciones = observaciones;
    }

    // --- Getters y Setters ---

    public int getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(int idVisita) {
        this.idVisita = idVisita;
    }

    public int getIdMascota() {
        return idMascota;
    }

    public void setIdMascota(int idMascota) {
        this.idMascota = idMascota;
    }

    public Date getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(Date fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getMedicamentosRecetados() {
        return medicamentosRecetados;
    }

    public void setMedicamentosRecetados(String medicamentosRecetados) {
        this.medicamentosRecetados = medicamentosRecetados;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "VisitaVeterinario{" +
               "idVisita=" + idVisita +
               ", idMascota=" + idMascota +
               ", fechaVisita=" + fechaVisita +
               ", motivo='" + motivo + '\'' +
               ", diagnostico='" + diagnostico + '\'' +
               ", tratamiento='" + tratamiento + '\'' +
               ", medicamentosRecetados='" + medicamentosRecetados + '\'' +
               ", costo=" + costo +
               ", observaciones='" + observaciones + '\'' +
               '}';
    }
}
