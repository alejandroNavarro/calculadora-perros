package com.calculadoraperros.web.dao;

import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.util.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date; // Importar java.sql.Date para mapear con Date de Java.util
import java.util.ArrayList;
import java.util.List;

/**
 * Clase DAO (Data Access Object) para la entidad Mascota.
 * Proporciona métodos para interactuar con la tabla 'mascotas' en la base de datos.
 */
public class MascotaDAO {

    // Nombres de las tablas y columnas
    private static final String TABLA_MASCOTAS = "mascotas";
    private static final String COL_ID_MASCOTA = "id_mascota";
    private static final String COL_ID_USUARIO = "id_usuario";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_SEXO = "sexo";
    private static final String COL_FECHA_NACIMIENTO = "fecha_nacimiento";
    private static final String COL_RAZA = "raza";
    private static final String COL_PESO_KG = "peso_kg";
    private static final String COL_ESTERILIZADO = "esterilizado";
    private static final String COL_FECHA_REGISTRO = "fecha_registro";
    private static final String COL_TIPO = "tipo";
    private static final String COL_NIVEL_ACTIVIDAD = "nivel_actividad";
    private static final String COL_CONDICION_SALUD = "condicion_salud";
    private static final String IMAGEN = "imagen"; // Campo de imagen

    // Consultas SQL
    private static final String INSERT_MASCOTA_SQL = "INSERT INTO " + TABLA_MASCOTAS + " (" + COL_ID_USUARIO + ", " + COL_NOMBRE + ", " + COL_SEXO + ", " + COL_FECHA_NACIMIENTO + ", " + COL_RAZA + ", " + COL_PESO_KG + ", " + COL_ESTERILIZADO + ", " + COL_TIPO + ", " + COL_NIVEL_ACTIVIDAD + ", " + COL_CONDICION_SALUD + ", " + IMAGEN + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_MASCOTA_BY_ID = "SELECT " + COL_ID_MASCOTA + ", " + COL_ID_USUARIO + ", " + COL_NOMBRE + ", " + COL_SEXO + ", " + COL_FECHA_NACIMIENTO + ", " + COL_RAZA + ", " + COL_PESO_KG + ", " + COL_ESTERILIZADO + ", " + COL_FECHA_REGISTRO + ", " + COL_TIPO + ", " + COL_NIVEL_ACTIVIDAD + ", " + COL_CONDICION_SALUD + ", " + IMAGEN + " FROM " + TABLA_MASCOTAS + " WHERE " + COL_ID_MASCOTA + " = ?";
    private static final String SELECT_ALL_MASCOTAS_BY_USER_ID = "SELECT " + COL_ID_MASCOTA + ", " + COL_ID_USUARIO + ", " + COL_NOMBRE + ", " + COL_SEXO + ", " + COL_FECHA_NACIMIENTO + ", " + COL_RAZA + ", " + COL_PESO_KG + ", " + COL_ESTERILIZADO + ", " + COL_FECHA_REGISTRO + ", " + COL_TIPO + ", " + COL_NIVEL_ACTIVIDAD + ", " + COL_CONDICION_SALUD + ", " + IMAGEN + " FROM " + TABLA_MASCOTAS + " WHERE " + COL_ID_USUARIO + " = ?";
    private static final String DELETE_MASCOTA_SQL = "DELETE FROM " + TABLA_MASCOTAS + " WHERE " + COL_ID_MASCOTA + " = ?";
    // La consulta UPDATE_MASCOTA_SQL es:
    // UPDATE mascotas SET nombre = ?, sexo = ?, fecha_nacimiento = ?, raza = ?, peso_kg = ?, esterilizado = ?, tipo = ?, nivel_actividad = ?, condicion_salud = ?, imagen = ? WHERE id_mascota = ?
    // Esto significa que los parámetros 1-9 son los campos de SET, el 10 es IMAGEN, y el 11 es ID_MASCOTA
    private static final String UPDATE_MASCOTA_SQL = "UPDATE " + TABLA_MASCOTAS + " SET " + COL_NOMBRE + " = ?, " + COL_SEXO + " = ?, " + COL_FECHA_NACIMIENTO + " = ?, " + COL_RAZA + " = ?, " + COL_PESO_KG + " = ?, " + COL_ESTERILIZADO + " = ?, " + COL_TIPO + " = ?, " + COL_NIVEL_ACTIVIDAD + " = ?, " + COL_CONDICION_SALUD + " = ?, " + IMAGEN + " = ? WHERE " + COL_ID_MASCOTA + " = ?";


    /**
     * Inserta una nueva mascota en la base de datos.
     * @param mascota Objeto Mascota con los datos a insertar.
     * @return true si la mascota fue insertada exitosamente, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public boolean insertarMascota(Mascota mascota) throws SQLException {
        boolean rowInserted = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_MASCOTA_SQL, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setInt(1, mascota.getIdUsuario());
            preparedStatement.setString(2, mascota.getNombre());
            preparedStatement.setString(3, mascota.getSexo());
            // Convertir java.util.Date a java.sql.Date para la base de datos
            preparedStatement.setDate(4, new Date(mascota.getFechaNacimiento().getTime()));
            preparedStatement.setString(5, mascota.getRaza());
            preparedStatement.setDouble(6, mascota.getPesoKg());
            preparedStatement.setBoolean(7, mascota.isEsterilizado());
            preparedStatement.setString(8, mascota.getTipo());
            preparedStatement.setString(9, mascota.getNivelActividad());
            preparedStatement.setString(10, mascota.getCondicionSalud()); // Este es el 10º parámetro
            preparedStatement.setString(11, mascota.getImagen());       // Este es el 11º parámetro (IMAGEN)

            rowInserted = preparedStatement.executeUpdate() > 0;

            if (rowInserted) {
                try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                    if (rs.next()) {
                        mascota.setIdMascota(rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción para que el llamador la capture
        }
        return rowInserted;
    }

    /**
     * Obtiene una mascota por su ID.
     * @param idMascota El ID de la mascota.
     * @return El objeto Mascota si se encuentra, null en caso contrario.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public Mascota obtenerMascotaPorId(int idMascota) throws SQLException {
        Mascota mascota = null;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_MASCOTA_BY_ID)) {

            preparedStatement.setInt(1, idMascota);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    mascota = new Mascota();
                    mascota.setIdMascota(rs.getInt(COL_ID_MASCOTA));
                    mascota.setIdUsuario(rs.getInt(COL_ID_USUARIO));
                    mascota.setNombre(rs.getString(COL_NOMBRE));
                    mascota.setSexo(rs.getString(COL_SEXO));
                    
                    // Convertir java.sql.Date a java.util.Date para evitar UnsupportedOperationException
                    java.sql.Date sqlDate = rs.getDate(COL_FECHA_NACIMIENTO);
                    if (sqlDate != null) {
                        mascota.setFechaNacimiento(new java.util.Date(sqlDate.getTime()));
                    } else {
                        mascota.setFechaNacimiento(null); // Manejar caso de fecha nula
                    }

                    mascota.setRaza(rs.getString(COL_RAZA));
                    mascota.setPesoKg(rs.getDouble(COL_PESO_KG));
                    mascota.setEsterilizado(rs.getBoolean(COL_ESTERILIZADO));
                    mascota.setFechaRegistro(rs.getTimestamp(COL_FECHA_REGISTRO));
                    mascota.setTipo(rs.getString(COL_TIPO));
                    mascota.setNivelActividad(rs.getString(COL_NIVEL_ACTIVIDAD));
                    mascota.setCondicionSalud(rs.getString(COL_CONDICION_SALUD));
                    mascota.setImagen(rs.getString(IMAGEN)); // CORRECCIÓN: Asignar a setImagen()
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción
        }
        return mascota;
    }

    /**
     * Selecciona todas las mascotas de un usuario específico.
     * @param idUsuario ID del usuario cuyas mascotas se quieren obtener.
     * @return Una lista de objetos Mascota.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public List<Mascota> obtenerTodasMascotasPorUsuario(int idUsuario) throws SQLException {
        List<Mascota> mascotas = new ArrayList<>();
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_MASCOTAS_BY_USER_ID)) {

            preparedStatement.setInt(1, idUsuario);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    Mascota mascota = new Mascota();
                    mascota.setIdMascota(rs.getInt(COL_ID_MASCOTA));
                    mascota.setIdUsuario(rs.getInt(COL_ID_USUARIO));
                    mascota.setNombre(rs.getString(COL_NOMBRE));
                    mascota.setSexo(rs.getString(COL_SEXO));
                    
                    // Convertir java.sql.Date a java.util.Date para evitar UnsupportedOperationException
                    java.sql.Date sqlDate = rs.getDate(COL_FECHA_NACIMIENTO);
                    if (sqlDate != null) {
                        mascota.setFechaNacimiento(new java.util.Date(sqlDate.getTime()));
                    } else {
                        mascota.setFechaNacimiento(null); // Manejar caso de fecha nula
                    }

                    mascota.setRaza(rs.getString(COL_RAZA));
                    mascota.setPesoKg(rs.getDouble(COL_PESO_KG));
                    mascota.setEsterilizado(rs.getBoolean(COL_ESTERILIZADO));
                    mascota.setFechaRegistro(rs.getTimestamp(COL_FECHA_REGISTRO));
                    mascota.setTipo(rs.getString(COL_TIPO));
                    mascota.setNivelActividad(rs.getString(COL_NIVEL_ACTIVIDAD));
                    mascota.setCondicionSalud(rs.getString(COL_CONDICION_SALUD));
                    mascota.setImagen(rs.getString(IMAGEN)); // CORRECCIÓN: Asignar a setImagen()
                    mascotas.add(mascota);
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción
        }
        return mascotas;
    }

    /**
     * Elimina una mascota de la base de datos.
     * @param idMascota ID de la mascota a eliminar.
     * @return true si la mascota fue eliminada exitosamente, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public boolean eliminarMascota(int idMascota) throws SQLException {
        boolean rowDeleted = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_MASCOTA_SQL)) {

            preparedStatement.setInt(1, idMascota);
            rowDeleted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // Relanza la excepción
        }
        return rowDeleted;
    }

    /**
     * Actualiza los datos de una mascota en la base de datos.
     * @param mascota Objeto Mascota con los datos actualizados.
     * @return true si la mascota fue actualizada exitosamente, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public boolean actualizarMascota(Mascota mascota) throws SQLException {
        boolean rowUpdated = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_MASCOTA_SQL)) {

            preparedStatement.setString(1, mascota.getNombre());
            preparedStatement.setString(2, mascota.getSexo());
            // Convertir java.util.Date a java.sql.Date para la base de datos
            preparedStatement.setDate(3, new Date(mascota.getFechaNacimiento().getTime()));
            preparedStatement.setString(4, mascota.getRaza());
            preparedStatement.setDouble(5, mascota.getPesoKg());
            preparedStatement.setBoolean(6, mascota.isEsterilizado());
            preparedStatement.setString(7, mascota.getTipo());
            preparedStatement.setString(8, mascota.getNivelActividad());
            preparedStatement.setString(9, mascota.getCondicionSalud());
            preparedStatement.setString(10, mascota.getImagen()); // CORRECCIÓN: IMAGEN es el 10º parámetro
            preparedStatement.setInt(11, mascota.getIdMascota()); // CORRECCIÓN: ID_MASCOTA es el 11º parámetro (WHERE clause)

            rowUpdated = preparedStatement.executeUpdate() > 0;
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
