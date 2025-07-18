<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Period" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Usuario - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
    <%-- Los estilos específicos de este JSP se han movido a style.css --%>
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

    <main class="container mt-5 panel-container"> <%-- Contenedor principal para el panel --%>
        <%
            Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
            if (usuarioActual == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp"); // Usar contextPath
                return;
            }
        %>
        <h2 class="text-center mb-4">Panel de Mascotas de <%= usuarioActual.getNombre() %></h2>

        <%-- Mensaje de éxito o error (si existe) --%>
        <% String message = (String) session.getAttribute("message");
           String messageType = (String) session.getAttribute("messageType");
           if (message != null && !message.isEmpty()) { %>
            <div class="alert alert-<%= messageType %> alert-dismissible fade show" role="alert">
                <%= message %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <%
                session.removeAttribute("message"); // Limpiar el mensaje después de mostrarlo
                session.removeAttribute("messageType");
           }
        %>

        <div class="text-center mb-4">
            <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario" class="btn btn-primary btn-lg">
                <i class="fas fa-plus-circle"></i> Añadir Nueva Mascota
            </a>
        </div>

        <div class="row">
            <%
                List<Mascota> mascotas = (List<Mascota>) request.getAttribute("listaMascotas"); // Cambiado de "mascotas" a "listaMascotas"
                if (mascotas != null && !mascotas.isEmpty()) {
                    for (Mascota mascota : mascotas) {
                        // Calcular edad a partir de fechaNacimiento
                        String edadDisplay = "Fecha de Nacimiento no disponible";
                        if (mascota.getFechaNacimiento() != null) {
                            LocalDate fechaNacimientoLocal = mascota.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDate fechaActual = LocalDate.now();
                            Period periodo = Period.between(fechaNacimientoLocal, fechaActual);
                            int anos = periodo.getYears();
                            int meses = periodo.getMonths();
                            edadDisplay = anos + " años";
                            if (meses > 0) {
                                edadDisplay += " " + meses + " meses";
                            }
                        }
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); // Formato para mostrar la fecha de nacimiento
            %>
                        <div class="col-md-6 col-lg-4 mb-4"> <%-- Añadido mb-4 para margen inferior entre tarjetas --%>
                            <div class="card mascota-card h-100"> <%-- Añadido h-100 para alturas iguales --%>
                                <div class="card-body d-flex flex-column"> <%-- Flexbox para alinear contenido --%>
                                    <h5 class="card-title text-primary"><%= mascota.getNombre() %></h5>
                                    <p class="card-text"><strong>Tipo:</strong> <%= mascota.getTipo() %></p>
                                    <p class="card-text"><strong>Raza:</strong> <%= mascota.getRaza() %></p>
                                    <p class="card-text"><strong>Sexo:</strong> <%= mascota.getSexo() %></p>
                                    <p class="card-text"><strong>Nacimiento:</strong> <%= mascota.getFechaNacimiento() != null ? sdf.format(mascota.getFechaNacimiento()) : "N/A" %></p>
                                    <p class="card-text"><strong>Edad:</strong> <%= edadDisplay %></p>
                                    <p class="card-text"><strong>Peso:</strong> <%= String.format("%.1f", mascota.getPesoKg()).replace(",", ".") %> kg</p>
                                    <p class="card-text"><strong>Esterilizado:</strong> <%= mascota.isEsterilizado() ? "Sí" : "No" %></p>
                                    <p class="card-text"><strong>Nivel de Actividad:</strong> <%= mascota.getNivelActividad() %></p>
                                    <p class="card-text"><strong>Condición de Salud:</strong> <%= mascota.getCondicionSalud() %></p>
                                    <hr>
                                    <%-- --- SECCIÓN DE CÁLCULO DE CALORÍAS --- --%>
                                    <h6 class="card-subtitle mb-2 text-muted">Calorías Diarias Necesarias:</h6>
                                    <p class="card-text">
                                        <strong class="text-success fs-5"><%= String.format("%.0f", mascota.calcularCaloriasNecesarias()) %> kcal/día</strong>
                                    </p>
                                    <%-- --- FIN SECCIÓN DE CÁLCULO DE CALORÍAS --- --%>

                                    <div class="mt-auto d-flex flex-column gap-2"> <%-- mt-auto para empujar botones abajo, flex-column y gap para apilar --%>
                                        <a href="<%= request.getContextPath() %>/MascotaServlet?action=editar&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-warning btn-sm">
                                            Editar Mascota
                                        </a>

                                        <%-- Botón para Dosis --%>
                                        <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormularioDosis&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-info btn-sm">
                                            Ver Dosis
                                        </a>
                                        <%-- Botón para Calculadora de Comida --%>
                                        <a href="<%= request.getContextPath() %>/CalculadoraComidaServlet?idMascota=<%= mascota.getIdMascota() %>" class="btn btn-success btn-sm">
                                            Calcular Comida
                                        </a>

                                        <button type="button" class="btn btn-danger btn-sm" data-bs-toggle="modal" data-bs-target="#confirmDeleteModal" data-id="<%= mascota.getIdMascota() %>" data-nombre="<%= mascota.getNombre() %>">
                                            Eliminar Mascota
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
            <%
                    }
                } else {
            %>
                    <div class="col-12">
                        <div class="no-mascotas-message">
                            <p class="lead">Aún no tienes mascotas registradas. ¡Añade una para empezar a calcular sus necesidades nutricionales!</p>
                            <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario" class="btn btn-info btn-lg mt-3">
                                <i class="fas fa-plus-circle"></i> Registrar Primera Mascota
                            </a>
                        </div>
                    </div>
            <%
                }
            %>
        </div>
    </main>

    <%-- Modal de Confirmación de Eliminación --%>
    <div class="modal fade" id="confirmDeleteModal" tabindex="-1" aria-labelledby="confirmDeleteModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="confirmDeleteModalLabel">Confirmar Eliminación</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    ¿Estás seguro de que deseas eliminar a <strong id="mascotaNombreEliminar"></strong>? Esta acción no se puede deshacer.
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <a id="confirmDeleteButton" href="#" class="btn btn-danger">Eliminar</a>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Script para manejar el modal de confirmación de eliminación
        var confirmDeleteModal = document.getElementById('confirmDeleteModal');
        confirmDeleteModal.addEventListener('show.bs.modal', function (event) {
            // Botón que disparó el modal
            var button = event.relatedTarget;
            // Extraer información de los atributos data-*
            var mascotaId = button.getAttribute('data-id');
            var mascotaNombre = button.getAttribute('data-nombre');

            // Actualizar el contenido del modal
            var modalBodyMascotaNombre = confirmDeleteModal.querySelector('#mascotaNombreEliminar');
            var confirmDeleteButton = confirmDeleteModal.querySelector('#confirmDeleteButton');

            modalBodyMascotaNombre.textContent = mascotaNombre;
            confirmDeleteButton.href = '<%= request.getContextPath() %>/MascotaServlet?action=eliminar&idMascota=' + mascotaId;
        });
    </script>
</body>
</html>
