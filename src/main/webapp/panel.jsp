<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Period" %>
<%@ page import="java.time.ZoneId" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel de Usuario - Calculadora Perros</title>
    <!-- Incluir Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Incluir Font Awesome para iconos -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <!-- Enlace a tu archivo CSS externo (style.css) -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <style>
        /* Estilos adicionales para las imágenes de las mascotas */
        .mascota-img {
            width: 120px; /* Tamaño fijo para las miniaturas */
            height: 120px;
            object-fit: cover; /* Asegura que la imagen cubra el área sin distorsionarse */
            border-radius: 50%; /* Para hacerla circular */
            border: 3px solid #6366f1; /* Borde con color de acento */
            margin-bottom: 15px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        }
        .mascota-card-panel {
            display: flex;
            flex-direction: column;
            align-items: center;
            padding: 20px;
            border-radius: 10px;
            background-color: #fff;
            box-shadow: 0 2px 10px rgba(0,0,0,0.08);
            text-align: center;
        }
        .card-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #333;
            margin-bottom: 10px;
        }
        .card-text-info {
            font-size: 0.95rem;
            color: #555;
            margin-bottom: 5px;
        }
        .btn-edit-mascota {
            margin-top: 15px;
            background-color: #6366f1;
            border-color: #6366f1;
        }
        .btn-edit-mascota:hover {
            background-color: #4f46e5;
            border-color: #4f46e5;
        }
        .no-mascotas-message {
            text-align: center;
            padding: 40px;
            background-color: #f8f9fa;
            border-radius: 10px;
            margin-top: 30px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
    </style>
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

            List<Mascota> mascotas = (List<Mascota>) request.getAttribute("listaMascotas");
            
            if (mascotas == null) {
                mascotas = new ArrayList<>();
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
                if (!mascotas.isEmpty()) { 
                    for (Mascota mascota : mascotas) {
                        String edadDisplay = "N/A";
                        if (mascota.getFechaNacimiento() != null) {
                            LocalDate fechaNacimientoLocal = mascota.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                            LocalDate fechaActual = LocalDate.now();
                            Period periodo = Period.between(fechaNacimientoLocal, fechaActual);
                            int anos = periodo.getYears();
                            int meses = periodo.getMonths();
                            edadDisplay = anos + " año" + (anos == 1 ? "" : "s");
                            if (meses > 0) {
                                if (!edadDisplay.isEmpty() && anos > 0) edadDisplay += ", ";
                                edadDisplay += meses + " mes" + (meses == 1 ? "" : "es");
                            }
                            if (anos == 0 && meses == 0) {
                                edadDisplay = periodo.getDays() + " día" + (periodo.getDays() == 1 ? "" : "s");
                            }
                        }
            %>
                        <div class="col-md-6 col-lg-4 mb-4">
                            <div class="mascota-card-panel h-100">
                                <%
                                    // Determinar la URL de la imagen. Si hay una imagen, usarla; si no, usar el placeholder.
                                    String imageUrl = "";
                                    if (mascota.getImagen() != null && !mascota.getImagen().isEmpty()) {
                                        imageUrl = request.getContextPath() + "/uploads/" + mascota.getImagen();
                                    } else {
                                        // Placeholder si no hay imagen
                                        String placeholderText = mascota.getNombre().substring(0, Math.min(mascota.getNombre().length(), 5)).toUpperCase();
                                        imageUrl = "https://placehold.co/120x120/ADD8E6/000000?text=" + placeholderText;
                                    }
                                %>
                                <img src="<%= imageUrl %>" 
                                     alt="Foto de <%= mascota.getNombre() %>" 
                                     class="mascota-img">
                                
                                <h5 class="card-title"><%= mascota.getNombre() %></h5>
                                <p class="card-text-info">Edad: <%= edadDisplay %></p>
                                <p class="card-text-info">Raza: <%= mascota.getRaza() %></p>
                                
                                <a href="<%= request.getContextPath() %>/MascotaServlet?action=editar&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-primary btn-sm btn-edit-mascota">
                                    <i class="fas fa-edit me-2"></i> Editar Mascota
                                </a>
                                <!-- Botón de eliminar con confirmación (opcional, pero buena práctica) -->
                                <form action="<%= request.getContextPath() %>/MascotaServlet" method="post" onsubmit="return confirm('¿Estás seguro de que quieres eliminar a <%= mascota.getNombre() %>?');" class="mt-2">
                                    <input type="hidden" name="action" value="eliminar">
                                    <input type="hidden" name="idMascota" value="<%= mascota.getIdMascota() %>">
                                    <button type="submit" class="btn btn-danger btn-sm">
                                        <i class="fas fa-trash-alt me-2"></i> Eliminar Mascota
                                    </button>
                                </form>
                            </div>
                        </div>
            <%
                    }
                } else {
            %>
                    <div class="col-12">
                        <div class="no-mascotas-message">
                            <p class="lead">Aún no tienes mascotas registradas. ¡Añade una para empezar a gestionar sus necesidades!</p>
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

    <!-- Script de Bootstrap, se mantiene aquí -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
