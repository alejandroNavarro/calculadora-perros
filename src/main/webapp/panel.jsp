<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Calendar" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Usuario - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <%!
    // Función auxiliar para calcular la edad a partir de la fecha de nacimiento
    public int calculateAge(java.util.Date birthDate) {
        if (birthDate == null) {
            return 0; // Retorna 0 si la fecha de nacimiento es nula
        }
        Calendar dob = Calendar.getInstance();
        dob.setTime(birthDate);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
    %>

    <%
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
        if (usuarioActual == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        List<Mascota> mascotas = (List<Mascota>) request.getAttribute("listaMascotas");
        if (mascotas == null) {
            mascotas = new java.util.ArrayList<>();
        }

        System.out.println("DEBUG en panel.jsp: Tamaño de la lista de mascotas: " + mascotas.size());

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

    <div class="top-bar"> <%-- Barra superior con botón de toggle --%>
        <button class="sidebar-toggle" id="sidebarToggle">
            <i class="fas fa-bars"></i>
        </button>
        <span class="top-bar-title">Panel de Usuario</span>
        <div class="top-bar-user-info">
            <span>Hola, <%= usuarioActual.getNombre() %></span>
            <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-sm btn-outline-secondary ms-2">Salir</a>
        </div>
    </div>

    <aside class="sidebar" id="sidebar"> <%-- Sidebar lateral, oculto por defecto --%>
        <div class="sidebar-header">
            <h3>Menú</h3>
        </div>
        <ul class="sidebar-nav">
            <li><a href="<%= request.getContextPath() %>/MascotaServlet"><i class="fas fa-home"></i> Mis Mascotas</a></li>
            <li><a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario"><i class="fas fa-plus-circle"></i> Añadir Mascota</a></li>
            <li><a href="<%= request.getContextPath() %>/CalculadoraComidaServlet"><i class="fas fa-calculator"></i> Calculadora de Comida</a></li>
            <%-- NUEVO: Enlace a Visitas Veterinarias --%>
            <li><a href="#" onclick="alert('Selecciona una mascota para ver sus visitas desde su tarjeta, o añade una visita desde el formulario de mascota.'); return false;"><i class="fas fa-stethoscope"></i> Visitas Veterinarias</a></li>
            <li><a href="<%= request.getContextPath() %>/LogoutServlet"><i class="fas fa-sign-out-alt"></i> Cerrar Sesión</a></li>
        </ul>
    </aside>

    <div class="backdrop" id="sidebarBackdrop"></div> <%-- Telón de fondo para el sidebar --%>

    <main class="main-content panel-container"> <%-- Contenido principal, siempre centrado --%>
        <div class="welcome-card">
            <span class="dog-illustration">🐶</span>
            ¡Bienvenida, <%= usuarioActual.getNombre() %>!
        </div>

        <h2 class="text-center mb-4">Mis Mascotas</h2>

        <%-- Botones para móvil (ocultos en escritorio) --%>
        <div class="d-grid gap-3 mb-4 d-md-none">
            <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario" class="btn btn-primary btn-lg">
                <i class="fas fa-plus-circle me-2"></i> Añadir Nueva Mascota
            </a>
            <a href="<%= request.getContextPath() %>/CalculadoraComidaServlet" class="btn btn-info btn-lg">
                <i class="fas fa-calculator me-2"></i> Ir a Calculadora de Comida
            </a>
            <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-secondary btn-lg">
                <i class="fas fa-sign-out-alt me-2"></i> Cerrar Sesión
            </a>
        </div>

        <% if (mascotas.isEmpty()) { %>
            <div class="no-mascotas-message">
                <p>Aún no tienes mascotas registradas. ¡Añade la primera!</p>
                <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario" class="btn btn-primary mt-3">
                    <i class="fas fa-plus-circle me-2"></i> Añadir Mascota
                </a>
            </div>
        <% } else { %>
            <div class="d-flex flex-wrap justify-content-center gap-4">
                <% for (Mascota mascota : mascotas) { %>
                    <div class="mascota-card-panel">
                        <img src="<%= mascota.getImagen() != null && !mascota.getImagen().isEmpty() ? request.getContextPath() + "/uploads/" + mascota.getImagen() : "https://placehold.co/120x120/FFC107/333?text=Sin+Imagen" %>"
                             class="mascota-img" alt="Imagen de <%= mascota.getNombre() %>"
                             onerror="this.onerror=null;this.src='https://placehold.co/120x120/FFC107/333?text=Sin+Imagen';">
                        <h5 class="card-title"><%= mascota.getNombre() %></h5>
                        <p class="card-text-info">Raza: <%= mascota.getRaza() %></p>
                        <p class="card-text-info">Edad: <%= calculateAge(mascota.getFechaNacimiento()) %> años</p>
                        <p class="card-text-info">Peso: <%= String.format(java.util.Locale.US, "%.1f", mascota.getPeso()) %> kg</p>
                        <div class="d-flex flex-column w-100 mt-auto">
                            <a href="<%= request.getContextPath() %>/CalculadoraComidaServlet?idMascota=<%= mascota.getIdMascota() %>" class="btn btn-info mb-2">
                                <i class="fas fa-calculator me-2"></i> Calcular Comida
                            </a>
                            <%-- NUEVO: Botón para ver visitas --%>
                            <a href="<%= request.getContextPath() %>/VisitaVeterinarioServlet?action=listar&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-secondary mb-2">
                                <i class="fas fa-stethoscope me-2"></i> Ver Visitas
                            </a>
                            <a href="<%= request.getContextPath() %>/MascotaServlet?action=editar&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-edit-mascota mb-2">
                                <i class="fas fa-edit me-2"></i> Editar
                            </a>
                            <%-- Botón de eliminar que abre el modal de confirmación --%>
                            <button type="button" class="btn btn-danger" data-bs-toggle="modal" data-bs-target="#confirmDeleteModal"
                                    data-mascota-id="<%= mascota.getIdMascota() %>" data-mascota-nombre="<%= mascota.getNombre() %>">
                                <i class="fas fa-trash-alt me-2"></i> Eliminar
                            </button>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>
    </main>

    <!-- Modal de Confirmación de Eliminación -->
    <div class="modal fade" id="confirmDeleteModal" tabindex="-1" aria-labelledby="confirmDeleteModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="confirmDeleteModalLabel">Confirmar Eliminación</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    ¿Estás seguro de que quieres eliminar a <strong id="mascotaNombreEliminar"></strong>? Esta acción no se puede deshacer.
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
                    <%-- Formulario oculto para enviar la petición POST de eliminación --%>
                    <form id="deleteMascotaForm" action="<%= request.getContextPath() %>/MascotaServlet" method="post" style="display:inline;">
                        <input type="hidden" name="action" value="eliminar">
                        <input type="hidden" name="idMascota" id="deleteMascotaId">
                        <button type="submit" class="btn btn-danger">Eliminar</button>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const sidebar = document.getElementById('sidebar');
            const sidebarToggle = document.getElementById('sidebarToggle');
            const sidebarBackdrop = document.getElementById('sidebarBackdrop');
            const confirmDeleteModal = document.getElementById('confirmDeleteModal');

            // Función para alternar la visibilidad del sidebar
            function toggleSidebar() {
                sidebar.classList.toggle('active');
                sidebarBackdrop.classList.toggle('active');
                // Bloquear scroll del body cuando el sidebar está activo
                document.body.classList.toggle('no-scroll', sidebar.classList.contains('active'));
            }

            // Event listener para el botón de toggle
            if (sidebarToggle) {
                sidebarToggle.addEventListener('click', toggleSidebar);
            }

            // Event listener para cerrar el sidebar al hacer clic en el telón de fondo
            if (sidebarBackdrop) {
                sidebarBackdrop.addEventListener('click', toggleSidebar);
            }

            // Lógica para el modal de confirmación de eliminación
            if (confirmDeleteModal) {
                confirmDeleteModal.addEventListener('show.bs.modal', function (event) {
                    // Botón que disparó el modal
                    const button = event.relatedTarget;
                    // Extraer información de los atributos data-*
                    const mascotaId = button.getAttribute('data-mascota-id');
                    const mascotaNombre = button.getAttribute('data-mascota-nombre');

                    // Actualizar el contenido del modal
                    const modalBodyMascotaNombre = confirmDeleteModal.querySelector('#mascotaNombreEliminar');
                    const deleteMascotaIdInput = confirmDeleteModal.querySelector('#deleteMascotaId');

                    modalBodyMascotaNombre.textContent = mascotaNombre;
                    deleteMascotaIdInput.value = mascotaId; // Establecer el ID en el input oculto del formulario
                });
            }
        });
    </script>
</body>
</html>
