<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Registro de Usuario - Calculadora Perros</title>
    <!-- Incluir Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Enlace a tu archivo de estilos personalizado -->
    <link rel="stylesheet" href="css/style.css">
    <!-- Font Awesome para iconos (ej. mostrar/ocultar contraseña) -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <!-- Script de Google Identity Services -->
    <script src="https://accounts.google.com/gsi/client" async defer></script>
</head>
<body>
    <header class="app-header"> <%-- Barra superior --%>
        <div class="text-center">
            <span class="dog-icon">🐾</span>
            <h1>Calculadora de Perros</h1>
            <p>Herramientas útiles para el cuidado de tu mascota.</p>
        </div>
    </header>

    <div class="registration-wrapper"> <%-- Nuevo contenedor para centrar verticalmente --%>
        <div class="registration-container"> <%-- Contenedor del formulario de registro --%>
            <h2 class="text-center mb-4">🐾 Crear una Cuenta</h2>

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

                // Recuperar valores antiguos del formulario en caso de error de validación
                String oldNombre = (String) request.getAttribute("oldNombre");
                String oldEmail = (String) request.getAttribute("oldEmail");
            %>

            <form action="UsuarioServlet" method="post" id="registrationForm">
                <input type="hidden" name="action" value="register"> <%-- Campo oculto para la acción --%>
                <div class="mb-3">
                    <label for="nombre" class="form-label">Nombre Completo</label>
                    <input type="text" class="form-control" id="nombre" name="nombre" value="<%= oldNombre != null ? oldNombre : "" %>" required>
                    <div class="invalid-feedback" id="nombreError"></div>
                </div>
                <div class="mb-3">
                    <label for="email" class="form-label">Correo Electrónico</label>
                    <input type="email" class="form-control" id="email" name="email" value="<%= oldEmail != null ? oldEmail : "" %>" required>
                    <div class="invalid-feedback" id="emailError"></div>
                </div>
                <div class="mb-3 password-input-group"> <%-- Grupo para el campo de contraseña con botón de toggle --%>
                    <label for="password" class="form-label">Contraseña</label>
                    <input type="password" class="form-control" id="password" name="password" required autocomplete="new-password">
                    <button type="button" id="togglePassword" class="password-toggle" title="Mostrar/Ocultar Contraseña">
                        <i class="fas fa-eye"></i>
                    </button>
                    <div class="password-strength-indicator">
                        <div class="password-strength-bar" id="passwordStrengthBar"></div>
                    </div>
                    <div class="password-strength-text" id="passwordStrengthText"></div>
                    <div class="invalid-feedback" id="passwordError"></div>
                </div>
                <div class="mb-3 password-input-group"> <%-- Grupo para el campo de confirmar contraseña con botón de toggle --%>
                    <label for="confirmPassword" class="form-label">Confirmar Contraseña</label>
                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required autocomplete="new-password">
                    <button type="button" id="toggleConfirmPassword" class="password-toggle" title="Mostrar/Ocultar Contraseña">
                        <i class="fas fa-eye"></i>
                    </button>
                    <div class="invalid-feedback" id="confirmPasswordError"></div>
                </div>
                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary btn-lg">Registrarse</button>
                </div>
            </form>

            <div class="divider">O registrarte con</div> <%-- Divisor para opciones de registro social --%>

            <!-- Botón de Google Sign-In -->
            <div id="g_id_onload"
                 data-client_id="595405886937-tf6nba4e60c0dvu6t9n8u5a5cd4nruju.apps.googleusercontent.com" <%-- ¡IMPORTANTE! REEMPLAZA ESTO con tu ID de cliente de Google REAL --%>
                 data-callback="handleCredentialResponse"
                 data-auto_prompt="false"
                 data-ux_mode="popup" <%-- O 'redirect' si prefieres la redirección --%>
                 data-itp_support="true">
            </div>
            <div class="g_id_signin"
                 data-type="standard"
                 data-size="large"
                 data-theme="outline"
                 data-text="signup_with" <%-- Texto específico para registro --%>
                 data-shape="rectangular"
                 data-logo_alignment="left">
            </div>

            <div class="text-center mt-3">
                <p>¿Ya tienes una cuenta? <a href="login.jsp">Inicia sesión aquí</a></p>
            </div>
        </div>
    </div>

    <!-- Incluir Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Función de callback para el registro con Google - DECLARADA GLOBALMENTE
        function handleCredentialResponse(response) {
            console.log("handleCredentialResponse: ID Token recibido (primeros 50 caracteres): " + (response.credential ? response.credential.substring(0, Math.min(response.credential.length, 50)) + "..." : "null"));

            // Envía el token a tu servlet (e.g., UsuarioServlet) para la verificación
            fetch('UsuarioServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    'action': 'googleRegister', // Acción para manejar el registro de Google
                    'id_token': response.credential // El token de identidad de Google
                })
            })
            .then(response => {
                console.log("handleCredentialResponse: Respuesta del servidor recibida. Status: " + response.status);
                return response.json(); // Espera una respuesta JSON del servidor
            })
            .then(data => {
                console.log("handleCredentialResponse: Datos JSON del servidor: ", data);
                if (data.success) {
                    window.location.href = data.redirectUrl; // Redirige al panel o página de éxito
                } else {
                    // Muestra un mensaje de error en un div de alerta de Bootstrap
                    const alertDiv = document.createElement('div');
                    alertDiv.className = 'alert alert-danger alert-dismissible fade show mt-3';
                    alertDiv.setAttribute('role', 'alert');
                    alertDiv.innerHTML = `
                        ${data.message || "Error al registrarse con Google."}
                        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                    `;
                    document.getElementById('registrationForm').prepend(alertDiv); // Añadir la alerta al principio del formulario
                    console.error("Error en el registro de Google (servidor):", data.message);
                }
            })
            .catch((error) => {
                console.error('Error al enviar el token de Google al servidor o procesar la respuesta:', error);
                // Muestra un mensaje de error genérico en un div de alerta de Bootstrap
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger alert-dismissible fade show mt-3';
                alertDiv.setAttribute('role', 'alert');
                alertDiv.innerHTML = `
                    Hubo un error de comunicación. Inténtalo de nuevo.
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                `;
                document.getElementById('registrationForm').prepend(alertDiv);
            });
        }

        document.addEventListener('DOMContentLoaded', function() {
            const registrationForm = document.getElementById('registrationForm');
            const nombreInput = document.getElementById('nombre');
            const emailInput = document.getElementById('email');
            const passwordInput = document.getElementById('password');
            const confirmPasswordInput = document.getElementById('confirmPassword');

            const nombreError = document.getElementById('nombreError');
            const emailError = document.getElementById('emailError');
            const passwordError = document.getElementById('passwordError');
            const confirmPasswordError = document.getElementById('confirmPasswordError');

            const passwordStrengthBar = document.getElementById('passwordStrengthBar');
            const passwordStrengthText = document.getElementById('passwordStrengthText');

            const togglePasswordButton = document.getElementById('togglePassword');
            const toggleConfirmPasswordButton = document.getElementById('toggleConfirmPassword');

            // Funcionalidad de mostrar/ocultar contraseña para ambos campos
            function setupPasswordToggle(field, button) {
                if (button && field) {
                    button.addEventListener('click', function() {
                        const type = field.getAttribute('type') === 'password' ? 'text' : 'password';
                        field.setAttribute('type', type);

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
            }

            setupPasswordToggle(passwordInput, togglePasswordButton);
            setupPasswordToggle(confirmPasswordInput, toggleConfirmPasswordButton);

            // Función para validar la fuerza de la contraseña
            function checkPasswordStrength() {
                const password = passwordInput.value;
                let strength = 0;
                let text = "Muy Débil";
                let color = "red";

                if (password.length > 0) {
                    strength = 20; // Base strength for having any character
                }
                if (password.length >= 8) {
                    strength += 20;
                }
                if (password.match(/[a-z]/) && password.match(/[A-Z]/)) {
                    strength += 20;
                }
                if (password.match(/\d/)) {
                    strength += 20;
                }
                if (password.match(/[^a-zA-Z\d]/)) {
                    strength += 20;
                }

                if (strength >= 80) {
                    text = "Fuerte";
                    color = "green";
                } else if (strength >= 60) {
                    text = "Moderada";
                    color = "orange";
                } else if (strength >= 40) {
                    text = "Débil";
                    color = "darkorange";
                }

                passwordStrengthBar.style.width = strength + '%';
                passwordStrengthBar.style.backgroundColor = color;
                passwordStrengthText.textContent = text;
            }

            passwordInput.addEventListener('input', checkPasswordStrength);

            // Función para validar el formulario antes de enviar
            registrationForm.addEventListener('submit', function(event) {
                let isValid = true;

                // Resetear mensajes de error
                nombreInput.classList.remove('is-invalid');
                nombreError.textContent = '';
                emailInput.classList.remove('is-invalid');
                emailError.textContent = '';
                passwordInput.classList.remove('is-invalid');
                passwordError.textContent = '';
                confirmPasswordInput.classList.remove('is-invalid');
                confirmPasswordError.textContent = '';

                // Validación de Nombre
                if (nombreInput.value.trim() === '') {
                    nombreInput.classList.add('is-invalid');
                    nombreError.textContent = 'El nombre es obligatorio.';
                    isValid = false;
                }

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
                if (passwordInput.value.trim() === '') {
                    passwordInput.classList.add('is-invalid');
                    passwordError.textContent = 'La contraseña es obligatoria.';
                    isValid = false;
                } else if (passwordInput.value.length < 8) {
                    passwordInput.classList.add('is-invalid');
                    passwordError.textContent = 'La contraseña debe tener al menos 8 caracteres.';
                    isValid = false;
                }

                // Validación de Confirmar Contraseña
                if (confirmPasswordInput.value.trim() === '') {
                    confirmPasswordInput.classList.add('is-invalid');
                    confirmPasswordError.textContent = 'Confirma tu contraseña.';
                    isValid = false;
                } else if (passwordInput.value !== confirmPasswordInput.value) {
                    confirmPasswordInput.classList.add('is-invalid');
                    confirmPasswordError.textContent = 'Las contraseñas no coinciden.';
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
