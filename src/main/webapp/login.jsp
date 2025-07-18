<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - Calculadora Perros</title>
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

    <div class="login-container">
        <h2 class="text-center mb-4">🐾 Iniciar Sesión</h2>

        <%-- Mensaje de error o éxito (lee de sesión y lo limpia) --%>
        <%
            String message = (String) session.getAttribute("message");
            String messageType = (String) session.getAttribute("messageType");
            if (message != null && !message.isEmpty()) {
        %>
            <div class="alert alert-<%= messageType %>" role="alert">
                <%= message %>
            </div>
        <%
                session.removeAttribute("message"); // Limpiar de la sesión una vez leído
                session.removeAttribute("messageType"); // Limpiar de la sesión una vez leído
            }

            String error = (String) request.getAttribute("error"); // Mensajes de error específicos del request (si los hay)
            if (error != null && !error.isEmpty()) {
        %>
            <div class="alert alert-danger" role="alert">
                <%= error %>
            </div>
        <%
            }
        %>

        <form action="UsuarioServlet" method="post" id="loginForm"> <%-- CAMBIO CLAVE: Apunta a UsuarioServlet --%>
            <input type="hidden" name="action" value="login"> <%-- Campo oculto para la acción --%>
            <div class="mb-3">
                <label for="email" class="form-label">Correo Electrónico</label>
                <input type="email" class="form-control" id="email" name="email" required>
            </div>
            <div class="mb-3 password-input-group">
                <label for="password" class="form-label">Contraseña</label>
                <input type="password" class="form-control" id="password" name="password" required autocomplete="new-password"> <%-- Añadido autocomplete="new-password" para intentar evitar autocompletado --%>
                <button type="button" id="togglePassword" class="password-toggle" title="Mostrar/Ocultar Contraseña">👁️</button>
            </div>
            <button type="submit" class="btn btn-primary w-100">Entrar</button>
        </form>
        <div class="text-center mt-3">
            <p>¿No tienes una cuenta? <a href="registro.jsp">Regístrate aquí</a></p>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const passwordField = document.getElementById('password');
            const togglePasswordButton = document.getElementById('togglePassword');

            // 1. Borrar la contraseña al cargar la página
            // Esto ayuda a evitar que el navegador autocompleta o guarda la contraseña
            if (passwordField) {
                // Usar un pequeño setTimeout para permitir que el navegador autocomplete, luego borrar
                setTimeout(() => {
                    passwordField.value = '';
                }, 100); // Borrar después de 100ms
            }

            // 2. Funcionalidad de mostrar/ocultar contraseña
            if (togglePasswordButton && passwordField) {
                togglePasswordButton.addEventListener('click', function() {
                    // Cambiar el tipo de input entre 'password' y 'text'
                    const type = passwordField.getAttribute('type') === 'password' ? 'text' : 'password';
                    passwordField.setAttribute('type', type);

                    // Cambiar el icono del botón
                    this.textContent = type === 'password' ? '�️' : '🔒'; // Ojos abierto/cerrado o candado
                    this.setAttribute('title', type === 'password' ? 'Mostrar Contraseña' : 'Ocultar Contraseña');
                });
            }
        });
    </script>
</body>
</html>
�