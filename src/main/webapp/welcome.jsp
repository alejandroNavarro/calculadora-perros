<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>¡Bienvenido! - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <style>
        
    </style>
</head>
<body>
    <header class="app-header">
        <div class="text-center">
            <span class="dog-icon">🐾</span>
            <h1>Calculadora de Perros</h1>
            <p>Herramientas útiles para el cuidado de tu mascota.</p>
        </div>
    </header>

    <div class="welcome-page-container">
        <div class="welcome-card-large">
            <span class="dog-icon-large">🐶</span>
            <%
                Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
                String nombreUsuario = (usuarioActual != null) ? usuarioActual.getNombre() : "Usuario";
            %>
            <h2>¡Hola, <%= nombreUsuario %>!</h2>
            <p>Estamos encantados de tenerte a bordo. Para empezar a usar la calculadora de comida y todas nuestras herramientas, lo primero es añadir a tu fiel compañero.</p>
            <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario" class="btn btn-primary">
                <i class="fas fa-plus-circle me-2"></i> Añadir mi primera mascota
            </a>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
