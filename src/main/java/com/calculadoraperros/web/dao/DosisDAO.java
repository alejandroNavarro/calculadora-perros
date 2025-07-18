package com.calculadoraperros.web.dao;

import com.calculadoraperros.web.modelo.Dosis;
import com.calculadoraperros.web.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Statement; // Necesario para Statement.RETURN_GENERATED_KEYS si se usa, aunque no en este DAO directamente.
import java.sql.Timestamp; // Necesario para fecha_registro

/**
 * Clase DAO (Data Access Object) para la entidad Dosis.
 * Proporciona métodos para interactuar con la tabla 'dosis' en la base de datos.
 */
public class DosisDAO {

    // Nombres de las tablas y columnas (para evitar errores de escritura y mejorar consistencia)
    private static final String TABLA_DOSIS = "dosis";
    private static final String COL_ID_DOSIS = "id_dosis";
    private static final String COL_ID_MASCOTA = "id_mascota";
    private static final String COL_TIPO_MEDICAMENTO = "tipo_medicamento"; // Renombrado para consistencia con el modelo
    private static final String COL_CANTIDAD = "cantidad";
    private static final String COL_UNIDAD = "unidad";
    private static final String COL_FRECUENCIA = "frecuencia";
    private static final String COL_FECHA_ADMINISTRACION = "fecha_administracion";
    private static final String COL_NOTAS = "notas"; // Renombrado para consistencia con el modelo
    private static final String COL_FECHA_REGISTRO = "fecha_registro";


    // Consultas SQL - ACTUALIZADAS para usar COL_TIPO_MEDICAMENTO y COL_NOTAS
    private static final String INSERT_DOSIS_SQL = "INSERT INTO " + TABLA_DOSIS + " (" + COL_ID_MASCOTA + ", " + COL_TIPO_MEDICAMENTO + ", " + COL_CANTIDAD + ", " + COL_UNIDAD + ", " + COL_FRECUENCIA + ", " + COL_FECHA_ADMINISTRACION + ", " + COL_NOTAS + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_DOSIS_BY_ID = "SELECT " + COL_ID_DOSIS + ", " + COL_ID_MASCOTA + ", " + COL_TIPO_MEDICAMENTO + ", " + COL_CANTIDAD + ", " + COL_UNIDAD + ", " + COL_FRECUENCIA + ", " + COL_FECHA_ADMINISTRACION + ", " + COL_NOTAS + ", " + COL_FECHA_REGISTRO + " FROM " + TABLA_DOSIS + " WHERE " + COL_ID_DOSIS + " = ?";
    private static final String SELECT_ALL_DOSIS_BY_MASCOTA_ID = "SELECT " + COL_ID_DOSIS + ", " + COL_ID_MASCOTA + ", " + COL_TIPO_MEDICAMENTO + ", " + COL_CANTIDAD + ", " + COL_UNIDAD + ", " + COL_FRECUENCIA + ", " + COL_FECHA_ADMINISTRACION + ", " + COL_NOTAS + ", " + COL_FECHA_REGISTRO + " FROM " + TABLA_DOSIS + " WHERE " + COL_ID_MASCOTA + " = ? ORDER BY " + COL_FECHA_ADMINISTRACION + " DESC";
    private static final String DELETE_DOSIS_SQL = "DELETE FROM " + TABLA_DOSIS + " WHERE " + COL_ID_DOSIS + " = ?";
    private static final String UPDATE_DOSIS_SQL = "UPDATE " + TABLA_DOSIS + " SET " + COL_TIPO_MEDICAMENTO + " = ?, " + COL_CANTIDAD + " = ?, " + COL_UNIDAD + " = ?, " + COL_FRECUENCIA + " = ?, " + COL_FECHA_ADMINISTRACION + " = ?, " + COL_NOTAS + " = ? WHERE " + COL_ID_DOSIS + " = ?";


    /**
     * Inserta una nueva dosis en la base de datos.
     * @param dosis El objeto Dosis a insertar.
     * @return true si la inserción fue exitosa.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean insertarDosis(Dosis dosis) throws SQLException {
        boolean rowInserted = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_DOSIS_SQL)) {
            statement.setInt(1, dosis.getIdMascota());
            statement.setString(2, dosis.getTipoMedicamento());
            statement.setDouble(3, dosis.getCantidad());
            statement.setString(4, dosis.getUnidad());
            statement.setString(5, dosis.getFrecuencia());
            statement.setDate(6, new java.sql.Date(dosis.getFechaAdministracion().getTime()));
            statement.setString(7, dosis.getNotas());

            rowInserted = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción para que el llamador la capture
        }
        return rowInserted;
    }

    /**
     * Obtiene todas las dosis registradas para una mascota específica.
     * @param idMascota El ID de la mascota.
     * @return Una lista de objetos Dosis.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public List<Dosis> obtenerDosisPorMascota(int idMascota) throws SQLException {
        List<Dosis> dosisList = new ArrayList<>();
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_DOSIS_BY_MASCOTA_ID)) {
            statement.setInt(1, idMascota);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Dosis dosis = new Dosis(
                        resultSet.getInt(COL_ID_DOSIS),
                        resultSet.getInt(COL_ID_MASCOTA),
                        resultSet.getString(COL_TIPO_MEDICAMENTO), // Usar el nuevo nombre de columna
                        resultSet.getDouble(COL_CANTIDAD),
                        resultSet.getString(COL_UNIDAD),
                        resultSet.getString(COL_FRECUENCIA),
                        resultSet.getDate(COL_FECHA_ADMINISTRACION),
                        resultSet.getString(COL_NOTAS), // Usar el nuevo nombre de columna
                        resultSet.getTimestamp(COL_FECHA_REGISTRO)
                    );
                    dosisList.add(dosis);
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción
        }
        return dosisList;
    }

    /**
     * Obtiene una dosis específica por su ID.
     * @param idDosis El ID de la dosis.
     * @return El objeto Dosis si se encuentra, o null en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public Dosis obtenerDosisPorId(int idDosis) throws SQLException {
        String sql = SELECT_DOSIS_BY_ID; // Usar la constante SQL
        Dosis dosis = null;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idDosis);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    dosis = new Dosis(
                        resultSet.getInt(COL_ID_DOSIS),
                        resultSet.getInt(COL_ID_MASCOTA),
                        resultSet.getString(COL_TIPO_MEDICAMENTO), // Usar el nuevo nombre de columna
                        resultSet.getDouble(COL_CANTIDAD),
                        resultSet.getString(COL_UNIDAD),
                        resultSet.getString(COL_FRECUENCIA),
                        resultSet.getDate(COL_FECHA_ADMINISTRACION),
                        resultSet.getString(COL_NOTAS), // Usar el nuevo nombre de columna
                        resultSet.getTimestamp(COL_FECHA_REGISTRO)
                    );
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción
        }
        return dosis;
    }

    /**
     * Elimina una dosis por su ID.
     * @param idDosis El ID de la dosis a eliminar.
     * @return true si la eliminación fue exitosa.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean eliminarDosis(int idDosis) throws SQLException {
        boolean rowDeleted = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_DOSIS_SQL)) {
            statement.setInt(1, idDosis);
            rowDeleted = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción
        }
        return rowDeleted;
    }

    /**
     * Actualiza una dosis existente en la base de datos.
     * @param dosis El objeto Dosis con los datos actualizados.
     * @return true si la actualización fue exitosa.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean actualizarDosis(Dosis dosis) throws SQLException {
        boolean rowUpdated = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_DOSIS_SQL)) {
            statement.setString(1, dosis.getTipoMedicamento());
            statement.setDouble(2, dosis.getCantidad());
            statement.setString(3, dosis.getUnidad());
            statement.setString(4, dosis.getFrecuencia());
            statement.setDate(5, new java.sql.Date(dosis.getFechaAdministracion().getTime()));
            statement.setString(6, dosis.getNotas());
            statement.setInt(7, dosis.getIdDosis());

            rowUpdated = statement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción
        }
        return rowUpdated;
    }

    /**
     * Imprime información detallada de una SQLException.
     * @param ex La SQLException a imprimir.
     */
    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                String sqlState = ((SQLException) e).getSQLState();
                if (!ignoreSQLException(sqlState)) {
                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " + (sqlState != null ? sqlState : "N/A"));
                    System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
                    System.err.println("Message: " + e.getMessage());
                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    /**
     * Ignora ciertas SQLStates que no son errores críticos (por ejemplo, advertencias).
     * @param sqlState El SQLState a verificar.
     * @return true si el SQLState debe ser ignorado, false en caso contrario.
     */
    private boolean ignoreSQLException(String sqlState) {
        if (sqlState == null) {
            return false;
        }
        if (sqlState.equalsIgnoreCase("02000")) { // NO DATA
            return true;
        }
        return false;
    }
}
