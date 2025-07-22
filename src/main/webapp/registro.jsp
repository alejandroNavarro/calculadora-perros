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

            <form action="UsuarioServlet" method="post" id="registrationForm">
                <input type="hidden" name="action" value="register"> <%-- Campo oculto para la acción --%>
                <div class="mb-3">
                    <label for="nombre" class="form-label">Nombre Completo</label>
                    <input type="text" class="form-control" id="nombre" name="nombre" required>
                </div>
                <div class="mb-3">
                    <label for="email" class="form-label">Correo Electrónico</label>
                    <input type="email" class="form-control" id="email" name="email" required>
                </div>
                <div class="mb-3 password-input-group"> <%-- Grupo para el campo de contraseña con botón de toggle --%>
                    <label for="password" class="form-label">Contraseña</label>
                    <input type="password" class="form-control" id="password" name="password" required autocomplete="new-password">
                    <button type="button" id="togglePassword" class="password-toggle" title="Mostrar/Ocultar Contraseña">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
                <div class="mb-3 password-input-group"> <%-- Grupo para el campo de confirmar contraseña con botón de toggle --%>
                    <label for="confirmPassword" class="form-label">Confirmar Contraseña</label>
                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required autocomplete="new-password">
                    <button type="button" id="toggleConfirmPassword" class="password-toggle" title="Mostrar/Ocultar Contraseña">
                        <i class="fas fa-eye"></i>
                    </button>
                </div>
                <div class="d-grid gap-2">
                    <button type="submit" class="btn btn-primary btn-lg">Registrarse</button>
                </div>
            </form>

            <div class="divider">O registrarte con</div> <%-- Divisor para opciones de registro social --%>

            <!-- Botón de Google Sign-In -->
            <div id="g_id_onload"
                 data-client_id="595405886937-tf6nba4e60c0dvu6t9n8u5a5cd4nruju.apps.googleusercontent.com" <%-- REEMPLAZA ESTO con tu ID de cliente de Google --%>
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
        document.addEventListener('DOMContentLoaded', function() {
            const passwordField = document.getElementById('password');
            const togglePasswordButton = document.getElementById('togglePassword');
            const confirmPasswordField = document.getElementById('confirmPassword');
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

            setupPasswordToggle(passwordField, togglePasswordButton);
            setupPasswordToggle(confirmPasswordField, toggleConfirmPasswordButton);
        });

        // Función de callback para el registro con Google
        function handleCredentialResponse(response) {
            console.log("ID Token: " + response.credential);

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
            .then(response => response.json()) // Espera una respuesta JSON del servidor
            .then(data => {
                if (data.success) {
                    window.location.href = data.redirectUrl; // Redirige al panel o página de éxito
                } else {
                    // Muestra un mensaje de error. Puedes mejorar esto para mostrarlo en un div de alerta en el JSP
                    alert(data.message || "Error al registrarse con Google.");
                    console.error("Error en el registro de Google (servidor):", data.message);
                }
            })
            .catch((error) => {
                console.error('Error al enviar el token de Google al servidor:', error);
                alert("Hubo un error de comunicación. Inténtalo de nuevo.");
            });
        }
    </script>
</body>
</html>
