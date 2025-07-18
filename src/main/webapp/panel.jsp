<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Period" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Usuario - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
    <header class="app-header">
        <div class="text-center">
            <span class="dog-icon">🐾</span>
            <h1>Calculadora de Perros</h1>
            <p>Herramientas útiles para el cuidado de tu mascota.</p>
        </div>
        <nav class="navbar navbar-expand-lg navbar-light bg-light mt-3">
            <div class="container-fluid">
                <a class="navbar-brand" href="<%= request.getContextPath() %>/index.jsp">Calculadoras</a>
                <button type="button" class="navbar-toggler" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
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

    <main class="container mt-5 panel-container">
        <%
            Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
            if (usuarioActual == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }

            // Las mascotas y las calorías ya vienen en el request desde MascotaServlet
            // La lista de mascotas también puede estar en la sesión (session.getAttribute("mascotasUsuario"))
            List<Mascota> mascotas = (List<Mascota>) request.getAttribute("listaMascotas");
            Map<Integer, Double> caloriasPorMascota = (Map<Integer, Double>) request.getAttribute("caloriasPorMascota");

            // Asegurarse de que las listas no sean null
            if (mascotas == null) {
                mascotas = new ArrayList<>();
            }
            if (caloriasPorMascota == null) {
                caloriasPorMascota = new HashMap<>();
            }

            // Mensaje de éxito o error (si existe) del servlet o de la propia carga del JSP
            String sessionMessage = (String) session.getAttribute("message");
            String sessionMessageType = (String) session.getAttribute("messageType");
            
            if (sessionMessage != null && !sessionMessage.isEmpty()) { %>
                <div class="alert alert-<%= sessionMessageType %> alert-dismissible fade show" role="alert">
                    <%= sessionMessage %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            <%
                session.removeAttribute("message");
                session.removeAttribute("messageType");
            } else { // Si no hay mensaje de sesión, verificar si hay mensaje de error del request (ej. de MascotaServlet)
                String requestMessage = (String) request.getAttribute("message");
                String requestMessageType = (String) request.getAttribute("messageType");
                if (requestMessage != null && !requestMessage.isEmpty()) {
            %>
                    <div class="alert alert-<%= requestMessageType %> alert-dismissible fade show" role="alert">
                        <%= requestMessage %>
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    </div>
            <%
                }
            }
            %>

        <h2 class="text-center mb-4">Panel de Mascotas de <%= usuarioActual.getNombre() %></h2>

        <div class="text-center mb-4">
            <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario" class="btn btn-primary btn-lg">
                <i class="fas fa-plus-circle"></i> Añadir Nueva Mascota
            </a>
        </div>

        <div class="row">
            <%
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
                        
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            %>
                        <div class="col-md-6 col-lg-4 mb-4">
                            <div class="card mascota-card h-100">
                                <div class="card-body d-flex flex-column">
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
                                        <strong class="text-success fs-5">
                                            <%
                                                Double calorias = caloriasPorMascota.get(mascota.getIdMascota());
                                                if (calorias != null) {
                                                    out.print(String.format("%.0f", calorias) + " kcal/día");
                                                } else {
                                                    out.print("N/A");
                                                }
                                            %>
                                        </strong>
                                    </p>
                                    <%-- --- FIN SECCIÓN DE CÁLCULO DE CALORÍAS --- --%>

                                    <div class="mt-auto d-flex flex-column gap-2">
                                        <a href="<%= request.getContextPath() %>/MascotaServlet?action=editar&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-warning btn-sm">
                                            Editar Mascota
                                        </a>

                                        <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormularioDosis&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-info btn-sm">
                                            Ver Dosis
                                        </a>
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
        var confirmDeleteModal = document.getElementById('confirmDeleteModal');
        confirmDeleteModal.addEventListener('show.bs.modal', function (event) {
            var button = event.relatedTarget;
            var mascotaId = button.getAttribute('data-id');
            var mascotaNombre = button.getAttribute('data-nombre');

            var modalBodyMascotaNombre = confirmDeleteModal.querySelector('#mascotaNombreEliminar');
            var confirmDeleteButton = confirmDeleteModal.querySelector('#confirmDeleteButton');

            modalBodyMascotaNombre.textContent = mascotaNombre;
            confirmDeleteButton.href = '<%= request.getContextPath() %>/MascotaServlet?action=eliminar&idMascota=' + mascotaId;
        });
    </script>
</body>
</html>
