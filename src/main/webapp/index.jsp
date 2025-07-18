<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %> <%-- Importar la clase Usuario --%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Calculadora de Perros</title>
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
        <nav class="navbar navbar-expand-lg navbar-light bg-light mt-3"> <%-- Barra de navegación --%>
            <div class="container-fluid">
                <a class="navbar-brand" href="<%= request.getContextPath() %>/index.jsp">Inicio</a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav ms-auto">
                        <%
                            Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
                            if (usuarioActual != null) {
                        %>
                                <li class="nav-item">
                                    <a class="nav-link" href="<%= request.getContextPath() %>/MascotaServlet">Panel de <%= usuarioActual.getNombre() %></a>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link btn btn-danger ms-2" href="<%= request.getContextPath() %>/LogoutServlet">Cerrar Sesión</a> <%-- Botón de cerrar sesión --%>
                                </li>
                        <%
                            } else {
                        %>
                                <li class="nav-item">
                                    <a class="nav-link btn btn-outline-primary me-2" href="<%= request.getContextPath() %>/login.jsp">Iniciar Sesión</a> <%-- Botón de iniciar sesión --%>
                                </li>
                                <li class="nav-item">
                                    <a class="nav-link btn btn-primary" href="<%= request.getContextPath() %>/registro.jsp">Registrarse</a> <%-- Botón de registrarse --%>
                                </li>
                        <%
                            }
                        %>
                    </ul>
                </div>
            </div>
        </nav>
    </header>

    <div class="container">
        <!-- Sección de Calculadora de Comida -->
        <div class="calculator-section">
            <h2>Calculadora de Comida</h2>
            <form action="CalculadoraServlet" method="post">
                <input type="hidden" name="actionType" value="comida">
                <label for="pesoComida">Peso del perro (kg):</label>
                <input type="number" id="pesoComida" name="peso" step="0.1" required>

                <label for="edadComida">Edad del perro (años):</label>
                <input type="number" id="edadComida" name="edadAnos" min="0" required>
                
                <label for="actividadComida">Nivel de actividad:</label>
                <select id="actividadComida" name="actividad" required>
                    <option value="">Seleccione...</option>
                    <option value="baja">Baja</option>
                    <option value="media">Media</option>
                    <option value="alta">Alta</option>
                </select>

                <label for="razaComida">Raza (opcional):</label>
                <input type="text" id="razaComida" name="razaComida" placeholder="Ej: Labrador, Bulldog, Mestizo">

                <label for="tipoComida">Tipo de comida:</label>
                <select id="tipoComida" name="tipoComida" required>
                    <option value="">Seleccione...</option>
                    <option value="pienso">Pienso seco</option>
                    <option value="humeda">Comida húmeda</option>
                    <option value="casera">Comida casera</option>
                </select>

                <label>¿Está esterilizado/a?</label>
                <div class="radio-group">
                    <div class="form-check-inline">
                        <input type="radio" id="esterilizadoSi" name="esterilizado" value="si" required>
                        <label for="esterilizadoSi">Sí</label>
                    </div>
                    <div class="form-check-inline">
                        <input type="radio" id="esterilizadoNo" name="esterilizado" value="no">
                        <label for="esterilizadoNo">No</label>
                    </div>
                </div>
                <button type="submit">Calcular Comida</button>
            </form>
            <%-- Mostrar resultado de comida si existe (NUEVO BLOQUE) --%>
            <%
                // Recupera los resultados de la sesión
                Integer calorias = (Integer) session.getAttribute("caloriasNecesarias");
                Integer gramos = (Integer) session.getAttribute("gramosComida");
                String tipoComidaR = (String) session.getAttribute("tipoComidaResult");

                if (calorias != null && gramos != null && tipoComidaR != null) {
            %>
                <div class="alert alert-success mt-3" role="alert">
                    <h4 class="alert-heading">Recomendación de Alimentación</h4>
                    <p>Tu perro necesita aproximadamente **<%= calorias %> kcal/día**.</p>
                    <p>Esto equivale a **<%= gramos %> gramos de <%= tipoComidaR %>**.</p>
                    <hr>
                    <small>Recuerda que estas son estimaciones. Consulta siempre con tu veterinario.</small>
                </div>
            <%
                    // Limpiar los atributos de la sesión una vez mostrados para que no aparezcan en futuras recargas sin calcular
                    session.removeAttribute("caloriasNecesarias");
                    session.removeAttribute("gramosComida");
                    session.removeAttribute("tipoComidaResult");
                    // Asegurarse de limpiar también el atributo antiguo por si acaso
                    session.removeAttribute("comidaResult");
                }
            %>
        </div>

        <!-- Sección de Calculadora de Edad -->
        <div class="calculator-section">
            <h2>Calculadora de Edad</h2>
            <form action="CalculadoraServlet" method="post">
                <input type="hidden" name="actionType" value="edad">
                <label for="edadPerro">Edad del perro (años):</label>
                <input type="number" id="edadPerro" name="edad" required>

                <label for="razaEdad">Tamaño de la raza:</label>
                <select id="razaEdad" name="raza" required>
                    <option value="">Seleccione...</option>
                    <option value="pequeña">Pequeña (hasta 10 kg)</option>
                    <option value="mediana">Mediana (11-25 kg)</option>
                    <option value="grande">Grande (más de 25 kg)</option>
                </select>
                <button type="submit">Calcular Edad</button>
            </form>
            <%-- Mostrar resultado de edad si existe --%>
            <% String edadResult = (String) session.getAttribute("edadResult"); %>
            <% if (edadResult != null && !edadResult.isEmpty()) { %>
                <div class="result">
                    <p><%= edadResult %></p>
                </div>
                <% session.removeAttribute("edadResult"); %> <%-- Limpiar de la sesión --%>
            <% } %>
        </div>
    </div>

    <!-- Incluir Bootstrap JS -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>