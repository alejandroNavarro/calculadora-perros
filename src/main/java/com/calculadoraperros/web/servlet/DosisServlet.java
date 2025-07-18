package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.DosisDAO;
import com.calculadoraperros.web.dao.MascotaDAO;
import com.calculadoraperros.web.modelo.Dosis;
import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.modelo.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Date; // Usar java.util.Date para el modelo Dosis
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList; // Importar ArrayList para inicializar listas vacías

@WebServlet("/dosis")
public class DosisServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DosisDAO dosisDAO;
    private MascotaDAO mascotaDAO;

    public void init() {
        dosisDAO = new DosisDAO();
        mascotaDAO = new MascotaDAO();
        System.out.println("DosisServlet inicializado.");
    }

    // Método auxiliar para obtener y validar la mascota
    private Mascota getAndValidateMascota(HttpServletRequest request, HttpServletResponse response, Usuario usuario, String idMascotaStr) throws IOException, ServletException, SQLException {
        int idMascota = 0;
        if (idMascotaStr != null && !idMascotaStr.isEmpty()) {
            try {
                idMascota = Integer.parseInt(idMascotaStr);
            } catch (NumberFormatException e) {
                request.getSession().setAttribute("message", "ID de mascota inválido.");
                request.getSession().setAttribute("messageType", "danger");
                response.sendRedirect(request.getContextPath() + "/MascotaServlet");
                throw new ServletException("ID de mascota inválido."); // Lanzar para detener la ejecución
            }
        } else {
            request.getSession().setAttribute("message", "No se especificó la mascota.");
            request.getSession().setAttribute("messageType", "warning");
            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            throw new ServletException("No se especificó la mascota."); // Lanzar para detener la ejecución
        }

        Mascota mascota = mascotaDAO.obtenerMascotaPorId(idMascota);

        if (mascota == null || mascota.getIdUsuario() != usuario.getIdUsuario()) {
            request.getSession().setAttribute("message", "Mascota no encontrada o no pertenece al usuario.");
            request.getSession().setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/MascotaServlet");
            throw new ServletException("Mascota no encontrada o no autorizada."); // Lanzar para detener la ejecución
        }
        return mascota;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        String idMascotaStr = request.getParameter("idMascota");
        Mascota mascota = null; // Declarar mascota fuera del try para que sea accesible

        try {
            mascota = getAndValidateMascota(request, response, usuario, idMascotaStr);
            request.setAttribute("mascota", mascota); // Poner la mascota en el request para el JSP

            // Lógica para cargar una dosis específica si la acción es "edit"
            if ("edit".equals(action)) {
                String idDosisEditStr = request.getParameter("idDosis");
                if (idDosisEditStr != null && !idDosisEditStr.isEmpty()) {
                    int idDosisEdit = Integer.parseInt(idDosisEditStr);
                    Dosis dosisToEdit = dosisDAO.obtenerDosisPorId(idDosisEdit);
                    if (dosisToEdit != null && dosisToEdit.getIdMascota() == mascota.getIdMascota()) {
                        request.setAttribute("dosisToEdit", dosisToEdit); // Pasa la dosis al JSP para rellenar el formulario
                        request.setAttribute("message", "Cargando dosis para edición.");
                        request.setAttribute("messageType", "info");
                    } else {
                        request.setAttribute("message", "Dosis no encontrada o no autorizada para edición.");
                        request.setAttribute("messageType", "danger");
                    }
                } else {
                    request.setAttribute("message", "ID de dosis no especificado para edición.");
                    request.setAttribute("messageType", "warning");
                }
            } else if ("delete".equals(action)) {
                // La eliminación debe hacerse con POST por seguridad.
                request.setAttribute("message", "La eliminación de dosis debe realizarse mediante una solicitud POST.");
                request.setAttribute("messageType", "warning");
            }
            
            // Siempre cargar la lista de dosis para mostrarla en el JSP
            List<Dosis> dosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
            request.setAttribute("listaDosis", dosisList); // Usar "listaDosis" para coincidir con el JSP

            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
            return; // Añadido para detener la ejecución después del forward

        } catch (SQLException ex) {
            ex.printStackTrace();
            request.setAttribute("message", "Error de base de datos al cargar las dosis: " + ex.getMessage());
            request.setAttribute("messageType", "danger");
            // Recargar la lista de dosis para mostrarla junto con el error
            try {
                List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota != null ? mascota.getIdMascota() : 0);
                request.setAttribute("listaDosis", currentDosisList);
            } catch (SQLException innerEx) {
                innerEx.printStackTrace();
                request.setAttribute("listaDosis", new ArrayList<Dosis>()); // Asegurarse de que la lista no sea null
            }
            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
            return; // Añadido para detener la ejecución después del forward
        } catch (ServletException e) { // Capturar la ServletException lanzada por getAndValidateMascota
            // Ya se ha hecho la redirección en getAndValidateMascota, solo se detiene la ejecución aquí.
            return;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            request.setAttribute("message", "ID de dosis inválido para la operación.");
            request.setAttribute("messageType", "danger");
            response.sendRedirect(request.getContextPath() + "/dosis?action=list&idMascota=" + (mascota != null ? mascota.getIdMascota() : "")); // Redirige a la lista
            return;
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado en la solicitud.");
            request.setAttribute("messageType", "danger");
            // Recargar la lista de dosis para mostrarla junto con el error
            try {
                List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota != null ? mascota.getIdMascota() : 0);
                request.setAttribute("listaDosis", currentDosisList);
            } catch (SQLException innerEx) {
                innerEx.printStackTrace();
                request.setAttribute("listaDosis", new ArrayList<Dosis>()); // Asegurarse de que la lista no sea null
            }
            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
            return;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        String idMascotaStr = request.getParameter("idMascota");
        Mascota mascota = null; // Declarar mascota fuera del try para que sea accesible

        try { // Un solo try-catch para la lógica principal del doPost
            mascota = getAndValidateMascota(request, response, usuario, idMascotaStr);
            request.setAttribute("mascota", mascota); // Poner la mascota en el request para el JSP

            // Instanciar SimpleDateFormat una sola vez para las operaciones de fecha en POST
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            switch (action != null ? action : "") {
                case "guardarDosis": // Acción para insertar una nueva dosis
                    String tipoMedicamento = request.getParameter("tipoMedicamento");
                    String dosisCompleta = request.getParameter("dosisCompleta"); // Nuevo campo
                    String frecuencia = request.getParameter("frecuencia");
                    String fechaAdministracionStr = request.getParameter("fechaAdministracion");
                    String notas = request.getParameter("notas");

                    // Validar campos obligatorios (ahora incluyendo dosisCompleta)
                    if (tipoMedicamento == null || tipoMedicamento.trim().isEmpty() ||
                        dosisCompleta == null || dosisCompleta.trim().isEmpty() ||
                        fechaAdministracionStr == null || fechaAdministracionStr.trim().isEmpty()) {
                        request.setAttribute("message", "Los campos obligatorios (Tipo de Medicamento, Dosis y Unidad, Fecha de Administración) deben ser rellenados.");
                        request.setAttribute("messageType", "danger");
                        List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                        request.setAttribute("listaDosis", currentDosisList);
                        request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                        return;
                    }

                    double cantidad = 0.0;
                    String unidad = "";

                    // Lógica para parsear dosisCompleta (ej. "20 mg", "5 ml", "3 gotas")
                    Pattern pattern = Pattern.compile("([0-9]*\\.?[0-9]+)\\s*([a-zA-Z]+)?");
                    Matcher matcher = pattern.matcher(dosisCompleta.trim());

                    if (matcher.matches()) {
                        try {
                            cantidad = Double.parseDouble(matcher.group(1));
                            unidad = matcher.group(2) != null ? matcher.group(2).trim() : "";
                        } catch (NumberFormatException e) {
                            request.setAttribute("message", "La cantidad de dosis debe ser un número válido en el campo 'Dosis y Unidad'.");
                            request.setAttribute("messageType", "danger");
                            List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                            request.setAttribute("listaDosis", currentDosisList);
                            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                            return;
                        }
                    } else {
                        request.setAttribute("message", "Formato de 'Dosis y Unidad' inválido. Use 'cantidad unidad' (ej. '20 mg').");
                        request.setAttribute("messageType", "danger");
                        List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                        request.setAttribute("listaDosis", currentDosisList);
                        request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                        return;
                    }

                    Date fechaAdministracion = null;
                    try {
                        fechaAdministracion = sdf.parse(fechaAdministracionStr);
                    } catch (ParseException e) {
                        request.setAttribute("message", "Formato de fecha inválido. Use AAAA-MM-DD.");
                        request.setAttribute("messageType", "danger");
                        List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                        request.setAttribute("listaDosis", currentDosisList);
                        request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                        return;
                    }

                    Dosis nuevaDosis = new Dosis(mascota.getIdMascota(), tipoMedicamento, cantidad, unidad, frecuencia, fechaAdministracion, notas);

                    if (dosisDAO.insertarDosis(nuevaDosis)) {
                        session.setAttribute("message", "Dosis registrada correctamente.");
                        session.setAttribute("messageType", "success");
                    } else {
                        session.setAttribute("message", "Error al registrar la dosis en la base de datos.");
                        session.setAttribute("messageType", "danger");
                    }
                    response.sendRedirect(request.getContextPath() + "/dosis?action=list&idMascota=" + mascota.getIdMascota());
                    return;

                case "eliminarDosis": // Acción para eliminar una dosis
                    String idDosisStr = request.getParameter("idDosis");
                    if (idDosisStr != null && !idDosisStr.isEmpty()) {
                        int idDosis = Integer.parseInt(idDosisStr);
                        Dosis dosisToDelete = dosisDAO.obtenerDosisPorId(idDosis);
                        if (dosisToDelete != null && dosisToDelete.getIdMascota() == mascota.getIdMascota()) {
                            if (dosisDAO.eliminarDosis(idDosis)) {
                                session.setAttribute("message", "Dosis eliminada correctamente.");
                                session.setAttribute("messageType", "success");
                            } else {
                                session.setAttribute("message", "Error al eliminar la dosis.");
                                session.setAttribute("messageType", "danger");
                            }
                        } else {
                            session.setAttribute("message", "Dosis no encontrada o no autorizada.");
                            session.setAttribute("messageType", "danger");
                        }
                    } else {
                        session.setAttribute("message", "ID de dosis no especificado para eliminar.");
                        session.setAttribute("messageType", "warning");
                    }
                    response.sendRedirect(request.getContextPath() + "/dosis?action=list&idMascota=" + mascota.getIdMascota());
                    return;

                case "actualizarDosis": // Acción para actualizar una dosis existente
                    String idDosisUpdateStr = request.getParameter("idDosis");
                    if (idDosisUpdateStr != null && !idDosisUpdateStr.isEmpty()) {
                        int idDosisUpdate = Integer.parseInt(idDosisUpdateStr);
                        String tipoMedicamentoUpdate = request.getParameter("tipoMedicamento");
                        String dosisCompletaUpdate = request.getParameter("dosisCompleta"); // Nuevo campo
                        String unidadUpdate = ""; // Se extraerá del dosisCompletaUpdate
                        String frecuenciaUpdate = request.getParameter("frecuencia");
                        String fechaAdministracionUpdateStr = request.getParameter("fechaAdministracion");
                        String notasUpdate = request.getParameter("notas");

                        // Validar campos obligatorios
                        if (tipoMedicamentoUpdate == null || tipoMedicamentoUpdate.trim().isEmpty() ||
                            dosisCompletaUpdate == null || dosisCompletaUpdate.trim().isEmpty() ||
                            fechaAdministracionUpdateStr == null || fechaAdministracionUpdateStr.trim().isEmpty()) {
                            request.setAttribute("message", "Todos los campos obligatorios (Tipo de Medicamento, Dosis y Unidad, Fecha de Administración) deben ser rellenados para actualizar.");
                            request.setAttribute("messageType", "danger");
                            List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                            request.setAttribute("listaDosis", currentDosisList);
                            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                            return;
                        }

                        double cantidadUpdate = 0.0;
                        // Lógica para parsear dosisCompletaUpdate
                        Pattern patternUpdate = Pattern.compile("([0-9]*\\.?[0-9]+)\\s*([a-zA-Z]+)?");
                        Matcher matcherUpdate = patternUpdate.matcher(dosisCompletaUpdate.trim());

                        if (matcherUpdate.matches()) {
                            try {
                                cantidadUpdate = Double.parseDouble(matcherUpdate.group(1));
                                unidadUpdate = matcherUpdate.group(2) != null ? matcherUpdate.group(2).trim() : "";
                            } catch (NumberFormatException e) {
                                request.setAttribute("message", "La cantidad de dosis debe ser un número válido en el campo 'Dosis y Unidad' para actualizar.");
                                request.setAttribute("messageType", "danger");
                                List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                                request.setAttribute("listaDosis", currentDosisList);
                                request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                                return;
                            }
                        } else {
                            request.setAttribute("message", "Formato de 'Dosis y Unidad' inválido para actualizar. Use 'cantidad unidad' (ej. '20 mg').");
                            request.setAttribute("messageType", "danger");
                            List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                            request.setAttribute("listaDosis", currentDosisList);
                            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                            return;
                        }

                        Date fechaAdministracionUpdate = null;
                        try {
                            fechaAdministracionUpdate = sdf.parse(fechaAdministracionUpdateStr); // Usando la instancia única
                        } catch (ParseException e) {
                            request.setAttribute("message", "Formato de fecha inválido para actualizar. Use AAAA-MM-DD.");
                            request.setAttribute("messageType", "danger");
                            List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota.getIdMascota());
                            request.setAttribute("listaDosis", currentDosisList);
                            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
                            return;
                        }

                        Dosis dosisToUpdate = new Dosis(idDosisUpdate, mascota.getIdMascota(), tipoMedicamentoUpdate, cantidadUpdate, unidadUpdate, frecuenciaUpdate, fechaAdministracionUpdate, notasUpdate, null);

                        if (dosisDAO.actualizarDosis(dosisToUpdate)) {
                            session.setAttribute("message", "Dosis actualizada correctamente.");
                            session.setAttribute("messageType", "success");
                        } else {
                            session.setAttribute("message", "Error al actualizar la dosis en la base de datos.");
                            session.setAttribute("messageType", "danger");
                        }
                    } else {
                        session.setAttribute("message", "ID de dosis no especificado para actualizar.");
                        session.setAttribute("messageType", "warning");
                    }
                    response.sendRedirect(request.getContextPath() + "/dosis?action=list&idMascota=" + mascota.getIdMascota());
                    return;

                default:
                    session.setAttribute("message", "Acción POST no válida.");
                    session.setAttribute("messageType", "warning");
                    response.sendRedirect(request.getContextPath() + "/dosis?action=list&idMascota=" + mascota.getIdMascota());
                    return;
            }
        } catch (ServletException e) { // Capturar la ServletException lanzada por getAndValidateMascota
            // Ya se ha hecho la redirección en getAndValidateMascota, solo se detiene la ejecución aquí.
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos en DosisServlet (POST): " + e.getMessage());
            request.setAttribute("messageType", "danger");
            // Recargar la lista de dosis para mostrarla junto con el error
            try {
                List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota != null ? mascota.getIdMascota() : 0);
                request.setAttribute("listaDosis", currentDosisList);
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("listaDosis", new ArrayList<Dosis>()); // Asegurarse de que la lista no sea null
            }
            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado al procesar la solicitud.");
            request.setAttribute("messageType", "danger");
            // Recargar la lista de dosis para mostrarla junto con el error
            try {
                List<Dosis> currentDosisList = dosisDAO.obtenerDosisPorMascota(mascota != null ? mascota.getIdMascota() : 0);
                request.setAttribute("listaDosis", currentDosisList);
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("listaDosis", new ArrayList<Dosis>()); // Asegurarse de que la lista no sea null
            }
            request.getRequestDispatcher("/dosisForm.jsp").forward(request, response);
            return;
        }
    }
}
