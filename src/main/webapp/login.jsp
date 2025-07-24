<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Iniciar Sesión - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css"> <%-- Este es el archivo que contiene los estilos --%>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <header class="app-header"> <%-- Barra superior --%>
        <div class="text-center">
            <span class="dog-icon">🐶</span> <%-- Icono de perro en la cabecera --%>
            <h1>Calculadora de Perros</h1>
            <p>Herramientas útiles para el cuidado de tu mascota.</p>
        </div>
        <%-- La barra de navegación se ha eliminado de aquí para la página de login --%>
    </header>

    <div class="login-wrapper"> <%-- Contenedor para centrar verticalmente --%>
        <div class="login-container">
            <h2 class="text-center mb-4">Iniciar Sesión</h2> <%-- Sin icono en el título --%>

            <%-- Mensaje de error o éxito (lee de request y luego de sesión) --%>
            <%
                String message = (String) request.getAttribute("message"); // Primero del request
                String messageType = (String) request.getAttribute("messageType");

                if (message == null || message.isEmpty()) { // Si no hay mensaje en request, busca en session
                    message = (String) session.getAttribute("message");
                    messageType = (String) session.getAttribute("messageType");
                    // Limpiar de la sesión una vez leído
                    if (message != null && !message.isEmpty()) {
                        session.removeAttribute("message");
                        session.removeAttribute("messageType");
                    }
                }

                if (message != null && !message.isEmpty()) {
            %>
                <div class="alert alert-<%= messageType %> alert-dismissible fade show" role="alert">
                    <%= message %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            <%
                }

                // Recuperar valor antiguo del email en caso de error de validación
                String oldEmail = (String) request.getAttribute("oldEmail");
            %>

            <form action="UsuarioServlet" method="post" id="loginForm">
                <input type="hidden" name="action" value="login">
                <div class="mb-3">
                    <label for="email" class="form-label">Correo Electrónico</label>
                    <input type="email" class="form-control" id="email" name="email" value="<%= oldEmail != null ? oldEmail : "" %>" required>
                    <div class="invalid-feedback" id="emailError"></div>
                </div>
                <div class="mb-3 password-input-group">
                    <label for="password" class="form-label">Contraseña</label>
                    <input type="password" class="form-control" id="password" name="password" required autocomplete="current-password">
                    <button type="button" id="togglePassword" class="password-toggle" title="Mostrar/Ocultar Contraseña">
                        <i class="fas fa-eye"></i>
                    </button>
                    <div class="invalid-feedback" id="passwordError"></div>
                </div>
                <button type="submit" class="btn btn-primary w-100">Entrar</button>
            </form>
            <div class="text-center mt-3">
                <p>¿No tienes una cuenta? <a href="registro.jsp">Regístrate aquí</a></p>
            </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const passwordField = document.getElementById('password');
            const togglePasswordButton = document.getElementById('togglePassword');
            const loginForm = document.getElementById('loginForm');
            const emailInput = document.getElementById('email');
            const emailError = document.getElementById('emailError');
            const passwordError = document.getElementById('passwordError');

            // Intento de borrar la contraseña al cargar la página para evitar autocompletado persistente
            if (passwordField) {
                setTimeout(() => {
                    passwordField.value = '';
                }, 100);
            }

            // Funcionalidad de mostrar/ocultar contraseña
            if (togglePasswordButton && passwordField) {
                togglePasswordButton.addEventListener('click', function() {
                    const type = passwordField.getAttribute('type') === 'password' ? 'text' : 'password';
                    passwordField.setAttribute('type', type);

                    const icon = this.querySelector('i');
                    if (type === 'password') {
                        icon.classList.remove('fa-eye-slash');
                        icon.classList.add('fa-eye');
                        this.setAttribute('title', 'Mostrar Contraseña');
                    } else {
                        icon.classList.remove('fa-eye');
                        icon.classList.add('fa-eye-slash');
                        this.setAttribute('title', 'Ocultar Contraseña');
                    }
                });
            }

            // Validación del formulario antes de enviar
            loginForm.addEventListener('submit', function(event) {
                let isValid = true;

                // Resetear mensajes de error
                emailInput.classList.remove('is-invalid');
                emailError.textContent = '';
                passwordField.classList.remove('is-invalid');
                passwordError.textContent = '';

                // Validación de Email
                if (emailInput.value.trim() === '') {
                    emailInput.classList.add('is-invalid');
                    emailError.textContent = 'El correo electrónico es obligatorio.';
                    isValid = false;
                } else if (!/\S+@\S+\.\S+/.test(emailInput.value)) {
                    emailInput.classList.add('is-invalid');
                    emailError.textContent = 'Introduce un formato de correo electrónico válido.';
                    isValid = false;
                }

                // Validación de Contraseña
                if (passwordField.value.trim() === '') {
                    passwordField.classList.add('is-invalid');
                    passwordError.textContent = 'La contraseña es obligatoria.';
                    isValid = false;
                }

                if (!isValid) {
                    event.preventDefault(); // Detener el envío del formulario si hay errores
                }
            });
        });
    </script>
</body>
</html>
