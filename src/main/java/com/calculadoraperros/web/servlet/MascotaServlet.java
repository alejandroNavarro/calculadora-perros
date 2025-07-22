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
import jakarta.servlet.http.Part; // Importación necesaria para manejar archivos

import java.io.File; // Para manejar archivos en el sistema de archivos
import java.io.IOException;
import java.sql.SQLException;
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
    maxFileSize = 1024 * 1024 * 10,    // 10 MB
    maxRequestSize = 1024 * 1024 * 50  // 50 MB
)
public class MascotaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MascotaDAO mascotaDAO;
    private CalculadoraNutricional calculadoraNutricional;

    // Directorio donde se guardarán las imágenes subidas
    // Asegúrate de que este directorio exista y sea escribible por el servidor
    private static final String UPLOAD_DIRECTORY = "uploads";

    /**
     * Inicializa el Servlet y crea instancias de MascotaDAO y CalculadoraNutricional.
     */
    public void init() {
        this.mascotaDAO = new MascotaDAO();
        this.calculadoraNutricional = new CalculadoraNutricional();
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

        mascotas = (List<Mascota>) session.getAttribute("mascotasUsuario");

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

            // Obtener el directorio real de subida en el servidor
            String applicationPath = request.getServletContext().getRealPath("");
            String uploadFilePath = applicationPath + File.separator + UPLOAD_DIRECTORY;

            // Crear el directorio de subida si no existe
            File uploadDir = new File(uploadFilePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

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
                    
                    String imagenFileName = null; // Para almacenar el nombre del archivo de imagen

                    // Procesar la subida de la imagen
                    Part filePart = request.getPart("imagenFile"); // "imagenFile" es el 'name' del input type="file"
                    if (filePart != null && filePart.getSize() > 0) {
                        String fileName = getFileName(filePart);
                        if (fileName != null && !fileName.isEmpty()) {
                            // Generar un nombre único para el archivo
                            imagenFileName = UUID.randomUUID().toString() + "_" + fileName;
                            filePart.write(uploadFilePath + File.separator + imagenFileName);
                            System.out.println("Archivo subido: " + imagenFileName + " a " + uploadFilePath);
                        }
                    }

                    try {
                        pesoKg = Double.parseDouble(request.getParameter("pesoKg"));
                    } catch (NumberFormatException e) {
                        message = "Error en el peso. Por favor, introduce un valor numérico válido.";
                        messageType = "danger";
                        break;
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date fechaNacimientoTemp = null;
                    java.sql.Date fechaNacimientoSql = null;
                    try {
                        fechaNacimientoTemp = sdf.parse(fechaNacimientoStr);
                        fechaNacimientoSql = new java.sql.Date(fechaNacimientoTemp.getTime());
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

                    Mascota nuevaMascota = new Mascota(usuarioActual.getIdUsuario(), nombre, sexo, fechaNacimientoSql, raza, pesoKg, esterilizado, tipo, nivelActividad, condicionSalud, imagenFileName);
                    
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
                            String nombreUpdate = request.getParameter("nombre");
                            String sexoUpdate = request.getParameter("sexo");
                            String fechaNacimientoUpdateStr = request.getParameter("fechaNacimiento");
                            String razaUpdate = request.getParameter("raza");
                            double pesoKgUpdate = 0.0;
                            boolean esterilizadoUpdate = "true".equalsIgnoreCase(request.getParameter("esterilizado"));
                            String tipoUpdate = request.getParameter("tipo");
                            String nivelActividadUpdate = request.getParameter("nivelActividad");
                            String condicionSaludUpdate = request.getParameter("condicionSalud");
                            String currentImageFileName = request.getParameter("currentImage"); // Nombre de la imagen actual (campo oculto del JSP)

                            String newImageFileName = null; // Para la nueva imagen si se sube

                            // Procesar la subida de la nueva imagen
                            Part newFilePart = request.getPart("imagenFile");
                            if (newFilePart != null && newFilePart.getSize() > 0) {
                                String fileName = getFileName(newFilePart);
                                if (fileName != null && !fileName.isEmpty()) {
                                    newImageFileName = UUID.randomUUID().toString() + "_" + fileName;
                                    newFilePart.write(uploadFilePath + File.separator + newImageFileName);
                                    System.out.println("Nueva imagen subida: " + newImageFileName);

                                    // Eliminar la imagen antigua si existe y es diferente a la nueva
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
                            } else {
                                // Si no se subió un nuevo archivo, mantener el nombre de la imagen actual
                                newImageFileName = currentImageFileName;
                            }

                            try {
                                pesoKgUpdate = Double.parseDouble(request.getParameter("pesoKg"));
                            } catch (NumberFormatException e) {
                                message = "Error en el peso para actualizar. Por favor, introduce un valor numérico válido.";
                                messageType = "danger";
                                request.setAttribute("mascota", mascotaAActualizar);
                                session.setAttribute("message", message); // Usar sesión para el mensaje
                                session.setAttribute("messageType", messageType);
                                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                                return;
                            }

                            SimpleDateFormat sdfUpdate = new SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date fechaNacimientoUpdateTemp = null;
                            java.sql.Date fechaNacimientoUpdateSql = null;
                            try {
                                fechaNacimientoUpdateTemp = sdfUpdate.parse(fechaNacimientoUpdateStr);
                                fechaNacimientoUpdateSql = new java.sql.Date(fechaNacimientoUpdateTemp.getTime());
                            } catch (ParseException e) {
                                message = "Formato de fecha de nacimiento inválido para actualizar. Use AAAA-MM-DD.";
                                messageType = "danger";
                                request.setAttribute("mascota", mascotaAActualizar);
                                session.setAttribute("message", message); // Usar sesión para el mensaje
                                session.setAttribute("messageType", messageType);
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
                                session.setAttribute("message", message); // Usar sesión para el mensaje
                                session.setAttribute("messageType", messageType);
                                request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                                return;
                            }

                            mascotaAActualizar.setNombre(nombreUpdate);
                            mascotaAActualizar.setSexo(sexoUpdate);
                            mascotaAActualizar.setFechaNacimiento(fechaNacimientoUpdateSql);
                            mascotaAActualizar.setRaza(razaUpdate);
                            mascotaAActualizar.setPesoKg(pesoKgUpdate);
                            mascotaAActualizar.setEsterilizado(esterilizadoUpdate);
                            mascotaAActualizar.setTipo(tipoUpdate);
                            mascotaAActualizar.setNivelActividad(nivelActividadUpdate);
                            mascotaAActualizar.setCondicionSalud(condicionSaludUpdate);
                            mascotaAActualizar.setImagen(newImageFileName); // Establecer el nombre de la nueva imagen o la existente

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
                session.removeAttribute("mascotasUsuario"); // Forzar recarga de la lista de mascotas
            }
            
            session.setAttribute("message", message);
            session.setAttribute("messageType", messageType);

            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            return;

        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("message", "Error de base de datos en MascotaServlet (POST): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            try {
                if ("agregar".equals(action) || "actualizar".equals(action)) {
                    // Si hubo un error en agregar/actualizar, redirigir al formulario con los datos pre-rellenados
                    // (el request.setAttribute ya se hizo en los bloques de error específicos)
                    request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                    return;
                } else {
                    response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "Ocurrió un error inesperado en MascotaServlet (POST): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            try {
                if ("agregar".equals(action) || "actualizar".equals(action)) {
                    request.getRequestDispatcher("/mascotaForm.jsp").forward(request, response);
                    return;
                } else {
                    response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                return;
            }
        }
    }

    /**
     * Método auxiliar para extraer el nombre del archivo de la cabecera Content-Disposition.
     * @param part El objeto Part que representa el archivo subido.
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
                // La eliminación se maneja por POST para seguridad y idempotencia
                session.setAttribute("message", "La eliminación de mascotas debe realizarse mediante una solicitud POST.");
                session.setAttribute("messageType", "warning");
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            } else if ("mostrarFormularioDosis".equals(action)) {
                String idMascotaStr = request.getParameter("idMascota");
                if (idMascotaStr != null && !idMascotaStr.trim().isEmpty()) {
                    int idMascota = Integer.parseInt(idMascotaStr);
                    Mascota mascotaSeleccionada = this.mascotaDAO.obtenerMascotaPorId(idMascota);

                    if (mascotaSeleccionada != null && mascotaSeleccionada.getIdUsuario() == usuarioActual.getIdUsuario()) {
                        response.sendRedirect(request.getContextPath() + "/dosis?action=list&idMascota=" + idMascota);
                        return;
                    } else {
                        session.setAttribute("message", "Mascota no encontrada o no tienes permiso para ver sus dosis.");
                        session.setAttribute("messageType", "danger");
                        response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                    }
                } else {
                    session.setAttribute("message", "ID de mascota no especificado para mostrar el formulario de dosis.");
                    session.setAttribute("messageType", "warning");
                    response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                }
            }
            else {
                cargarMascotas(request, session, usuarioActual);
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
            session.setAttribute("message", "Error de base de datos: " + e.getMessage());
            session.setAttribute("messageType", "danger");
            request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            request.getRequestDispatcher("/panel.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "Ocurrió un error inesperado: " + e.getMessage());
            session.setAttribute("messageType", "danger");
            request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            request.getRequestDispatcher("/panel.jsp").forward(request, response);
        }
    }
}
