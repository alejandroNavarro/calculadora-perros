package com.calculadoraperros.web.util; // Un nuevo paquete para utilidades

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de utilidad para gestionar la conexión a la base de datos.
 * Centraliza la configuración de la conexión para evitar duplicación de código.
 */
public class ConexionDB {

    // Configuración de la conexión a la base de datos
    // Asegúrate de que estos valores coincidan con tu configuración de MySQL (Docker)
    // IMPORTANTE: Se añadió allowPublicKeyRetrieval=true para MySQL 8.0+
    private static final String JDBC_URL = "jdbc:mysql://localhost:3308/calculadora_perros?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String JDBC_USERNAME = "root"; // Tu usuario de MySQL
    private static final String JDBC_PASSWORD = "root"; // Tu contraseña de MySQL

    /**
     * Establece y devuelve una conexión a la base de datos.
     * @return Objeto Connection si la conexión es exitosa, null en caso de error.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Cargar el driver JDBC (no es estrictamente necesario en JDBC 4.0+ pero es buena práctica)
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USERNAME, JDBC_PASSWORD);
        } catch (SQLException e) {
            // Imprimir la excepción para depuración
            System.err.println("Error de SQL al intentar conectar a la base de datos:");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver no encontrado. Asegúrate de que el JAR esté en el classpath.");
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Cierra una conexión a la base de datos de forma segura.
     * @param connection La conexión a cerrar.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión a la base de datos:");
                e.printStackTrace();
            }
        }
    }

    // Puedes añadir un método main para probar la conexión si lo deseas
    /*
    public static void main(String[] args) {
        Connection conn = null;
        try {
            conn = getConnection();
            if (conn != null) {
                System.out.println("Conexión a la base de datos exitosa!");
            } else {
                System.out.println("Fallo la conexión a la base de datos.");
            }
        } finally {
            closeConnection(conn);
        }
    }
    */
}
