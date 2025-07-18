package com.calculadoraperros.web.dao;

import com.calculadoraperros.web.modelo.Usuario;
import com.calculadoraperros.web.util.ConexionDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Clase DAO (Data Access Object) para la entidad Usuario.
 * Proporciona métodos para interactuar con la tabla 'usuarios' en la base de datos.
 */
public class UsuarioDAO {

    // Nombres de las tablas y columnas (para evitar errores de escritura)
    private static final String TABLA_USUARIOS = "usuarios";
    private static final String COL_ID_USUARIO = "id_usuario";
    private static final String COL_NOMBRE = "nombre";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_FECHA_REGISTRO = "fecha_registro";

    // Consultas SQL
    private static final String INSERT_USUARIO_SQL = "INSERT INTO " + TABLA_USUARIOS + " (" + COL_NOMBRE + ", " + COL_EMAIL + ", " + COL_PASSWORD + ") VALUES (?, ?, ?)";
    private static final String SELECT_USUARIO_BY_EMAIL_PASSWORD_SQL = "SELECT " + COL_ID_USUARIO + ", " + COL_NOMBRE + ", " + COL_EMAIL + ", " + COL_PASSWORD + ", " + COL_FECHA_REGISTRO + " FROM " + TABLA_USUARIOS + " WHERE " + COL_EMAIL + " = ? AND " + COL_PASSWORD + " = ?";
    private static final String SELECT_USUARIO_BY_EMAIL_SQL = "SELECT " + COL_ID_USUARIO + ", " + COL_NOMBRE + ", " + COL_EMAIL + ", " + COL_PASSWORD + ", " + COL_FECHA_REGISTRO + " FROM " + TABLA_USUARIOS + " WHERE " + COL_EMAIL + " = ?";


    /**
     * Inserta un nuevo usuario en la base de datos.
     * @param usuario Objeto Usuario con los datos a insertar.
     * @return true si el usuario fue insertado exitosamente.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public boolean insertarUsuario(Usuario usuario) throws SQLException { // <-- AÑADIDO: throws SQLException
        boolean rowInserted = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USUARIO_SQL)) {
            
            preparedStatement.setString(1, usuario.getNombre());
            preparedStatement.setString(2, usuario.getEmail());
            preparedStatement.setString(3, usuario.getPassword()); // En producción, aquí iría la contraseña hasheada
            rowInserted = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // <-- CAMBIO CLAVE: Relanza la excepción para que el llamador la capture
        }
        return rowInserted;
    }

    /**
     * Valida las credenciales de un usuario.
     * @param email Correo electrónico del usuario.
     * @param password Contraseña del usuario (sin hashear para esta versión simple).
     * @return Objeto Usuario si las credenciales son válidas, null en caso contrario.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public Usuario validarUsuario(String email, String password) throws SQLException { // <-- AÑADIDO: throws SQLException
        Usuario usuario = null;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USUARIO_BY_EMAIL_PASSWORD_SQL)) {
            
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password); // En producción, aquí se compararía con la contraseña hasheada
            System.out.println(preparedStatement); // Para depuración

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(COL_ID_USUARIO);
                    String nombre = rs.getString(COL_NOMBRE);
                    String userEmail = rs.getString(COL_EMAIL);
                    String userPassword = rs.getString(COL_PASSWORD);
                    Timestamp fechaRegistro = rs.getTimestamp(COL_FECHA_REGISTRO);
                    usuario = new Usuario(id, nombre, userEmail, userPassword, fechaRegistro);
                }
            }
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // <-- CAMBIO CLAVE: Relanza la excepción
        }
        return usuario;
    }

    /**
     * Verifica si un email ya existe en la base de datos.
     * @param email Correo electrónico a verificar.
     * @return true si el email ya existe, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL durante la operación.
     */
    public boolean existeEmail(String email) throws SQLException { // <-- AÑADIDO: throws SQLException
        boolean existe = false;
        try (Connection connection = ConexionDB.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USUARIO_BY_EMAIL_SQL)) {
            
            preparedStatement.setString(1, email);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                existe = rs.next(); // Si hay alguna fila, el email existe
            }
        } catch (SQLException e) {
            printSQLException(e); // Log del error
            throw e; // <-- CAMBIO CLAVE: Relanza la excepción
        }
        return existe;
    }


    /**
     * Imprime información detallada de una SQLException.
     * @param ex La SQLException a imprimir.
     */
    private void printSQLException(SQLException ex) {
        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (!ignoreSQLException(((SQLException) e).getSQLState())) {
                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " + ((SQLException) e).getSQLState());
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
