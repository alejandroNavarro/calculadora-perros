<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.VisitaVeterinario" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Locale" %> <%-- ¡IMPORTACIÓN AÑADIDA! --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Confirmar Eliminación de Visita - Calculadora Perros</title>
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

    Mascota mascota = (Mascota) request.getAttribute("mascota"); // La mascota asociada a la visita
    VisitaVeterinario visitaAEliminar = (VisitaVeterinario) request.getAttribute("visitaAEliminar");

    if (mascota == null || visitaAEliminar == null) {
        session.setAttribute("message", "No se ha especificado la visita o mascota para eliminar.");
        session.setAttribute("messageType", "danger");
        response.sendRedirect(request.getContextPath() + "/MascotaServlet"); // Redirige al panel general
        return;
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
%>
<body class="dog-theme">
    <header class="app-header">
        <div class="text-center">
            <span class="pet-icon">🩺</span>
            <h1>Confirmar Eliminación de Visita</h1>
            <p>Estás a punto de eliminar un registro.</p>
        </div>
    </header>

    <main class="container mt-5">
        <div class="form-card mx-auto text-center" style="max-width: 600px;">
            <h2 class="mb-4 text-danger">¿Estás seguro de que quieres eliminar esta visita?</h2>
            <p class="lead">Esta acción es irreversible.</p>
            
            <div class="card mb-4">
                <div class="card-body text-start">
                    <h5 class="card-title">Detalles de la Visita a Eliminar:</h5>
                    <ul class="list-unstyled">
                        <li><strong>Mascota:</strong> <%= mascota.getNombre() %></li>
                        <li><strong>Fecha:</strong> <%= sdf.format(visitaAEliminar.getFechaVisita()) %></li>
                        <li><strong>Motivo:</strong> <%= visitaAEliminar.getMotivo() %></li>
                        <% if (visitaAEliminar.getDiagnostico() != null && !visitaAEliminar.getDiagnostico().isEmpty()) { %>
                            <li><strong>Diagnóstico:</strong> <%= visitaAEliminar.getDiagnostico() %></li>
                        <% } %>
                        <% if (visitaAEliminar.getCosto() != null) { %>
                            <li><strong>Costo:</strong> <%= String.format(Locale.US, "%.2f", visitaAEliminar.getCosto()) %> €</li>
                        <% } %>
                    </ul>
                </div>
            </div>

            <form action="<%= request.getContextPath() %>/VisitaVeterinarioServlet" method="post">
                <input type="hidden" name="action" value="eliminar">
                <input type="hidden" name="idVisita" value="<%= visitaAEliminar.getIdVisita() %>">
                <input type="hidden" name="idMascota" value="<%= mascota.getIdMascota() %>">
                <div class="d-flex justify-content-center gap-3">
                    <button type="submit" class="btn btn-danger btn-lg">
                        <i class="fas fa-trash-alt"></i> Sí, Eliminar Visita
                    </button>
                    <a href="<%= request.getContextPath() %>/VisitaVeterinarioServlet?action=listar&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-secondary btn-lg">
                        <i class="fas fa-times-circle"></i> Cancelar
                    </a>
                </div>
            </form>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
