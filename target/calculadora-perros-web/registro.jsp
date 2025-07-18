<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro de Usuario - Calculadora Perros</title>
    <!-- Incluir Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Google Fonts - Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Enlace a tu archivo de estilos personalizado -->
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <header class="app-header"> <%-- Barra superior --%>
        <div class="text-center">
            <span class="dog-icon">🐾</span>
            <h1>Calculadora de Perros</h1>
            <p>Herramientas útiles para el cuidado de tu mascota.</p>
        </div>
    </header>

    <div class="registration-container"> <%-- Contenedor del formulario de registro --%>
        <h2 class="text-center">🐾 Registrar Nueva Cuenta</h2>

        <%-- Mensaje de error o éxito (si existe) --%>
        <% String message = (String) request.getAttribute("message");
           String messageType = (String) request.getAttribute("messageType"); // "success" o "danger"
           if (message != null && !message.isEmpty()) { %>
            <div class="alert alert-<%= messageType %>" role="alert">
                <%= message %>
            </div>
        <% } %>

        <form action="UsuarioServlet" method="post"> <%-- CAMBIO CLAVE: Apunta a UsuarioServlet --%>
            <input type="hidden" name="action" value="register"> <%-- Campo oculto para la acción --%>
            <div class="mb-3">
                <label for="nombre" class="form-label">Nombre Completo</label>
                <input type="text" class="form-control" id="nombre" name="nombre" required>
            </div>
            <div class="mb-3">
                <label for="email" class="form-label">Correo Electrónico</label>
                <input type="email" class="form-control" id="email" name="email" required>
            </div>
            <div class="mb-3">
                <label for="password" class="form-label">Contraseña</label>
                <input type="password" class="form-control" id="password" name="password" required>
            </div>
            <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-lg">Registrarse</button>
            </div>
        </form>
        <div class="text-center mt-3">
            <p>¿Ya tienes una cuenta? <a href="login.jsp">Inicia sesión aquí</a></p>
        </div>
    </div>

    <!-- Incluir Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
