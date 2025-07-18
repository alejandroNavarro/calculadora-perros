package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.MascotaDAO;
import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.modelo.Usuario;
import com.calculadoraperros.web.util.CalculadoraNutricional;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet para manejar las operaciones relacionadas con las mascotas (registro, listado, edición, eliminación).
 * Gestiona la carga y actualización de la lista de mascotas en la sesión del usuario.
 */
@WebServlet("/MascotaServlet")
public class MascotaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MascotaDAO mascotaDAO;
    private CalculadoraNutricional calculadoraNutricional;

    /**
     * Inicializa el Servlet y crea instancias de MascotaDAO y CalculadoraNutricional.
     */
    public void init() {
        this.mascotaDAO = new MascotaDAO();
        this.calculadoraNutricional = new CalculadoraNutricional();
        System.out.println("MascotaServlet inicializado.");
    }

    /**
     * Carga la lista de mascotas para el usuario actual y la guarda en la sesión.
     * También calcula las calorías y las añade al request.
     * @param request Objeto HttpServletRequest.
     * @param session Objeto HttpSession.
     * @param usuarioActual Objeto Usuario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void cargarMascotasYCalorias(HttpServletRequest request, HttpSession session, Usuario usuarioActual) throws SQLException {
        List<Mascota> mascotas = null;

        // Intentar obtener la lista de mascotas de la sesión
        // Se usa un atributo específico para la lista de mascotas en sesión
        mascotas = (List<Mascota>) session.getAttribute("mascotasUsuario");

        if (mascotas == null || mascotas.isEmpty()) {
            // Si no está en sesión o está vacía, cargar de la base de datos
            mascotas = this.mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario());
            // Guardar en la sesión para futuras peticiones
            session.setAttribute("mascotasUsuario", mascotas);
            System.out.println("MascotaServlet: Mascotas cargadas desde DB y guardadas en sesión para usuario ID: " + usuarioActual.getIdUsuario());
        } else {
            System.out.println("MascotaServlet: Mascotas obtenidas de la sesión para usuario ID: " + usuarioActual.getIdUsuario());
        }
        
        // Calcular calorías para cada mascota y añadirlas al request (siempre se calculan frescas)
        Map<Integer, Double> caloriasPorMascota = new HashMap<>();
        for (Mascota mascota : mascotas) {
            double mer = calculadoraNutricional.calcularMER(mascota.getPesoKg());
            double factorAjuste = calculadoraNutricional.calcularFactorDER(
                mascota,
                mascota.getNivelActividad(),
                "MANTENER", // Asumir objetivo de mantenimiento para el panel
                "NINGUNO",  // Asumir estado reproductor normal para el panel
                0,          // No hay cachorros por defecto
                false       // Asumir no hay enfermedad por defecto
            );
            double der = calculadoraNutricional.calcularDER(mer, factorAjuste);
            caloriasPorMascota.put(mascota.getIdMascota(), der);
        }
        request.setAttribute("caloriasPorMascota", caloriasPorMascota);
        request.setAttribute("listaMascotas", mascotas); // La lista de mascotas también se pasa al request para el JSP
    }


    /**
     * Maneja las solicitudes POST de los formularios de mascota (registrar, actualizar).
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        if (usuarioActual == null) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login.jsp");
            System.out.println("doPost: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        String action = request.getParameter("action");
        System.out.println("MascotaServlet - doPost: Acción recibida = " + action);

        try {
            boolean operacionExitosa = false;
            String message = "";
            String messageType = "";

            switch (action != null ? action : "") {
                case "agregar":
                    String nombre = request.getParameter("nombre");
                    String sexo = request.getParameter("sexo");
                    String fechaNacimientoStr = request.getParameter("fechaNacimiento");
                    String raza = request.getParameter("raza");
                    double pesoKg = 0.0;
                    boolean esterilizado = "true".equalsIgnoreCase(request.getParameter("esterilizado"));
                    String tipo = request.getParameter("tipo");
                    String nivelActividad = request.getParameter("nivelActividad");
                    String condicionSalud = request.getParameter("condicionSalud");

                    try {
                        pesoKg = Double.parseDouble(request.getParameter("pesoKg"));
                    } catch (NumberFormatException e) {
                        message = "Error en el peso. Por favor, introduce un valor numérico válido.";
                        messageType = "danger";
                        break; // Salir del switch para ir al manejo de errores
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date fechaNacimiento = null;
                    try {
                        fechaNacimiento = sdf.parse(fechaNacimientoStr);
                    } catch (ParseException e) {
                        message = "Formato de fecha de nacimiento inválido. Use AAAA-MM-DD.";
                        messageType = "danger";
                        break;
                    }

                    if (nombre == null || nombre.trim().isEmpty() ||
                        sexo == null || sexo.trim().isEmpty() ||
                        fechaNacimientoStr == null || fechaNacimientoStr.trim().isEmpty() ||
                        raza == null || raza.trim().isEmpty() ||
                        tipo == null || tipo.trim().isEmpty() ||
                        nivelActividad == null || nivelActividad.trim().isEmpty() ||
                        condicionSalud == null || condicionSalud.trim().isEmpty()) {
                        message = "Todos los campos obligatorios (Nombre, Sexo, Fecha de Nacimiento, Raza, Tipo, Nivel de Actividad, Condición de Salud) deben ser rellenados.";
                        messageType = "danger";
                        break;
                    }

                    Mascota nuevaMascota = new Mascota(usuarioActual.getIdUsuario(), nombre, sexo, fechaNacimiento, raza, pesoKg, esterilizado, tipo, nivelActividad, condicionSalud);
                    
                    if (mascotaDAO.insertarMascota(nuevaMascota)) {
                        operacionExitosa = true;
                        message = "Mascota agregada correctamente.";
                        messageType = "success";
                    } else {
                        message = "Error al agregar la mascota.";
                        messageType = "danger";
                    }
                    break;

                case "eliminar":
                    String idMascotaEliminarStr = request.getParameter("idMascota");
                    if (idMascotaEliminarStr != null && !idMascotaEliminarStr.isEmpty()) {
                        int idMascotaEliminar = Integer.parseInt(idMascotaEliminarStr);
                        Mascota mascotaAEliminar = mascotaDAO.obtenerMascotaPorId(idMascotaEliminar);
                        if (mascotaAEliminar != null && mascotaAEliminar.getIdUsuario() == usuarioActual.getIdUsuario()) {
                            if (mascotaDAO.eliminarMascota(idMascotaEliminar)) {
                                operacionExitosa = true;
                                message = "Mascota eliminada correctamente.";
                                messageType = "success";
                            } else {
                                message = "Error al eliminar la mascota.";
                                messageType = "danger";
                            }
                        } else {
                            message = "Mascota no encontrada o no autorizada para eliminar.";
                            messageType = "danger";
                        }
                    } else {
                        message = "ID de mascota no especificado para eliminar.";
                        messageType = "warning";
                    }
                    break;

                case "actualizar":
                    String idMascotaActualizarStr = request.getParameter("idMascota");
                    if (idMascotaActualizarStr != null && !idMascotaActualizarStr.isEmpty()) {
                        int idMascotaActualizar = Integer.parseInt(idMascotaActualizarStr);
                        Mascota mascotaAActualizar = mascotaDAO.obtenerMascotaPorId(idMascotaActualizar);

                        if (mascotaAActualizar != null && mascotaAActualizar.getIdUsuario() == usuarioActual.getIdUsuario()) {
                            String nombreUpdate = request.getParameter("nombre");
                            String sexoUpdate = request.getParameter("sexo");
                            String fechaNacimientoUpdateStr = request.getParameter("fechaNacimiento");
                            String razaUpdate = request.getParameter("raza");
                            double pesoKgUpdate = 0.0;
                            boolean esterilizadoUpdate = "true".equalsIgnoreCase(request.getParameter("esterilizado"));
                            String tipoUpdate = request.getParameter("tipo");
                            String nivelActividadUpdate = request.getParameter("nivelActividad");
                            String condicionSaludUpdate = request.getParameter("condicionSalud");

                            try {
                                pesoKgUpdate = Double.parseDouble(request.getParameter("pesoKg"));
                            } catch (NumberFormatException e) {
                                message = "Error en el peso para actualizar. Por favor, introduce un valor numérico válido.";
                                messageType = "danger";
                                request.setAttribute("mascota", mascotaAActualizar); // Mantener el objeto mascota para repoblar el formulario
                                request.setAttribute("message", message);
                                request.setAttribute("messageType", messageType);
                                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                                return;
                            }

                            SimpleDateFormat sdfUpdate = new SimpleDateFormat("yyyy-MM-dd");
                            Date fechaNacimientoUpdate = null;
                            try {
                                fechaNacimientoUpdate = sdfUpdate.parse(fechaNacimientoUpdateStr);
                            } catch (ParseException e) {
                                message = "Formato de fecha de nacimiento inválido para actualizar. Use AAAA-MM-DD.";
                                messageType = "danger";
                                request.setAttribute("mascota", mascotaAActualizar);
                                request.setAttribute("message", message);
                                request.setAttribute("messageType", messageType);
                                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                                return;
                            }

                            if (nombreUpdate == null || nombreUpdate.trim().isEmpty() ||
                                sexoUpdate == null || sexoUpdate.trim().isEmpty() ||
                                fechaNacimientoUpdateStr == null || fechaNacimientoUpdateStr.trim().isEmpty() ||
                                razaUpdate == null || razaUpdate.trim().isEmpty() ||
                                tipoUpdate == null || tipoUpdate.trim().isEmpty() ||
                                nivelActividadUpdate == null || nivelActividadUpdate.trim().isEmpty() ||
                                condicionSaludUpdate == null || condicionSaludUpdate.trim().isEmpty()) {
                                message = "Todos los campos obligatorios deben ser rellenados para actualizar.";
                                messageType = "danger";
                                request.setAttribute("mascota", mascotaAActualizar);
                                request.setAttribute("message", message);
                                request.setAttribute("messageType", messageType);
                                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                                return;
                            }

                            mascotaAActualizar.setNombre(nombreUpdate);
                            mascotaAActualizar.setSexo(sexoUpdate);
                            mascotaAActualizar.setFechaNacimiento(fechaNacimientoUpdate);
                            mascotaAActualizar.setRaza(razaUpdate);
                            mascotaAActualizar.setPesoKg(pesoKgUpdate);
                            mascotaAActualizar.setEsterilizado(esterilizadoUpdate);
                            mascotaAActualizar.setTipo(tipoUpdate);
                            mascotaAActualizar.setNivelActividad(nivelActividadUpdate);
                            mascotaAActualizar.setCondicionSalud(condicionSaludUpdate);

                            if (mascotaDAO.actualizarMascota(mascotaAActualizar)) {
                                operacionExitosa = true;
                                message = "Mascota actualizada correctamente.";
                                messageType = "success";
                            } else {
                                message = "Error al actualizar la mascota.";
                                messageType = "danger";
                            }
                        } else {
                            message = "Mascota no encontrada o no tienes permiso para actualizarla.";
                            messageType = "danger";
                        }
                    } else {
                        message = "ID de mascota no especificado para actualizar.";
                        messageType = "warning";
                    }
                    break;

                default:
                    message = "Acción POST no válida.";
                    messageType = "warning";
                    break;
            }

            // Después de cualquier operación POST (agregar, eliminar, actualizar),
            // recargar la lista de mascotas en la sesión para asegurar la consistencia.
            // Esto es CRUCIAL para que los cambios se reflejen sin necesidad de reloguear.
            if (operacionExitosa) { // Solo recargar si la operación de DB fue exitosa
                session.removeAttribute("mascotasUsuario"); // Eliminar la lista antigua de la sesión
                // La próxima llamada a cargarMascotasYCalorias la recargará de la DB
            }
            
            // Establecer el mensaje en la sesión para que se muestre en la siguiente página
            session.setAttribute("message", message);
            session.setAttribute("messageType", messageType);

            // Redirigir siempre al doGet para que se cargue la lista actualizada y se muestre el panel
            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            return;

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos en MascotaServlet (POST): " + e.getMessage());
            request.setAttribute("messageType", "danger");
            // En caso de error de DB, intentar recargar las mascotas para el formulario si es necesario
            try {
                cargarMascotasYCalorias(request, session, usuarioActual); // Recargar para mostrar en el formulario
            } catch (SQLException ex) {
                ex.printStackTrace(); // Log del error de recarga
            }
            request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado en MascotaServlet (POST): " + e.getMessage());
            request.setAttribute("messageType", "danger");
            try {
                cargarMascotasYCalorias(request, session, usuarioActual);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
            return;
        }
    }

    /**
     * Maneja las solicitudes GET (mostrar formulario de registro/edición, listar mascotas, eliminar).
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        if (usuarioActual == null) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login.jsp");
            System.out.println("doGet: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        String action = request.getParameter("action");
        System.out.println("MascotaServlet - doGet: Acción recibida = " + action + ", Usuario ID: " + usuarioActual.getIdUsuario());

        try {
            if ("mostrarFormulario".equals(action)) {
                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
            } else if ("editar".equals(action)) {
                String idMascotaStr = request.getParameter("idMascota");
                if (idMascotaStr != null && !idMascotaStr.isEmpty()) {
                    int idMascota = Integer.parseInt(idMascotaStr);
                    Mascota mascotaEditar = this.mascotaDAO.obtenerMascotaPorId(idMascota);

                    if (mascotaEditar != null && mascotaEditar.getIdUsuario() == usuarioActual.getIdUsuario()) {
                        request.setAttribute("mascota", mascotaEditar);
                        request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                    } else {
                        session.setAttribute("message", "Mascota no encontrada o no tienes permiso para editarla.");
                        session.setAttribute("messageType", "danger");
                        response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                    }
                } else {
                    session.setAttribute("message", "ID de mascota no especificado para editar.");
                    session.setAttribute("messageType", "warning");
                    response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                }
            } else if ("eliminar".equals(action)) {
                // La eliminación se maneja por POST, esto es un catch-all si alguien intenta GET /eliminar
                session.setAttribute("message", "La eliminación de mascotas debe realizarse mediante una solicitud POST.");
                session.setAttribute("messageType", "warning");
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            } else if ("mostrarFormularioDosis".equals(action)) {
                String idMascotaStr = request.getParameter("idMascota");
                if (idMascotaStr != null && !idMascotaStr.trim().isEmpty()) {
                    int idMascota = Integer.parseInt(idMascotaStr);
                    Mascota mascotaSeleccionada = this.mascotaDAO.obtenerMascotaPorId(idMascota);

                    if (mascotaSeleccionada != null && mascotaSeleccionada.getIdUsuario() == usuarioActual.getIdUsuario()) {
                        request.setAttribute("mascota", mascotaSeleccionada);
                        System.out.println("MascotaServlet - doGet: Redirigiendo a /dosis?action=list&idMascota=" + idMascota);
                        response.sendRedirect(request.getContextPath() + "/dosis?action=list&idMascota=" + idMascota);
                        return;
                    } else {
                        session.setAttribute("message", "Mascota no encontrada o no tienes permiso para ver sus dosis.");
                        session.setAttribute("messageType", "danger");
                        response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                        System.out.println("MascotaServlet - doGet: No se pudo encontrar la mascota o no hay permisos. Redirigiendo a /MascotaServlet.");
                    }
                } else {
                    session.setAttribute("message", "ID de mascota no especificado para mostrar el formulario de dosis.");
                    session.setAttribute("messageType", "warning");
                    response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                    System.out.println("MascotaServlet - doGet: ID de mascota inválido. Redirigiendo a /MascotaServlet.");
                }
            }
            else {
                // Lógica principal para listar mascotas en el panel
                // Se llama a la función auxiliar para cargar de sesión o DB
                cargarMascotasYCalorias(request, session, usuarioActual);
                System.out.println("MascotaServlet - doGet: Preparando panel.jsp para usuario ID: " + usuarioActual.getIdUsuario());
                request.getRequestDispatcher("/panel.jsp").forward(request, response);
            }
        } catch (NumberFormatException e) {
            session.setAttribute("message", "ID inválido en la URL.");
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            request.setAttribute("caloriasPorMascota", new HashMap<Integer, Double>());
            request.getRequestDispatcher("/panel.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            request.setAttribute("caloriasPorMascota", new HashMap<Integer, Double>());
            request.getRequestDispatcher("/panel.jsp").forward(request, response);
        }
    }
}
