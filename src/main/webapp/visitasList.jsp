<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.VisitaVeterinario" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Visitas Veterinarias - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<%
    Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
    if (usuarioActual == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Mascota mascota = (Mascota) request.getAttribute("mascota");
    if (mascota == null) {
        session.setAttribute("message", "No se ha especificado una mascota para ver sus visitas.");
        session.setAttribute("messageType", "danger");
        response.sendRedirect(request.getContextPath() + "/MascotaServlet");
        return;
    }

    List<VisitaVeterinario> listaVisitas = (List<VisitaVeterinario>) request.getAttribute("listaVisitas");
    if (listaVisitas == null) {
        listaVisitas = new java.util.ArrayList<>(); // Asegurarse de que no sea null
    }

    // Mensaje de éxito o error (si existe)
    String message = (String) session.getAttribute("message");
    String messageType = (String) session.getAttribute("messageType");
    if (message != null && !message.isEmpty()) { %>
        <div class="alert alert-<%= messageType %> alert-dismissible fade show fixed-top-alert" role="alert">
            <%= message %>
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    <%
        session.removeAttribute("message");
        session.removeAttribute("messageType");
    }
%>
<body class="dog-theme"> <%-- Puedes ajustar el tema si lo deseas, o hacerlo dinámico --%>
    <header class="app-header">
        <div class="text-center">
            <span class="pet-icon">🩺</span> <%-- Icono para visitas veterinarias --%>
            <h1>Visitas Veterinarias de <%= mascota.getNombre() %></h1>
            <p>Historial médico completo de tu mascota.</p>
        </div>
    </header>

    <main class="container mt-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <a href="<%= request.getContextPath() %>/VisitaVeterinarioServlet?action=mostrarFormulario&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-success btn-lg">
                <i class="fas fa-plus-circle"></i> Añadir Nueva Visita
            </a>
            <a href="<%= request.getContextPath() %>/MascotaServlet" class="btn btn-secondary btn-lg">
                <i class="fas fa-arrow-left"></i> Volver al Panel de Mascotas
            </a>
        </div>

        <% if (listaVisitas.isEmpty()) { %>
            <div class="alert alert-info text-center" role="alert">
                Aún no hay visitas registradas para <%= mascota.getNombre() %>. ¡Añade la primera!
            </div>
        <% } else { %>
            <div class="table-responsive">
                <table class="table table-hover table-striped table-bordered align-middle">
                    <thead class="table-dark">
                        <tr>
                            <th>Fecha</th>
                            <th>Motivo</th>
                            <th>Diagnóstico</th>
                            <th>Tratamiento</th>
                            <th>Medicamentos</th>
                            <th>Costo (€)</th>
                            <th>Observaciones</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                           for (VisitaVeterinario visita : listaVisitas) { %>
                            <tr>
                                <td><%= sdf.format(visita.getFechaVisita()) %></td>
                                <td><%= visita.getMotivo() != null ? visita.getMotivo() : "" %></td>
                                <td><%= visita.getDiagnostico() != null ? visita.getDiagnostico() : "" %></td>
                                <td><%= visita.getTratamiento() != null ? visita.getTratamiento() : "" %></td>
                                <td><%= visita.getMedicamentosRecetados() != null ? visita.getMedicamentosRecetados() : "" %></td>
                                <td><%= visita.getCosto() != null ? String.format(Locale.US, "%.2f", visita.getCosto()) : "0.00" %></td>
                                <td><%= visita.getObservaciones() != null ? visita.getObservaciones() : "" %></td>
                                <td class="text-nowrap">
                                    <a href="<%= request.getContextPath() %>/VisitaVeterinarioServlet?action=mostrarFormulario&idMascota=<%= mascota.getIdMascota() %>&idVisita=<%= visita.getIdVisita() %>" class="btn btn-sm btn-primary me-1" title="Editar">
                                        <i class="fas fa-edit"></i>
                                    </a>
                                    <a href="<%= request.getContextPath() %>/VisitaVeterinarioServlet?action=eliminarConfirmar&idMascota=<%= mascota.getIdMascota() %>&idVisita=<%= visita.getIdVisita() %>" class="btn btn-sm btn-danger" title="Eliminar">
                                        <i class="fas fa-trash-alt"></i>
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
