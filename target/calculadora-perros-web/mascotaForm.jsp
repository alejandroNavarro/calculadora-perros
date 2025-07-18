<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gestión de Mascota - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
    <style>
        /* Estilos específicos para el contenedor del formulario de mascota */
        .mascota-form-container { /* Renombrado para consistencia */
            max-width: 600px;
            padding: 30px;
            background-color: #ffffff;
            border-radius: 15px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
            width: 100%;
            margin-top: 20px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
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
                            Usuario usuarioActualNav = (Usuario) session.getAttribute("usuario");
                            if (usuarioActualNav != null) {
                        %>
                                <li class="nav-item">
                                    <a class="nav-link" href="<%= request.getContextPath() %>/MascotaServlet">Panel de <%= usuarioActualNav.getNombre() %></a>
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

    <div class="mascota-form-container">
        <%
            // Obtener el objeto Mascota del request (si estamos editando)
            Mascota mascotaParaEditar = (Mascota) request.getAttribute("mascota");
            boolean isEditing = (mascotaParaEditar != null);
            String formTitle = isEditing ? "🐾 Editar Mascota" : "🐾 Registrar Nueva Mascota";
            String actionValue = isEditing ? "actualizar" : "agregar"; // Cambiado de "registrar" a "agregar"
            String submitButtonText = isEditing ? "Actualizar Mascota" : "Registrar Mascota";

            // Verificar si el usuario ha iniciado sesión
            Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
            if (usuarioActual == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp"); // Usar contextPath
                return;
            }
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

        <form action="<%= request.getContextPath() %>/MascotaServlet" method="post">
            <input type="hidden" name="action" value="<%= actionValue %>">
            <% if (isEditing) { %>
                <input type="hidden" name="idMascota" value="<%= mascotaParaEditar.getIdMascota() %>">
            <% } %>

            <div class="mb-3">
                <label for="nombreMascota" class="form-label">Nombre de la Mascota</label>
                <input type="text" class="form-control" id="nombreMascota" name="nombre" value="<%= isEditing ? mascotaParaEditar.getNombre() : "" %>" required>
            </div>
            
            <div class="mb-3">
                <label class="form-label">Sexo</label>
                <div class="radio-group justify-content-start">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="sexo" id="sexoMacho" value="Macho" <%= isEditing && "Macho".equalsIgnoreCase(mascotaParaEditar.getSexo()) ? "checked" : "" %> required>
                        <label class="form-check-label" for="sexoMacho">Macho</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="sexo" id="sexoHembra" value="Hembra" <%= isEditing && "Hembra".equalsIgnoreCase(mascotaParaEditar.getSexo()) ? "checked" : "" %>>
                        <label class="form-check-label" for="sexoHembra">Hembra</label>
                    </div>
                </div>
            </div>

            <div class="mb-3">
                <label for="fechaNacimientoMascota" class="form-label">Fecha de Nacimiento</label>
                <% 
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String fechaNacimientoDefault = "";
                    if (isEditing && mascotaParaEditar.getFechaNacimiento() != null) {
                        fechaNacimientoDefault = sdf.format(mascotaParaEditar.getFechaNacimiento());
                    } else {
                        fechaNacimientoDefault = sdf.format(new Date()); // Fecha actual por defecto para nuevas mascotas
                    }
                %>
                <input type="date" class="form-control" id="fechaNacimientoMascota" name="fechaNacimiento" value="<%= fechaNacimientoDefault %>" required>
            </div>

            <div class="mb-3">
                <label for="razaMascota" class="form-label">Raza</label>
                <input type="text" class="form-control" id="razaMascota" name="raza" placeholder="Ej: Labrador, Mestizo" value="<%= isEditing ? mascotaParaEditar.getRaza() : "" %>" required>
            </div>
            <div class="mb-3">
                <label for="pesoKgMascota" class="form-label">Peso (kg)</label>
                <input type="number" class="form-control" id="pesoKgMascota" name="pesoKg" step="0.1" min="0.1" value="<%= isEditing ? String.format("%.1f", mascotaParaEditar.getPesoKg()).replace(",", ".") : "" %>" required>
            </div>
            <div class="mb-3">
                <label class="form-label">¿Está esterilizado/a?</label>
                <div class="radio-group justify-content-start">
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="esterilizado" id="esterilizadoSiMascota" value="true" <%= isEditing && mascotaParaEditar.isEsterilizado() ? "checked" : "" %> required>
                        <label class="form-check-label" for="esterilizadoSiMascota">Sí</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input class="form-check-input" type="radio" name="esterilizado" id="esterilizadoNoMascota" value="false" <%= isEditing && !mascotaParaEditar.isEsterilizado() ? "checked" : "" %> >
                        <label class="form-check-label" for="esterilizadoNoMascota">No</label>
                    </div>
                </div>
            </div>

            <%-- --- NUEVOS CAMPOS DE SELECCIÓN --- --%>
            <div class="mb-3">
                <label for="tipoMascota" class="form-label">Tipo de Mascota</label>
                <select class="form-select" id="tipoMascota" name="tipo" required>
                    <option value="">Seleccione...</option>
                    <option value="Perro" <%= isEditing && "Perro".equalsIgnoreCase(mascotaParaEditar.getTipo()) ? "selected" : "" %>>Perro</option>
                    <option value="Gato" <%= isEditing && "Gato".equalsIgnoreCase(mascotaParaEditar.getTipo()) ? "selected" : "" %>>Gato</option>
                </select>
            </div>

            <div class="mb-3">
                <label for="nivelActividadMascota" class="form-label">Nivel de Actividad</label>
                <select class="form-select" id="nivelActividadMascota" name="nivelActividad" required>
                    <option value="">Seleccione...</option>
                    <option value="Bajo" <%= isEditing && "Bajo".equalsIgnoreCase(mascotaParaEditar.getNivelActividad()) ? "selected" : "" %>>Bajo (Sedentario)</option>
                    <option value="Normal" <%= isEditing && "Normal".equalsIgnoreCase(mascotaParaEditar.getNivelActividad()) ? "selected" : "" %>>Normal (Paseos diarios)</option>
                    <option value="Activo" <%= isEditing && "Activo".equalsIgnoreCase(mascotaParaEditar.getNivelActividad()) ? "selected" : "" %>>Activo (Juegos intensos, deporte)</option>
                    <option value="Muy Activo" <%= isEditing && "Muy Activo".equalsIgnoreCase(mascotaParaEditar.getNivelActividad()) ? "selected" : "" %>>Muy Activo (Trabajo, alta competición)</option>
                </select>
            </div>

            <div class="mb-3">
                <label for="condicionSaludMascota" class="form-label">Condición de Salud / Etapa de Vida</label>
                <select class="form-select" id="condicionSaludMascota" name="condicionSalud" required>
                    <option value="">Seleccione...</option>
                    <option value="Saludable" <%= isEditing && "Saludable".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Saludable (Adulto)</option>
                    <option value="Cachorro (0-4 meses)" <%= isEditing && "Cachorro (0-4 meses)".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Cachorro (0-4 meses)</option>
                    <option value="Cachorro (4-8 meses)" <%= isEditing && "Cachorro (4-8 meses)".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Cachorro (4-8 meses)</option>
                    <option value="Cachorro (8-12 meses)" <%= isEditing && "Cachorro (8-12 meses)".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Cachorro (8-12 meses)</option>
                    <option value="Senior" <%= isEditing && "Senior".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Senior</option>
                    <option value="Obesidad" <%= isEditing && "Obesidad".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Obesidad / Sobrepeso</option>
                    <option value="Bajo Peso" <%= isEditing && "Bajo Peso".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Bajo Peso</option>
                    <option value="Gestacion Temprana" <%= isEditing && "Gestacion Temprana".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Gestación (1er tercio)</option>
                    <option value="Gestacion Tardia" <%= isEditing && "Gestacion Tardia".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Gestación (Último tercio)</option>
                    <option value="Lactancia Inicial" <%= isEditing && "Lactancia Inicial".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Lactancia (Semanas 1-3)</option>
                    <option value="Lactancia Pico" <%= isEditing && "Lactancia Pico".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Lactancia (Semanas 4-5 - Pico)</option>
                    <option value="Lactancia Tardia" <%= isEditing && "Lactancia Tardia".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Lactancia (Después del pico)</option>
                    <option value="Enfermedad" <%= isEditing && "Enfermedad".equalsIgnoreCase(mascotaParaEditar.getCondicionSalud()) ? "selected" : "" %>>Enfermedad (Requiere consulta veterinaria)</option>
                </select>
            </div>
            <%-- --- FIN NUEVOS CAMPOS DE SELECCIÓN --- --%>

            <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-lg"><%= submitButtonText %></button>
                <a href="<%= request.getContextPath() %>/MascotaServlet" class="btn btn-secondary btn-lg">Volver al Panel</a>
            </div>
        </form>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Script para establecer la fecha actual por defecto en el campo de fecha de nacimiento
        window.onload = function() {
            var fechaInput = document.getElementById('fechaNacimientoMascota');
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
