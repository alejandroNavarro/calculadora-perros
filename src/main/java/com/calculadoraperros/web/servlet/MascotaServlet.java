package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.MascotaDAO;
import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.modelo.Usuario;
import com.calculadoraperros.web.util.CalculadoraNutricional; // Importar la clase CalculadoraNutricional

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part; // Importación necesaria para manejar archivos

import java.io.File; // Para manejar archivos en el sistema de archivos
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException; // Importación específica para este error
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Date; // Para SimpleDateFormat.parse()
import java.util.UUID; // Para generar nombres de archivo únicos

// Anotación necesaria para manejar la subida de archivos (multipart/form-data)
@WebServlet("/MascotaServlet")
@jakarta.servlet.annotation.MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2 MB
    maxFileSize = 1024 * 1024 * 10,      // 10 MB
    maxRequestSize = 1024 * 1024 * 50    // 50 MB
)
public class MascotaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MascotaDAO mascotaDAO;

    // Directorio donde se guardarán las imágenes subidas
    // Asegúrate de que este directorio exista y sea escribible por el servidor
    private static final String UPLOAD_DIRECTORY = "uploads";

    /**
     * Inicializa el Servlet y crea instancias de MascotaDAO.
     */
    public void init() {
        this.mascotaDAO = new MascotaDAO();
        System.out.println("MascotaServlet inicializado.");
    }

    /**
     * Carga la lista de mascotas para el usuario actual y la guarda en la sesión y request.
     * @param request Objeto HttpServletRequest.
     * @param session Objeto HttpSession.
     * @param usuarioActual Objeto Usuario.
     * @throws SQLException Si ocurre un error de base de datos.
     */
    private void cargarMascotas(HttpServletRequest request, HttpSession session, Usuario usuarioActual) throws SQLException {
        List<Mascota> mascotas = null;

        // Intentar obtener mascotas de la sesión primero
        mascotas = (List<Mascota>) session.getAttribute("mascotasUsuario");

        // Si no están en sesión o la sesión está vacía, cargarlas de la DB
        if (mascotas == null || mascotas.isEmpty()) {
            mascotas = this.mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario());
            session.setAttribute("mascotasUsuario", mascotas);
            System.out.println("MascotaServlet: Mascotas cargadas desde DB y guardadas en sesión para usuario ID: " + usuarioActual.getIdUsuario());
        } else {
            System.out.println("MascotaServlet: Mascotas obtenidas de la sesión para usuario ID: " + usuarioActual.getIdUsuario());
        }
        
        request.setAttribute("listaMascotas", mascotas);
    }

    /**
     * Maneja las solicitudes GET para mostrar el panel de mascotas, el formulario de nueva mascota,
     * el formulario de edición de mascota o la confirmación de eliminación.
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
        System.out.println("MascotaServlet - doGet: Acción recibida = " + action);

        try {
            // Cargar mascotas para el panel principal o el selector de la calculadora
            cargarMascotas(request, session, usuarioActual);

            switch (action != null ? action : "") {
                case "mostrarFormulario":
                    request.setAttribute("isEditMode", false); // Explicitamente para modo "añadir"
                    request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                    break;
                case "editar":
                    String idMascotaEditarStr = request.getParameter("idMascota");
                    if (idMascotaEditarStr != null && !idMascotaEditarStr.isEmpty()) {
                        try {
                            int idMascota = Integer.parseInt(idMascotaEditarStr);
                            Mascota mascotaExistente = mascotaDAO.obtenerMascotaPorId(idMascota);

                            if (mascotaExistente != null && mascotaExistente.getIdUsuario() == usuarioActual.getIdUsuario()) {
                                request.setAttribute("mascota", mascotaExistente); // Establece la mascota para precargar el formulario
                                request.setAttribute("isEditMode", true); // Explicitamente para modo "editar"
                                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                                System.out.println("doGet: Cargando formulario de edición para mascota ID: " + idMascota);
                            } else {
                                session.setAttribute("message", "Mascota no encontrada o no tienes permiso para editarla.");
                                session.setAttribute("messageType", "danger");
                                response.sendRedirect(request.getContextPath() + "/MascotaServlet"); // Redirige al panel
                            }
                        } catch (NumberFormatException e) {
                            session.setAttribute("message", "ID de mascota inválido para editar.");
                            session.setAttribute("messageType", "danger");
                            response.sendRedirect(request.getContextPath() + "/MascotaServlet"); // Redirige al panel
                        }
                    } else {
                        session.setAttribute("message", "ID de mascota no especificado para editar.");
                        session.setAttribute("messageType", "warning");
                        response.sendRedirect(request.getContextPath() + "/MascotaServlet"); // Redirige al panel
                    }
                    break;
                case "eliminarConfirmar":
                    // Lógica para mostrar la página de confirmación de eliminación (si aplica)
                    String idMascotaConfirmarStr = request.getParameter("idMascota");
                    if (idMascotaConfirmarStr != null && !idMascotaConfirmarStr.isEmpty()) {
                        try {
                            int idMascota = Integer.parseInt(idMascotaConfirmarStr);
                            Mascota mascotaAEliminar = mascotaDAO.obtenerMascotaPorId(idMascota);
                            if (mascotaAEliminar != null && mascotaAEliminar.getIdUsuario() == usuarioActual.getIdUsuario()) {
                                request.setAttribute("mascotaAEliminar", mascotaAEliminar);
                                request.getRequestDispatcher("/confirmarEliminarMascota.jsp").forward(request, response);
                            } else {
                                session.setAttribute("message", "Mascota no encontrada o no tienes permiso para eliminarla.");
                                session.setAttribute("messageType", "danger");
                                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                            }
                        } catch (NumberFormatException e) {
                            session.setAttribute("message", "ID de mascota inválido para eliminar.");
                            session.setAttribute("messageType", "danger");
                            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                        }
                    } else {
                        session.setAttribute("message", "ID de mascota no especificado para eliminar.");
                        session.setAttribute("messageType", "warning");
                        response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                    }
                    break;
                default:
                    // Acción por defecto: mostrar el panel de mascotas
                    request.getRequestDispatcher("/panel.jsp").forward(request, response);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("message", "Error de base de datos en MascotaServlet (GET): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/MascotaServlet"); // Redirige al panel en caso de error
            System.err.println("MascotaServlet - doGet: Error SQL: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "Ocurrió un error inesperado en MascotaServlet (GET): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/MascotaServlet"); // Redirige al panel en caso de error
            System.err.println("MascotaServlet - doGet: Error inesperado: " + e.getMessage());
        }
    }

    /**
     * Maneja las solicitudes POST de los formularios de mascota (registrar, actualizar, eliminar).
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

            // Obtener el directorio real de subida en el servidor
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIRECTORY;

            // Crear el directorio de subida si no existe
            File uploadDir = new File(uploadFilePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            switch (action != null ? action : "") {
                case "insertar":
                    // Se construye la mascota directamente del request para validación y luego se usa para insertar
                    Mascota nuevaMascota = buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), null);
                    
                    // Procesar la subida de la imagen
                    Part filePart = request.getPart("imagenFile"); // "imagenFile" es el 'name' del input type="file"
                    if (filePart != null && filePart.getSize() > 0) {
                        String fileName = getFileName(filePart);
                        if (fileName != null && !fileName.isEmpty()) {
                            // Generar un nombre único para el archivo
                            String imagenFileName = UUID.randomUUID().toString() + "_" + fileName;
                            filePart.write(uploadFilePath + File.separator + imagenFileName);
                            System.out.println("Archivo subido: " + imagenFileName + " a " + uploadFilePath);
                            nuevaMascota.setImagen(imagenFileName); // Asignar el nombre de archivo a la mascota
                        }
                    }

                    // Validación de campos obligatorios para insertar
                    if (nuevaMascota.getNombre() == null || nuevaMascota.getNombre().trim().isEmpty() ||
                        nuevaMascota.getSexo() == null || nuevaMascota.getSexo().trim().isEmpty() ||
                        nuevaMascota.getFechaNacimiento() == null ||
                        nuevaMascota.getRaza() == null || nuevaMascota.getRaza().trim().isEmpty() ||
                        nuevaMascota.getTipo() == null || nuevaMascota.getTipo().trim().isEmpty() ||
                        nuevaMascota.getNivelActividad() == null || nuevaMascota.getNivelActividad().trim().isEmpty() ||
                        nuevaMascota.getPeso() <= 0) { // Validar peso positivo
                        
                        message = "Todos los campos obligatorios (Nombre, Sexo, Fecha de Nacimiento, Raza, Peso, Tipo, Nivel de Actividad) deben ser rellenados y válidos.";
                        messageType = "danger";
                        request.setAttribute("mascota", nuevaMascota); // Pasa la mascota con los datos del request para precargar
                        request.setAttribute("isEditMode", false); // ¡Importante! Mantener el modo "añadir" en caso de error
                        session.setAttribute("message", message);
                        session.setAttribute("messageType", messageType);
                        request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                        return;
                    }
                    
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
                            // Eliminar el archivo de imagen asociado si existe
                            if (mascotaAEliminar.getImagen() != null && !mascotaAEliminar.getImagen().isEmpty()) {
                                File imageFileToDelete = new File(uploadFilePath + File.separator + mascotaAEliminar.getImagen());
                                if (imageFileToDelete.exists()) {
                                    if (imageFileToDelete.delete()) {
                                        System.out.println("Imagen antigua eliminada: " + mascotaAEliminar.getImagen());
                                    } else {
                                        System.err.println("No se pudo eliminar la imagen antigua: " + mascotaAEliminar.getImagen());
                                    }
                                }
                            }

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
                            // Reconstruir la mascota con los datos del formulario para la validación
                            Mascota mascotaConNuevosDatos = buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), idMascotaActualizar);

                            // Debugging: Print values to console
                            System.out.println("--- Depuración Actualizar Mascota ---");
                            System.out.println("ID Mascota: " + idMascotaActualizar);
                            System.out.println("Nombre: " + mascotaConNuevosDatos.getNombre());
                            System.out.println("Sexo: " + mascotaConNuevosDatos.getSexo());
                            System.out.println("Fecha Nacimiento: " + mascotaConNuevosDatos.getFechaNacimiento());
                            System.out.println("Raza: " + mascotaConNuevosDatos.getRaza());
                            System.out.println("Peso: " + mascotaConNuevosDatos.getPeso());
                            System.out.println("Esterilizado: " + mascotaConNuevosDatos.isEsterilizado());
                            System.out.println("Tipo: " + mascotaConNuevosDatos.getTipo());
                            System.out.println("Nivel Actividad: " + mascotaConNuevosDatos.getNivelActividad());
                            System.out.println("Condición Salud: " + mascotaConNuevosDatos.getCondicionSalud());
                            System.out.println("Color: " + mascotaConNuevosDatos.getColor());
                            System.out.println("Chip ID: " + mascotaConNuevosDatos.getChipID());
                            System.out.println("Observaciones: " + mascotaConNuevosDatos.getObservaciones());
                            System.out.println("Objetivo Peso: " + mascotaConNuevosDatos.getObjetivoPeso());
                            System.out.println("Estado Reproductor: " + mascotaConNuevosDatos.getEstadoReproductor());
                            System.out.println("Num Cachorros: " + mascotaConNuevosDatos.getNumCachorros());
                            System.out.println("Tipo Alimento Predeterminado: " + mascotaConNuevosDatos.getTipoAlimentoPredeterminado());
                            System.out.println("Kcal por 100g: " + mascotaConNuevosDatos.getKcalPor100gAlimentoPredeterminado());
                            System.out.println("------------------------------------");

                            // Validación de campos obligatorios para actualizar
                            if (mascotaConNuevosDatos.getNombre() == null || mascotaConNuevosDatos.getNombre().trim().isEmpty() ||
                                mascotaConNuevosDatos.getSexo() == null || mascotaConNuevosDatos.getSexo().trim().isEmpty() ||
                                mascotaConNuevosDatos.getFechaNacimiento() == null ||
                                mascotaConNuevosDatos.getRaza() == null || mascotaConNuevosDatos.getRaza().trim().isEmpty() ||
                                mascotaConNuevosDatos.getNivelActividad() == null || mascotaConNuevosDatos.getNivelActividad().trim().isEmpty() ||
                                mascotaConNuevosDatos.getPeso() <= 0) { // Validar peso positivo
                                
                                message = "Todos los campos obligatorios (Nombre, Sexo, Fecha de Nacimiento, Raza, Peso, Nivel de Actividad) deben ser rellenados y válidos para actualizar.";
                                messageType = "danger";
                                request.setAttribute("mascota", mascotaConNuevosDatos); // Pasa los datos que el usuario intentó enviar
                                request.setAttribute("isEditMode", true); // ¡Importante! Mantener el modo "editar" en caso de error
                                session.setAttribute("message", message);
                                session.setAttribute("messageType", messageType);
                                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                                return;
                            }

                            // Procesar la subida de la nueva imagen si hay una
                            String currentImageFileName = request.getParameter("imagenExistente"); // Nombre de la imagen actual
                            Part newFilePart = request.getPart("imagenFile");
                            String newImageFileName = currentImageFileName; // Por defecto, se mantiene la imagen existente

                            if (newFilePart != null && newFilePart.getSize() > 0) {
                                String fileName = getFileName(newFilePart);
                                if (fileName != null && !fileName.isEmpty()) {
                                    newImageFileName = UUID.randomUUID().toString() + "_" + fileName;
                                    newFilePart.write(uploadFilePath + File.separator + newImageFileName);
                                    System.out.println("Nueva imagen subida: " + newImageFileName);

                                    // Si hay una nueva imagen, eliminar la antigua si existe y es diferente
                                    if (currentImageFileName != null && !currentImageFileName.isEmpty() && !currentImageFileName.equals(newImageFileName)) {
                                        File oldImageFile = new File(uploadFilePath + File.separator + currentImageFileName);
                                        if (oldImageFile.exists()) {
                                            if (oldImageFile.delete()) {
                                                System.out.println("Imagen antigua eliminada: " + currentImageFileName);
                                            } else {
                                                System.err.println("No se pudo eliminar la imagen antigua: " + currentImageFileName);
                                            }
                                        }
                                    }
                                }
                            }
                            mascotaConNuevosDatos.setImagen(newImageFileName);


                            // Actualizar el objeto mascotaAActualizar con los datos validados del formulario
                            mascotaAActualizar.setNombre(mascotaConNuevosDatos.getNombre());
                            mascotaAActualizar.setSexo(mascotaConNuevosDatos.getSexo());
                            mascotaAActualizar.setFechaNacimiento(mascotaConNuevosDatos.getFechaNacimiento());
                            mascotaAActualizar.setRaza(mascotaConNuevosDatos.getRaza());
                            mascotaAActualizar.setPeso(mascotaConNuevosDatos.getPeso());
                            mascotaAActualizar.setEsterilizado(mascotaConNuevosDatos.isEsterilizado());
                            mascotaAActualizar.setTipo(mascotaConNuevosDatos.getTipo());
                            mascotaAActualizar.setNivelActividad(mascotaConNuevosDatos.getNivelActividad());
                            mascotaAActualizar.setCondicionSalud(mascotaConNuevosDatos.getCondicionSalud());
                            mascotaAActualizar.setImagen(mascotaConNuevosDatos.getImagen());
                            mascotaAActualizar.setColor(mascotaConNuevosDatos.getColor());
                            mascotaAActualizar.setChipID(mascotaConNuevosDatos.getChipID());
                            mascotaAActualizar.setObservaciones(mascotaConNuevosDatos.getObservaciones());
                            mascotaAActualizar.setObjetivoPeso(mascotaConNuevosDatos.getObjetivoPeso());
                            mascotaAActualizar.setEstadoReproductor(mascotaConNuevosDatos.getEstadoReproductor());
                            mascotaAActualizar.setNumCachorros(mascotaConNuevosDatos.getNumCachorros());
                            mascotaAActualizar.setTipoAlimentoPredeterminado(mascotaConNuevosDatos.getTipoAlimentoPredeterminado());
                            mascotaAActualizar.setKcalPor100gAlimentoPredeterminado(mascotaConNuevosDatos.getKcalPor100gAlimentoPredeterminado());

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

            if (operacionExitosa) {
                session.removeAttribute("mascotasUsuario"); // Forzar recarga de la lista de mascotas en la sesión
            }
            
            session.setAttribute("message", message);
            session.setAttribute("messageType", messageType);

            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            return;

        } catch (SQLIntegrityConstraintViolationException e) {
            // Captura específica para errores de duplicidad (como chipID)
            e.printStackTrace();
            String errorMessage = "Error: ";
            if (e.getMessage().contains("chipID")) {
                errorMessage += "El número de chip ya está registrado para otra mascota. Por favor, introduce uno diferente.";
            } else {
                errorMessage += "Se ha producido un error de duplicidad de datos. Por favor, revisa los campos únicos.";
            }
            session.setAttribute("message", errorMessage);
            session.setAttribute("messageType", "danger");
            // Si hay un error de DB, intentar precargar el formulario con los datos que el usuario envió
            if ("insertar".equals(action)) {
                request.setAttribute("mascota", buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), null));
                request.setAttribute("isEditMode", false); // Mantener en modo "añadir"
                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                return;
            } else if ("actualizar".equals(action)) {
                request.setAttribute("mascota", buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), Integer.parseInt(request.getParameter("idMascota"))));
                request.setAttribute("isEditMode", true); // Mantener en modo "editar"
                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("message", "Error de base de datos en MascotaServlet (POST): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            // Si hay un error de DB, intentar precargar el formulario con los datos que el usuario envió
            if ("insertar".equals(action)) {
                request.setAttribute("mascota", buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), null));
                request.setAttribute("isEditMode", false); // Mantener en modo "añadir"
                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                return;
            } else if ("actualizar".equals(action)) {
                request.setAttribute("mascota", buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), Integer.parseInt(request.getParameter("idMascota"))));
                request.setAttribute("isEditMode", true); // Mantener en modo "editar"
                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "Ocurrió un error inesperado en MascotaServlet (POST): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            // Si hay un error inesperado, intentar precargar el formulario con los datos que el usuario envió
            if ("insertar".equals(action)) {
                request.setAttribute("mascota", buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), null));
                request.setAttribute("isEditMode", false); // Mantener en modo "añadir"
                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                return;
            } else if ("actualizar".equals(action)) {
                request.setAttribute("mascota", buildMascotaFromRequest(request, usuarioActual.getIdUsuario(), Integer.parseInt(request.getParameter("idMascota"))));
                request.setAttribute("isEditMode", true); // Mantener en modo "editar"
                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                return;
            } else {
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                return;
            }
        }
    }

    /**
     * Método auxiliar para construir un objeto Mascota a partir de los parámetros del request.
     * Útil para precargar el formulario en caso de errores de validación.
     * Ya no lanza ParseException, la maneja internamente.
     */
    private Mascota buildMascotaFromRequest(HttpServletRequest request, int idUsuario, Integer idMascota) {
        Mascota mascota = new Mascota();
        if (idMascota != null) {
            mascota.setIdMascota(idMascota);
        }
        mascota.setIdUsuario(idUsuario);
        mascota.setNombre(request.getParameter("nombre"));
        mascota.setSexo(request.getParameter("sexo"));
        
        String fechaNacimientoStr = request.getParameter("fechaNacimiento");
        if (fechaNacimientoStr != null && !fechaNacimientoStr.trim().isEmpty()) {
            try {
                mascota.setFechaNacimiento(new SimpleDateFormat("yyyy-MM-dd").parse(fechaNacimientoStr));
            } catch (ParseException e) {
                System.err.println("Error al parsear la fecha de nacimiento: " + fechaNacimientoStr + ". Estableciendo a null. " + e.getMessage());
                mascota.setFechaNacimiento(null); // Establecer a null si hay un error de parseo
            }
        } else {
            mascota.setFechaNacimiento(null);
        }

        mascota.setRaza(request.getParameter("raza"));
        
        String pesoStr = request.getParameter("peso");
        if (pesoStr != null && !pesoStr.trim().isEmpty()) {
            try {
                // Handle comma as decimal separator for locales
                String cleanedPesoStr = pesoStr.replace(',', '.');
                mascota.setPeso(Double.parseDouble(cleanedPesoStr));
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear el peso: " + pesoStr + ". Estableciendo a 0.0. " + e.getMessage());
                mascota.setPeso(0.0); // Default to 0.0 if there's a parsing error
            }
        } else {
            mascota.setPeso(0.0); // Default to 0.0 if empty
        }

        // Add parsing for boolean esterilizado
        mascota.setEsterilizado("true".equalsIgnoreCase(request.getParameter("esterilizado")));

        // Add parsing for existing fields
        mascota.setTipo(request.getParameter("tipo"));
        mascota.setNivelActividad(request.getParameter("nivelActividad"));
        mascota.setCondicionSalud(request.getParameter("condicionSalud"));

        // Modificación clave: Si chipID es vacío, establecerlo a null
        String chipIDParam = request.getParameter("chipID");
        mascota.setChipID(chipIDParam != null && !chipIDParam.trim().isEmpty() ? chipIDParam.trim() : null);

        mascota.setColor(request.getParameter("color"));
        mascota.setObservaciones(request.getParameter("observaciones"));
        mascota.setObjetivoPeso(request.getParameter("objetivoPeso"));
        mascota.setEstadoReproductor(request.getParameter("estadoReproductor"));
        
        String numCachorrosStr = request.getParameter("numCachorros");
        if (numCachorrosStr != null && !numCachorrosStr.trim().isEmpty()) {
            try {
                mascota.setNumCachorros(Integer.parseInt(numCachorrosStr));
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear el número de cachorros: " + numCachorrosStr + ". Estableciendo a null. " + e.getMessage());
                mascota.setNumCachorros(null);
            }
        } else {
            mascota.setNumCachorros(null);
        }

        mascota.setTipoAlimentoPredeterminado(request.getParameter("tipoAlimentoPredeterminado"));
        
        String kcalStr = request.getParameter("kcalPor100gAlimentoPredeterminado");
        if (kcalStr != null && !kcalStr.trim().isEmpty()) {
            try {
                mascota.setKcalPor100gAlimentoPredeterminado(Double.parseDouble(kcalStr.replace(',', '.')));
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear kcalPor100gAlimentoPredeterminado: " + kcalStr + ". Estableciendo a null. " + e.getMessage());
                mascota.setKcalPor100gAlimentoPredeterminado(null);
            }
        } else {
            mascota.setKcalPor100gAlimentoPredeterminado(null);
        }

        // La imagen se maneja por separado en doPost
        // mascota.setImagen(request.getParameter("imagen")); // No se obtiene aquí directamente del request

        return mascota;
    }

    /**
     * Método auxiliar para obtener el nombre del archivo de una parte (Part).
     * @param part La parte del archivo.
     * @return El nombre del archivo.
     */
    private String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return null;
    }
}
