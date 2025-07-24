package com.calculadoraperros.web.dao;

import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.util.ConexionDB; // Asume que tienes esta clase para la conexión

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Date; // Para java.util.Date

public class MascotaDAO {

    public MascotaDAO() {
        // La conexión se obtiene en cada método para asegurar que esté abierta y cerrada correctamente
    }

    /**
     * Inserta una nueva mascota en la base de datos.
     *
     * @param mascota El objeto Mascota a insertar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean insertarMascota(Mascota mascota) throws SQLException {
        String sql = "INSERT INTO mascotas (id_usuario, nombre, sexo, fecha_nacimiento, raza, peso_kg, " +
                     "esterilizado, tipo, nivel_actividad, condicion_salud, imagen, color, chip_id, observaciones, " +
                     "objetivo_peso, estado_reproductor, num_cachorros, tipo_alimento_predeterminado, kcal_por_100g_alimento_predeterminado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        boolean rowInserted = false;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, mascota.getIdUsuario());
            statement.setString(2, mascota.getNombre());
            statement.setString(3, mascota.getSexo());
            statement.setTimestamp(4, new Timestamp(mascota.getFechaNacimiento().getTime())); // Convertir Date a Timestamp
            statement.setString(5, mascota.getRaza());
            statement.setDouble(6, mascota.getPeso()); // Usa getPeso() del modelo
            statement.setBoolean(7, mascota.isEsterilizado());
            statement.setString(8, mascota.getTipo());
            statement.setString(9, mascota.getNivelActividad());
            statement.setString(10, mascota.getCondicionSalud());
            statement.setString(11, mascota.getImagen());
            statement.setString(12, mascota.getColor());
            statement.setString(13, mascota.getChipID()); // Puede ser NULL
            statement.setString(14, mascota.getObservaciones());
            statement.setString(15, mascota.getObjetivoPeso());
            statement.setString(16, mascota.getEstadoReproductor());
            
            // Manejar Integer y Double que pueden ser null
            if (mascota.getNumCachorros() != null) {
                statement.setInt(17, mascota.getNumCachorros());
            } else {
                statement.setNull(17, java.sql.Types.INTEGER);
            }
            statement.setString(18, mascota.getTipoAlimentoPredeterminado());
            if (mascota.getKcalPor100gAlimentoPredeterminado() != null) {
                statement.setDouble(19, mascota.getKcalPor100gAlimentoPredeterminado());
            } else {
                statement.setNull(19, java.sql.Types.DOUBLE);
            }

            rowInserted = statement.executeUpdate() > 0;

            if (rowInserted) {
                try (ResultSet rs = statement.getGeneratedKeys()) {
                    if (rs.next()) {
                        mascota.setIdMascota(rs.getInt(1)); // Asignar el ID generado a la mascota
                    }
                }
            }
        }
        return rowInserted;
    }

    /**
     * Actualiza la información de una mascota existente en la base de datos.
     *
     * @param mascota El objeto Mascota con la información actualizada.
     * @return true si la actualización fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean actualizarMascota(Mascota mascota) throws SQLException {
        String sql = "UPDATE mascotas SET nombre = ?, sexo = ?, fecha_nacimiento = ?, raza = ?, peso_kg = ?, " +
                     "esterilizado = ?, tipo = ?, nivel_actividad = ?, condicion_salud = ?, imagen = ?, " +
                     "color = ?, chip_id = ?, observaciones = ?, objetivo_peso = ?, estado_reproductor = ?, " +
                     "num_cachorros = ?, tipo_alimento_predeterminado = ?, kcal_por_100g_alimento_predeterminado = ? " +
                     "WHERE id_mascota = ? AND id_usuario = ?";
        boolean rowUpdated = false;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setString(1, mascota.getNombre());
            statement.setString(2, mascota.getSexo());
            statement.setTimestamp(3, new Timestamp(mascota.getFechaNacimiento().getTime()));
            statement.setString(4, mascota.getRaza());
            statement.setDouble(5, mascota.getPeso()); // Usa getPeso() del modelo
            statement.setBoolean(6, mascota.isEsterilizado());
            statement.setString(7, mascota.getTipo());
            statement.setString(8, mascota.getNivelActividad());
            statement.setString(9, mascota.getCondicionSalud());
            statement.setString(10, mascota.getImagen());
            statement.setString(11, mascota.getColor());
            statement.setString(12, mascota.getChipID()); // Puede ser NULL
            statement.setString(13, mascota.getObservaciones());
            statement.setString(14, mascota.getObjetivoPeso());
            statement.setString(15, mascota.getEstadoReproductor());
            
            // Manejar Integer y Double que pueden ser null
            if (mascota.getNumCachorros() != null) {
                statement.setInt(16, mascota.getNumCachorros());
            } else {
                statement.setNull(16, java.sql.Types.INTEGER);
            }
            statement.setString(17, mascota.getTipoAlimentoPredeterminado());
            if (mascota.getKcalPor100gAlimentoPredeterminado() != null) {
                statement.setDouble(18, mascota.getKcalPor100gAlimentoPredeterminado());
            } else {
                statement.setNull(18, java.sql.Types.DOUBLE);
            }

            statement.setInt(19, mascota.getIdMascota());
            statement.setInt(20, mascota.getIdUsuario());

            rowUpdated = statement.executeUpdate() > 0;
        }
        return rowUpdated;
    }

    /**
     * Obtiene una mascota por su ID.
     *
     * @param idMascota El ID de la mascota.
     * @return El objeto Mascota si se encuentra, o null si no.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public Mascota obtenerMascotaPorId(int idMascota) throws SQLException {
        Mascota mascota = null;
        String sql = "SELECT id_mascota, id_usuario, nombre, sexo, fecha_nacimiento, raza, peso_kg, " +
                     "esterilizado, fecha_registro, tipo, nivel_actividad, condicion_salud, imagen, " +
                     "color, chip_id, observaciones, objetivo_peso, estado_reproductor, num_cachorros, " +
                     "tipo_alimento_predeterminado, kcal_por_100g_alimento_predeterminado " +
                     "FROM mascotas WHERE id_mascota = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, idMascota);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    mascota = new Mascota();
                    mascota.setIdMascota(resultSet.getInt("id_mascota"));
                    mascota.setIdUsuario(resultSet.getInt("id_usuario"));
                    mascota.setNombre(resultSet.getString("nombre"));
                    mascota.setSexo(resultSet.getString("sexo"));
                    mascota.setFechaNacimiento(new Date(resultSet.getTimestamp("fecha_nacimiento").getTime())); // Convertir Timestamp a Date
                    mascota.setRaza(resultSet.getString("raza"));
                    mascota.setPeso(resultSet.getDouble("peso_kg")); // Leer de peso_kg
                    mascota.setEsterilizado(resultSet.getBoolean("esterilizado"));
                    mascota.setFechaRegistro(resultSet.getTimestamp("fecha_registro"));
                    mascota.setTipo(resultSet.getString("tipo"));
                    mascota.setNivelActividad(resultSet.getString("nivel_actividad"));
                    mascota.setCondicionSalud(resultSet.getString("condicion_salud"));
                    mascota.setImagen(resultSet.getString("imagen"));
                    mascota.setColor(resultSet.getString("color"));
                    mascota.setChipID(resultSet.getString("chip_id"));
                    mascota.setObservaciones(resultSet.getString("observaciones"));
                    mascota.setObjetivoPeso(resultSet.getString("objetivo_peso"));
                    mascota.setEstadoReproductor(resultSet.getString("estado_reproductor"));
                    
                    // Manejar Integer y Double que pueden ser null
                    Integer numCachorros = resultSet.getObject("num_cachorros", Integer.class);
                    mascota.setNumCachorros(numCachorros);

                    mascota.setTipoAlimentoPredeterminado(resultSet.getString("tipo_alimento_predeterminado"));
                    Double kcalPor100gAlimentoPredeterminado = resultSet.getObject("kcal_por_100g_alimento_predeterminado", Double.class);
                    mascota.setKcalPor100gAlimentoPredeterminado(kcalPor100gAlimentoPredeterminado);
                }
            }
        }
        return mascota;
    }

    /**
     * Obtiene todas las mascotas de un usuario específico.
     *
     * @param idUsuario El ID del usuario.
     * @return Una lista de objetos Mascota.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public List<Mascota> obtenerTodasMascotasPorUsuario(int idUsuario) throws SQLException {
        List<Mascota> listaMascotas = new ArrayList<>();
        String sql = "SELECT id_mascota, id_usuario, nombre, sexo, fecha_nacimiento, raza, peso_kg, " +
                     "esterilizado, fecha_registro, tipo, nivel_actividad, condicion_salud, imagen, " +
                     "color, chip_id, observaciones, objetivo_peso, estado_reproductor, num_cachorros, " +
                     "tipo_alimento_predeterminado, kcal_por_100g_alimento_predeterminado " +
                     "FROM mascotas WHERE id_usuario = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.setInt(1, idUsuario);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Mascota mascota = new Mascota();
                    mascota.setIdMascota(resultSet.getInt("id_mascota"));
                    mascota.setIdUsuario(resultSet.getInt("id_usuario"));
                    mascota.setNombre(resultSet.getString("nombre"));
                    mascota.setSexo(resultSet.getString("sexo"));
                    mascota.setFechaNacimiento(new Date(resultSet.getTimestamp("fecha_nacimiento").getTime()));
                    mascota.setRaza(resultSet.getString("raza"));
                    mascota.setPeso(resultSet.getDouble("peso_kg")); // Leer de peso_kg
                    mascota.setEsterilizado(resultSet.getBoolean("esterilizado"));
                    mascota.setFechaRegistro(resultSet.getTimestamp("fecha_registro"));
                    mascota.setTipo(resultSet.getString("tipo"));
                    mascota.setNivelActividad(resultSet.getString("nivel_actividad"));
                    mascota.setCondicionSalud(resultSet.getString("condicion_salud"));
                    mascota.setImagen(resultSet.getString("imagen"));
                    mascota.setColor(resultSet.getString("color"));
                    mascota.setChipID(resultSet.getString("chip_id"));
                    mascota.setObservaciones(resultSet.getString("observaciones"));
                    mascota.setObjetivoPeso(resultSet.getString("objetivo_peso"));
                    mascota.setEstadoReproductor(resultSet.getString("estado_reproductor"));

                    // Manejar Integer y Double que pueden ser null
                    Integer numCachorros = resultSet.getObject("num_cachorros", Integer.class);
                    mascota.setNumCachorros(numCachorros);

                    mascota.setTipoAlimentoPredeterminado(resultSet.getString("tipo_alimento_predeterminado"));
                    Double kcalPor100gAlimentoPredeterminado = resultSet.getObject("kcal_por_100g_alimento_predeterminado", Double.class);
                    mascota.setKcalPor100gAlimentoPredeterminado(kcalPor100gAlimentoPredeterminado);

                    listaMascotas.add(mascota);
                }
            }
        }
        return listaMascotas;
    }

    /**
     * Elimina una mascota de la base de datos por su ID.
     *
     * @param idMascota El ID de la mascota a eliminar.
     * @return true si la eliminación fue exitosa, false en caso contrario.
     * @throws SQLException Si ocurre un error de SQL.
     */
    public boolean eliminarMascota(int idMascota) throws SQLException {
        String sql = "DELETE FROM mascotas WHERE id_mascota = ?";
        boolean rowDeleted = false;
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, idMascota);
            rowDeleted = statement.executeUpdate() > 0;
        }
        return rowDeleted;
    }
}
