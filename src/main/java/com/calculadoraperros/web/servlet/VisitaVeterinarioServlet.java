package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.MascotaDAO; // Necesario para obtener la mascota asociada
import com.calculadoraperros.web.dao.VisitaVeterinarioDAO;
import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.modelo.Usuario;
import com.calculadoraperros.web.modelo.VisitaVeterinario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException; // Importación específica para este error
import java.text.ParseException; // Mantener importación por si se usa en otro lado o se decide relanzar
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Servlet para gestionar las operaciones relacionadas con las visitas al veterinario.
 * Permite listar, añadir, editar y eliminar visitas.
 */
@WebServlet("/VisitaVeterinarioServlet")
public class VisitaVeterinarioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private VisitaVeterinarioDAO visitaDAO;
    private MascotaDAO mascotaDAO; // Necesario para verificar la mascota y obtener su nombre

    public void init() {
        visitaDAO = new VisitaVeterinarioDAO();
        mascotaDAO = new MascotaDAO();
        System.out.println("VisitaVeterinarioServlet inicializado.");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        if (usuarioActual == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            System.out.println("VisitaVeterinarioServlet - doGet: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String idMascotaStr = request.getParameter("idMascota");
        int idMascota = -1;

        // Validar idMascota en todas las acciones que lo requieran
        if (idMascotaStr != null && !idMascotaStr.isEmpty()) {
            try {
                idMascota = Integer.parseInt(idMascotaStr);
                Mascota mascota = mascotaDAO.obtenerMascotaPorId(idMascota);
                if (mascota == null || mascota.getIdUsuario() != usuarioActual.getIdUsuario()) {
                    session.setAttribute("message", "Mascota no encontrada o no tienes permiso para ver sus visitas.");
                    session.setAttribute("messageType", "danger");
                    response.sendRedirect(request.getContextPath() + "/MascotaServlet"); // Redirige al panel de mascotas
                    return;
                }
                request.setAttribute("mascota", mascota); // Pasa la mascota a la JSP
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                session.setAttribute("message", "ID de mascota inválido.");
                session.setAttribute("messageType", "danger");
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                return;
            }
        } else {
            // Si no hay idMascota, redirigir al panel de mascotas o mostrar error
            session.setAttribute("message", "Se requiere un ID de mascota para gestionar las visitas.");
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            return;
        }

        System.out.println("VisitaVeterinarioServlet - doGet: Acción recibida = " + action + ", ID Mascota = " + idMascota);

        try {
            switch (action != null ? action : "listar") {
                case "mostrarFormulario":
                    String idVisitaEditarStr = request.getParameter("idVisita");
                    if (idVisitaEditarStr != null && !idVisitaEditarStr.isEmpty()) {
                        int idVisita = Integer.parseInt(idVisitaEditarStr);
                        VisitaVeterinario visitaExistente = visitaDAO.obtenerVisitaPorId(idVisita);
                        
                        // Verificar que la visita pertenece a la mascota y al usuario
                        if (visitaExistente != null && visitaExistente.getIdMascota() == idMascota) {
                            request.setAttribute("visita", visitaExistente);
                            request.setAttribute("isEditMode", true);
                            request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
                        } else {
                            session.setAttribute("message", "Visita no encontrada o no pertenece a esta mascota.");
                            session.setAttribute("messageType", "danger");
                            response.sendRedirect(request.getContextPath() + "/VisitaVeterinarioServlet?action=listar&idMascota=" + idMascota);
                        }
                    } else {
                        request.setAttribute("isEditMode", false);
                        request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
                    }
                    break;
                case "eliminarConfirmar":
                    String idVisitaEliminarStr = request.getParameter("idVisita");
                    if (idVisitaEliminarStr != null && !idVisitaEliminarStr.isEmpty()) {
                        int idVisita = Integer.parseInt(idVisitaEliminarStr);
                        VisitaVeterinario visitaAEliminar = visitaDAO.obtenerVisitaPorId(idVisita);
                        if (visitaAEliminar != null && visitaAEliminar.getIdMascota() == idMascota) {
                            request.setAttribute("visitaAEliminar", visitaAEliminar);
                            request.getRequestDispatcher("/confirmarEliminarVisita.jsp").forward(request, response);
                        } else {
                            session.setAttribute("message", "Visita no encontrada o no pertenece a esta mascota.");
                            session.setAttribute("messageType", "danger");
                            response.sendRedirect(request.getContextPath() + "/VisitaVeterinarioServlet?action=listar&idMascota=" + idMascota);
                        }
                    } else {
                        session.setAttribute("message", "ID de visita no especificado para eliminar.");
                        session.setAttribute("messageType", "warning");
                        response.sendRedirect(request.getContextPath() + "/VisitaVeterinarioServlet?action=listar&idMascota=" + idMascota);
                    }
                    break;
                case "listar":
                default:
                    List<VisitaVeterinario> visitas = visitaDAO.obtenerVisitasPorMascota(idMascota);
                    request.setAttribute("listaVisitas", visitas);
                    request.getRequestDispatcher("/visitasList.jsp").forward(request, response);
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            session.setAttribute("message", "Error de base de datos en VisitaVeterinarioServlet (GET): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/VisitaVeterinarioServlet?action=listar&idMascota=" + idMascota);
            System.err.println("VisitaVeterinarioServlet - doGet: Error SQL: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", "Ocurrió un error inesperado en VisitaVeterinarioServlet (GET): " + e.getMessage());
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/VisitaVeterinarioServlet?action=listar&idMascota=" + idMascota);
            System.err.println("VisitaVeterinarioServlet - doGet: Error inesperado: " + e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        if (usuarioActual == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            System.out.println("VisitaVeterinarioServlet - doPost: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String idMascotaStr = request.getParameter("idMascota");
        int idMascota = -1;

        if (idMascotaStr != null && !idMascotaStr.isEmpty()) {
            try {
                idMascota = Integer.parseInt(idMascotaStr);
                Mascota mascota = mascotaDAO.obtenerMascotaPorId(idMascota);
                if (mascota == null || mascota.getIdUsuario() != usuarioActual.getIdUsuario()) {
                    session.setAttribute("message", "Mascota no encontrada o no tienes permiso para gestionar sus visitas.");
                    session.setAttribute("messageType", "danger");
                    response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                    return;
                }
                request.setAttribute("mascota", mascota); // Pasa la mascota a la JSP en caso de reenvío
            } catch (NumberFormatException | SQLException e) {
                e.printStackTrace();
                session.setAttribute("message", "ID de mascota inválido.");
                session.setAttribute("messageType", "danger");
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                return;
            }
        } else {
            session.setAttribute("message", "Se requiere un ID de mascota para gestionar las visitas.");
            session.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            return;
        }

        System.out.println("VisitaVeterinarioServlet - doPost: Acción recibida = " + action + ", ID Mascota = " + idMascota);

        String message = "";
        String messageType = "";
        boolean operacionExitosa = false;

        try {
            switch (action != null ? action : "") {
                case "insertar":
                    VisitaVeterinario nuevaVisita = buildVisitaFromRequest(request, idMascota, null);
                    // Validaciones básicas
                    if (nuevaVisita.getFechaVisita() == null || nuevaVisita.getMotivo() == null || nuevaVisita.getMotivo().trim().isEmpty()) {
                        message = "La fecha de la visita y el motivo son obligatorios.";
                        messageType = "danger";
                        request.setAttribute("visita", nuevaVisita); // Para precargar el formulario
                        request.setAttribute("isEditMode", false);
                        request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
                        return;
                    }
                    if (visitaDAO.insertarVisita(nuevaVisita)) {
                        operacionExitosa = true;
                        message = "Visita agregada correctamente.";
                        messageType = "success";
                    } else {
                        message = "Error al agregar la visita.";
                        messageType = "danger";
                    }
                    break;
                case "actualizar":
                    String idVisitaActualizarStr = request.getParameter("idVisita");
                    if (idVisitaActualizarStr != null && !idVisitaActualizarStr.isEmpty()) {
                        int idVisitaActualizar = Integer.parseInt(idVisitaActualizarStr);
                        VisitaVeterinario visitaAActualizar = buildVisitaFromRequest(request, idMascota, idVisitaActualizar);

                        // Validaciones básicas
                        if (visitaAActualizar.getFechaVisita() == null || visitaAActualizar.getMotivo() == null || visitaAActualizar.getMotivo().trim().isEmpty()) {
                            message = "La fecha de la visita y el motivo son obligatorios para actualizar.";
                            messageType = "danger";
                            request.setAttribute("visita", visitaAActualizar); // Para precargar el formulario
                            request.setAttribute("isEditMode", true);
                            request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
                            return;
                        }

                        if (visitaDAO.actualizarVisita(visitaAActualizar)) {
                            operacionExitosa = true;
                            message = "Visita actualizada correctamente.";
                            messageType = "success";
                        } else {
                            message = "Error al actualizar la visita o no se encontró la visita para la mascota.";
                            messageType = "danger";
                        }
                    } else {
                        message = "ID de visita no especificado para actualizar.";
                        messageType = "warning";
                    }
                    break;
                case "eliminar":
                    String idVisitaEliminarStr = request.getParameter("idVisita");
                    if (idVisitaEliminarStr != null && !idVisitaEliminarStr.isEmpty()) {
                        int idVisitaEliminar = Integer.parseInt(idVisitaEliminarStr);
                        // Opcional: Verificar que la visita pertenece a la mascota del usuario antes de eliminar
                        VisitaVeterinario visitaCheck = visitaDAO.obtenerVisitaPorId(idVisitaEliminar);
                        if (visitaCheck != null && visitaCheck.getIdMascota() == idMascota) {
                            if (visitaDAO.eliminarVisita(idVisitaEliminar)) {
                                operacionExitosa = true;
                                message = "Visita eliminada correctamente.";
                                messageType = "success";
                            } else {
                                message = "Error al eliminar la visita.";
                                messageType = "danger";
                            }
                        } else {
                            message = "Visita no encontrada o no pertenece a esta mascota.";
                            messageType = "danger";
                        }
                    } else {
                        message = "ID de visita no especificado para eliminar.";
                        messageType = "warning";
                    }
                    break;
                default:
                    message = "Acción POST no válida.";
                    messageType = "warning";
                    break;
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            message = "Error de duplicidad de datos: " + e.getMessage();
            messageType = "danger";
            request.setAttribute("visita", buildVisitaFromRequest(request, idMascota, ("actualizar".equals(action) ? Integer.parseInt(request.getParameter("idVisita")) : null)));
            request.setAttribute("isEditMode", "actualizar".equals(action));
            session.setAttribute("message", message);
            session.setAttribute("messageType", messageType);
            request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            message = "Error de base de datos: " + e.getMessage();
            messageType = "danger";
            request.setAttribute("visita", buildVisitaFromRequest(request, idMascota, ("actualizar".equals(action) ? Integer.parseInt(request.getParameter("idVisita")) : null)));
            request.setAttribute("isEditMode", "actualizar".equals(action));
            session.setAttribute("message", message);
            session.setAttribute("messageType", messageType);
            request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
            return;
        } catch (NumberFormatException e) { // Capturar NumberFormatException para idVisita/idMascota
            e.printStackTrace();
            message = "Error de formato de número en ID: " + e.getMessage();
            messageType = "danger";
            request.setAttribute("visita", buildVisitaFromRequest(request, idMascota, null)); // No podemos usar idVisita si es inválido
            request.setAttribute("isEditMode", "actualizar".equals(action));
            session.setAttribute("message", message);
            session.setAttribute("messageType", messageType);
            request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            message = "Ocurrió un error inesperado: " + e.getMessage();
            messageType = "danger";
            request.setAttribute("visita", buildVisitaFromRequest(request, idMascota, ("actualizar".equals(action) ? Integer.parseInt(request.getParameter("idVisita")) : null)));
            request.setAttribute("isEditMode", "actualizar".equals(action));
            session.setAttribute("message", message);
            session.setAttribute("messageType", messageType);
            request.getRequestDispatcher("/visitaForm.jsp").forward(request, response);
            return;
        }

        session.setAttribute("message", message);
        session.setAttribute("messageType", messageType);
        response.sendRedirect(request.getContextPath() + "/VisitaVeterinarioServlet?action=listar&idMascota=" + idMascota);
    }

    /**
     * Construye un objeto VisitaVeterinario a partir de los parámetros del request.
     * Este método maneja internamente los errores de parseo de fecha y costo.
     * @param request La solicitud HTTP.
     * @param idMascota El ID de la mascota a la que pertenece la visita.
     * @param idVisita El ID de la visita (null si es una nueva visita).
     * @return Un objeto VisitaVeterinario.
     */
    private VisitaVeterinario buildVisitaFromRequest(HttpServletRequest request, int idMascota, Integer idVisita) {
        VisitaVeterinario visita = new VisitaVeterinario();
        if (idVisita != null) {
            visita.setIdVisita(idVisita);
        }
        visita.setIdMascota(idMascota);

        String fechaVisitaStr = request.getParameter("fechaVisita");
        if (fechaVisitaStr != null && !fechaVisitaStr.isEmpty()) {
            try {
                visita.setFechaVisita(new SimpleDateFormat("yyyy-MM-dd").parse(fechaVisitaStr));
            } catch (ParseException e) {
                System.err.println("Error al parsear la fecha de visita: " + fechaVisitaStr + " - " + e.getMessage());
                // Dejar fechaVisita como null, la validación en doPost lo detectará
                visita.setFechaVisita(null); // Asegurarse de que sea null en caso de error
            }
        }

        visita.setMotivo(request.getParameter("motivo"));
        visita.setDiagnostico(request.getParameter("diagnostico"));
        visita.setTratamiento(request.getParameter("tratamiento"));
        visita.setMedicamentosRecetados(request.getParameter("medicamentosRecetados"));

        String costoStr = request.getParameter("costo");
        if (costoStr != null && !costoStr.isEmpty()) {
            try {
                // Reemplazar coma por punto para parsear Double, si el cliente usa coma decimal
                visita.setCosto(new BigDecimal(costoStr.replace(',', '.')));
            } catch (NumberFormatException e) {
                // Manejar el error de formato, por ejemplo, establecer a null
                visita.setCosto(null);
                System.err.println("Error al parsear el costo: " + costoStr + " - " + e.getMessage());
            }
        } else {
            visita.setCosto(null); // O BigDecimal.ZERO si prefieres un valor numérico
        }
        
        visita.setObservaciones(request.getParameter("observaciones"));

        return visita;
    }
}
