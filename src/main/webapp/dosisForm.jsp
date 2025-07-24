<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="com.calculadoraperros.web.modelo.Dosis" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %> <%-- Importar la clase Usuario --%>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formulario de Dosis - Calculadora de Perros</title>
    
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css"> <%-- Ahora todos los estilos están aquí --%>
</head>
<body>
    <%
        // Obtener el usuario de la sesión una sola vez al principio del JSP
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        // Redirigir si el usuario no ha iniciado sesión
        if (usuarioActual == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return; // Detener la ejecución del JSP
        }
    %>

    <header class="app-header"> <%-- Barra superior --%>
        <div class="text-center">
            <span class="dog-icon">🐾</span>
            <h1>Calculadora de Perros</h1>
            <p>Herramientas útiles para el cuidado de tu mascota.</p>
        </div>
        <nav class="navbar navbar-expand-lg navbar-light bg-light mt-3"> <%-- Barra de navegación --%>
            <div class="container-fluid">
                <a class="navbar-brand" href="<%= request.getContextPath() %>/index.jsp">Calculadoras</a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav ms-auto">
                        <%
                            // Usar la variable usuarioActual ya declarada
                            if (usuarioActual != null) {
                        %>
                                <li class="nav-item">
                                    <a class="nav-link" href="<%= request.getContextPath() %>/MascotaServlet">Panel de <%= usuarioActual.getNombre() %></a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" href="<%= request.getContextPath() %>/LogoutServlet">Cerrar Sesión</a>
                                </li>
                        <%
                            } else {
                        %>
                                <li class="nav-item">
                                    <a class="nav-link" href="<%= request.getContextPath() %>/login.jsp">Iniciar Sesión</a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link" href="<%= request.getContextPath() %>/registro.jsp">Registrarse</a>
                                </li>
                        <%
                            }
                        %>
                    </ul>
                </div>
            </div>
        </nav>
    </header>

    <div class="container mt-5 dosis-form-container">
        <%
            Mascota mascota = (Mascota) request.getAttribute("mascota");
            Dosis dosisToEdit = (Dosis) request.getAttribute("dosisToEdit"); // Objeto Dosis para edición
            boolean isEditing = (dosisToEdit != null && dosisToEdit.getIdDosis() != 0); // Verificar si estamos editando
            
            String formTitle = isEditing ? "🐾 Editar Dosis para " : "🐾 Registrar Dosis para ";
            formTitle += (mascota != null ? mascota.getNombre() : "Mascota Desconocida");
            
            String actionValue = isEditing ? "actualizarDosis" : "guardarDosis";
            String submitButtonText = isEditing ? "Actualizar Dosis" : "Registrar Dosis";

            // La comprobación de sesión ya se hizo al principio del JSP, así que no es necesaria aquí.
        %>
        <h2 class="text-center mb-4"><%= formTitle %></h2>

        <%-- Mensaje de error o éxito (si existe) --%>
        <% String message = (String) request.getAttribute("message");
           String messageType = (String) request.getAttribute("messageType");
           if (message != null && !message.isEmpty()) { %>
            <div class="alert alert-<%= messageType %> alert-dismissible fade show" role="alert">
                <%= message %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <% } %>

        <% if (mascota != null) { %>
            <p>Aquí puedes registrar o calcular la dosis para **<%= mascota.getNombre() %>** (ID: <%= mascota.getIdMascota() %>).</p>
            <p>Peso actual: <%= String.format("%.1f", mascota.getPeso()).replace(",", ".") %> kg</p>
        <% } else { %>
            <p>No se pudo cargar la información de la mascota.</p>
        <% } %>

        <form action="<%= request.getContextPath() %>/dosis" method="post">
            <input type="hidden" name="action" value="<%= actionValue %>">
            <input type="hidden" name="idMascota" value="<%= mascota != null ? mascota.getIdMascota() : "" %>">
            <% if (isEditing) { %>
                <input type="hidden" name="idDosis" value="<%= dosisToEdit.getIdDosis() %>">
            <% } %>

            <div class="mb-3">
                <label for="tipoMedicamento" class="form-label">Tipo de Medicamento:</label>
                <input type="text" class="form-control" id="tipoMedicamento" name="tipoMedicamento" 
                    value="<%= isEditing ? dosisToEdit.getTipoMedicamento() : "" %>" required>
            </div>

            <%-- CAMBIO: Unificar Cantidad y Unidad en un solo campo --%>
            <div class="mb-3">
                <label for="dosisCompleta" class="form-label">Dosis y Unidad (ej. 20 mg, 5 ml, 3 gotas):</label>
                <input type="text" class="form-control" id="dosisCompleta" name="dosisCompleta" 
                    placeholder="Ej: 20 mg, 5 ml, 3 gotas" 
                    value="<%= (isEditing && dosisToEdit.getCantidad() != 0) ? String.format("%.2f %s", dosisToEdit.getCantidad(), dosisToEdit.getUnidad()).replace(",", ".") : "" %>" required>
            </div>

            <div class="mb-3">
                <label for="frecuencia" class="form-label">Frecuencia (ej. cada 8 horas, una vez al día):</label>
                <input type="text" class="form-control" id="frecuencia" name="frecuencia" 
                    value="<%= isEditing ? dosisToEdit.getFrecuencia() : "" %>">
            </div>

            <div class="mb-3">
                <label for="fechaAdministracion" class="form-label">Fecha de Administración:</label>
                <% 
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String defaultDate = "";
                    if (isEditing && dosisToEdit.getFechaAdministracion() != null) {
                        defaultDate = sdf.format(dosisToEdit.getFechaAdministracion());
                    } else {
                        defaultDate = sdf.format(new Date()); // Fecha actual por defecto para nuevas dosis
                    }
                %>
                <input type="date" class="form-control" id="fechaAdministracion" name="fechaAdministracion" 
                    value="<%= defaultDate %>" required>
            </div>

            <div class="mb-3">
                <label for="notas" class="form-label">Notas Adicionales:</label>
                <textarea class="form-control" id="notas" name="notas" rows="3"><%= isEditing ? dosisToEdit.getNotas() : "" %></textarea>
            </div>

            <button type="submit" class="btn btn-success"><%= submitButtonText %></button>
            <a href="<%= request.getContextPath() %>/MascotaServlet" class="btn btn-secondary">Volver a mis Mascotas</a>
        </form>

        <hr class="my-4">

        <h3>Dosis Registradas para <%= mascota != null ? mascota.getNombre() : "esta mascota" %>:</h3>
        <table class="table table-bordered table-striped mt-3">
            <thead>
                <tr>
                    <th>Tipo Medicamento</th>
                    <th>Cantidad</th>
                    <th>Unidad</th>
                    <th>Frecuencia</th>
                    <th>Fecha Admin.</th>
                    <th>Notas</th>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
                <% 
                    List<Dosis> listaDosis = (List<Dosis>) request.getAttribute("listaDosis");
                    if (listaDosis != null && !listaDosis.isEmpty()) {
                        for (Dosis dosis : listaDosis) {
                %>
                        <tr>
                            <td><%= dosis.getTipoMedicamento() %></td>
                            <td><%= String.format("%.2f", dosis.getCantidad()).replace(",", ".") %></td>
                            <td><%= dosis.getUnidad() %></td>
                            <td><%= dosis.getFrecuencia() %></td>
                            <td><%= sdf.format(dosis.getFechaAdministracion()) %></td>
                            <td><%= dosis.getNotas() %></td>
                            <td>
                                <form action="<%= request.getContextPath() %>/dosis" method="get" style="display:inline;">
                                    <input type="hidden" name="action" value="edit">
                                    <input type="hidden" name="idMascota" value="<%= mascota.getIdMascota() %>">
                                    <input type="hidden" name="idDosis" value="<%= dosis.getIdDosis() %>">
                                    <button type="submit" class="btn btn-sm btn-info me-2">Editar</button>
                                </form>
                                <form action="<%= request.getContextPath() %>/dosis" method="post" style="display:inline;" onsubmit="return confirm('¿Estás seguro de eliminar esta dosis?');">
                                    <input type="hidden" name="action" value="eliminarDosis">
                                    <input type="hidden" name="idMascota" value="<%= mascota.getIdMascota() %>">
                                    <input type="hidden" name="idDosis" value="<%= dosis.getIdDosis() %>">
                                    <button type="submit" class="btn btn-sm btn-danger">Eliminar</button>
                                </form>
                            </td>
                        </tr>
                <% 
                        }
                    } else {
                %>
                    <tr>
                        <td colspan="7">No hay dosis registradas para esta mascota.</td>
                    </tr>
                <%
                    }
                %>
            </tbody>
        </table>

    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Script para establecer la fecha actual por defecto en el campo de fecha
        window.onload = function() {
            var fechaInput = document.getElementById('fechaAdministracion');
            // Solo si el valor no ha sido ya establecido por el servidor (en caso de edición, por ejemplo)
            if (!fechaInput.value) { 
                var today = new Date();
                var dd = String(today.getDate()).padStart(2, '0');
                var mm = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
                var yyyy = today.getFullYear();
                var formattedDate = yyyy + '-' + mm + '-' + dd;
                fechaInput.value = formattedDate;
            }
        };
    </script>
</body>
</html>
