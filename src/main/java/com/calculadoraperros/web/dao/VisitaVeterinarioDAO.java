package com.calculadoraperros.web.dao;

import com.calculadoraperros.web.modelo.VisitaVeterinario;
import com.calculadoraperros.web.util.ConexionDB; // Asegúrate de que esta clase exista y funcione

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Clase DAO (Data Access Object) para la entidad VisitaVeterinario.
 * Proporciona métodos para interactuar con la tabla 'visitas_veterinario'
 * en la base de datos.
 */
public class VisitaVeterinarioDAO {

    /**
     * Inserta una nueva visita al veterinario en la base de datos.
     *
     * @param visita Objeto VisitaVeterinario a insertar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean insertarVisita(VisitaVeterinario visita) throws SQLException {
        String sql = "INSERT INTO visitas_veterinario (id_mascota, fecha_visita, motivo, diagnostico, " +
                     "tratamiento, medicamentos_recetados, costo, observaciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        boolean rowInserted = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, visita.getIdMascota());
            statement.setDate(2, new java.sql.Date(visita.getFechaVisita().getTime()));
            statement.setString(3, visita.getMotivo());
            statement.setString(4, visita.getDiagnostico());
            statement.setString(5, visita.getTratamiento());
            statement.setString(6, visita.getMedicamentosRecetados());
            statement.setBigDecimal(7, visita.getCosto());
            statement.setString(8, visita.getObservaciones());

            rowInserted = statement.executeUpdate() > 0;
        }
        return rowInserted;
    }

    /**
     * Obtiene una visita al veterinario por su ID.
     *
     * @param idVisita ID de la visita a obtener.
     * @return Objeto VisitaVeterinario si se encuentra, o null si no.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public VisitaVeterinario obtenerVisitaPorId(int idVisita) throws SQLException {
        VisitaVeterinario visita = null;
        String sql = "SELECT * FROM visitas_veterinario WHERE id_visita = ?";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idVisita);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                visita = new VisitaVeterinario();
                visita.setIdVisita(resultSet.getInt("id_visita"));
                visita.setIdMascota(resultSet.getInt("id_mascota"));
                visita.setFechaVisita(resultSet.getDate("fecha_visita"));
                visita.setMotivo(resultSet.getString("motivo"));
                visita.setDiagnostico(resultSet.getString("diagnostico"));
                visita.setTratamiento(resultSet.getString("tratamiento"));
                visita.setMedicamentosRecetados(resultSet.getString("medicamentos_recetados"));
                visita.setCosto(resultSet.getBigDecimal("costo"));
                visita.setObservaciones(resultSet.getString("observaciones"));
            }
        }
        return visita;
    }

    /**
     * Obtiene todas las visitas de una mascota específica.
     *
     * @param idMascota ID de la mascota.
     * @return Lista de objetos VisitaVeterinario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public List<VisitaVeterinario> obtenerVisitasPorMascota(int idMascota) throws SQLException {
        List<VisitaVeterinario> visitas = new ArrayList<>();
        String sql = "SELECT * FROM visitas_veterinario WHERE id_mascota = ? ORDER BY fecha_visita DESC";
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idMascota);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                VisitaVeterinario visita = new VisitaVeterinario();
                visita.setIdVisita(resultSet.getInt("id_visita"));
                visita.setIdMascota(resultSet.getInt("id_mascota"));
                visita.setFechaVisita(resultSet.getDate("fecha_visita"));
                visita.setMotivo(resultSet.getString("motivo"));
                visita.setDiagnostico(resultSet.getString("diagnostico"));
                visita.setTratamiento(resultSet.getString("tratamiento"));
                visita.setMedicamentosRecetados(resultSet.getString("medicamentos_recetados"));
                visita.setCosto(resultSet.getBigDecimal("costo"));
                visita.setObservaciones(resultSet.getString("observaciones"));
                visitas.add(visita);
            }
        }
        return visitas;
    }

    /**
     * Actualiza una visita al veterinario existente en la base de datos.
     *
     * @param visita Objeto VisitaVeterinario con los datos actualizados.
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean actualizarVisita(VisitaVeterinario visita) throws SQLException {
        String sql = "UPDATE visitas_veterinario SET fecha_visita = ?, motivo = ?, diagnostico = ?, " +
                     "tratamiento = ?, medicamentos_recetados = ?, costo = ?, observaciones = ? " +
                     "WHERE id_visita = ? AND id_mascota = ?"; // Aseguramos que la visita pertenece a la mascota correcta
        boolean rowUpdated = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, new java.sql.Date(visita.getFechaVisita().getTime()));
            statement.setString(2, visita.getMotivo());
            statement.setString(3, visita.getDiagnostico());
            statement.setString(4, visita.getTratamiento());
            statement.setString(5, visita.getMedicamentosRecetados());
            statement.setBigDecimal(6, visita.getCosto());
            statement.setString(7, visita.getObservaciones());
            statement.setInt(8, visita.getIdVisita());
            statement.setInt(9, visita.getIdMascota());

            rowUpdated = statement.executeUpdate() > 0;
        }
        return rowUpdated;
    }

    /**
     * Elimina una visita al veterinario de la base de datos por su ID.
     *
     * @param idVisita ID de la visita a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    public boolean eliminarVisita(int idVisita) throws SQLException {
        String sql = "DELETE FROM visitas_veterinario WHERE id_visita = ?";
        boolean rowDeleted = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idVisita);
            rowDeleted = statement.executeUpdate() > 0;
        }
        return rowDeleted;
    }
}
