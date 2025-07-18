<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Calculadora de Ración de Comida - CalculadoraPerros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
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
                <a class="navbar-brand" href="<%= request.getContextPath() %>/index.jsp">Calculadoras</a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav ms-auto">
                        <c:set var="usuarioActual" value="${sessionScope.usuario}" />
                        <c:if test="${usuarioActual != null}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/MascotaServlet">Panel de <c:out value="${usuarioActual.nombre}" /></a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/LogoutServlet">Cerrar Sesión</a>
                            </li>
                        </c:if>
                        <c:if test="${usuarioActual == null}">
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/login.jsp">Iniciar Sesión</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" href="${pageContext.request.contextPath}/registro.jsp">Registrarse</a>
                            </li>
                        </c:if>
                    </ul>
                </div>
            </div>
        </nav>
    </header>

    <div class="container">
        <h1>Calculadora Avanzada de Ración de Comida</h1>

        <%-- Mostrar mensajes (éxito/error) --%>
        <c:if test="${not empty message}">
            <div class="message ${messageType}">
                ${message}
            </div>
            <%-- Eliminar los atributos de sesión después de mostrarlos --%>
            <c:remove var="message" scope="session"/>
            <c:remove var="messageType" scope="session"/>
        </c:if>

        <form action="${pageContext.request.contextPath}/CalculadoraComidaServlet" method="post">
            <div class="form-group">
                <label for="idMascota">Selecciona una mascota:</label>
                <select id="idMascota" name="idMascota" class="form-select" required>
                    <option value="">-- Selecciona una mascota --</option>
                    <c:forEach var="mascota" items="${requestScope.mascotas}">
                        <option value="${mascota.idMascota}"
                            ${requestScope.mascotaSeleccionada.idMascota == mascota.idMascota ? 'selected' : ''}>
                            ${mascota.nombre} (Raza: ${mascota.raza}, ${mascota.edadAnos} años ${mascota.edadMeses} meses, ${mascota.pesoKg} kg)
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label for="pesoObjetivoKg">Peso Ideal/Objetivo (kg):</label>
                <input type="number" id="pesoObjetivoKg" name="pesoObjetivoKg" step="0.1" min="0.1" class="form-control" required
                       value="${requestScope.pesoObjetivoKg != null ? requestScope.pesoObjetivoKg : (requestScope.mascotaSeleccionada != null ? requestScope.mascotaSeleccionada.pesoKg : '')}">
                <small class="form-text text-muted">Introduce el peso que debería tener tu perro, o su peso actual si es ideal.</small>
            </div>

            <div class="form-group">
                <label>Nivel de Actividad:</label>
                <div class="radio-group">
                    <div class="form-check form-check-inline">
                        <input type="radio" id="actividadSedentario" name="nivelActividad" value="SEDENTARIO"
                            ${requestScope.nivelActividad eq 'SEDENTARIO' ? 'checked' : ''} class="form-check-input" required>
                        <label class="form-check-label" for="actividadSedentario">Sedentario</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" id="actividadModerado" name="nivelActividad" value="MODERADO"
                            ${requestScope.nivelActividad eq 'MODERADO' ? 'checked' : ''} class="form-check-input">
                        <label class="form-check-label" for="actividadModerado">Moderado</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" id="actividadActivo" name="nivelActividad" value="ACTIVO"
                            ${requestScope.nivelActividad eq 'ACTIVO' ? 'checked' : ''} class="form-check-input">
                        <label class="form-check-label" for="actividadActivo">Activo</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" id="actividadMuyActivo" name="nivelActividad" value="MUY_ACTIVO"
                            ${requestScope.nivelActividad eq 'MUY_ACTIVO' ? 'checked' : ''} class="form-check-input">
                        <label class="form-check-label" for="actividadMuyActivo">Muy Activo</label>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label>Objetivo de Peso:</label>
                <div class="radio-group">
                    <div class="form-check form-check-inline">
                        <input type="radio" id="objetivoMantener" name="objetivoPeso" value="MANTENER"
                            ${requestScope.objetivoPeso eq 'MANTENER' ? 'checked' : ''} class="form-check-input" required>
                        <label class="form-check-label" for="objetivoMantener">Mantener</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" id="objetivoPerder" name="objetivoPeso" value="PERDER"
                            ${requestScope.objetivoPeso eq 'PERDER' ? 'checked' : ''} class="form-check-input">
                        <label class="form-check-label" for="objetivoPerder">Perder</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" id="objetivoGanar" name="objetivoPeso" value="GANAR"
                            ${requestScope.objetivoPeso eq 'GANAR' ? 'checked' : ''} class="form-check-input">
                        <label class="form-check-label" for="objetivoGanar">Ganar</label>
                    </div>
                </div>
            </div>

            <div class="form-group">
                <label>Estado Reproductor/Especial:</label>
                <div class="radio-group">
                    <div class="form-check form-check-inline">
                        <input type="radio" id="estadoNinguno" name="estadoReproductor" value="NINGUNO"
                            ${requestScope.estadoReproductor eq 'NINGUNO' || requestScope.estadoReproductor == null ? 'checked' : ''}
                            onchange="toggleCachorrosField()" class="form-check-input">
                        <label class="form-check-label" for="estadoNinguno">Ninguno</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" id="estadoGestacion" name="estadoReproductor" value="GESTACION"
                            ${requestScope.estadoReproductor eq 'GESTACION' ? 'checked' : ''}
                            onchange="toggleCachorrosField()" class="form-check-input">
                        <label class="form-check-label" for="estadoGestacion">Gestación</label>
                    </div>
                    <div class="form-check form-check-inline">
                        <input type="radio" id="estadoLactancia" name="estadoReproductor" value="LACTANCIA"
                            ${requestScope.estadoReproductor eq 'LACTANCIA' ? 'checked' : ''}
                            onchange="toggleCachorrosField()" class="form-check-input">
                        <label class="form-check-label" for="estadoLactancia">Lactancia</label>
                    </div>
                </div>
            </div>

            <div class="form-group" id="numCachorrosGroup">
                <label for="numCachorros">Número de cachorros (si está en lactancia):</label>
                <input type="number" id="numCachorros" name="numCachorros" min="1" max="15" class="form-control"
                       value="${requestScope.numCachorros != 0 ? requestScope.numCachorros : ''}" placeholder="Ej: 4">
            </div>

            <div class="form-group" id="tieneEnfermedadGroup">
                <div class="form-check">
                    <input type="checkbox" id="tieneEnfermedad" name="tieneEnfermedad" value="true"
                        ${requestScope.tieneEnfermedad ? 'checked' : ''} class="form-check-input">
                    <label class="form-check-label" for="tieneEnfermedad">
                        ¿Tiene alguna enfermedad que afecte significativamente su metabolismo?
                    </label>
                </div>
                <small class="form-text text-muted">(Ej: hipotiroidismo no controlado, ciertas recuperaciones, etc. Consulta a tu veterinario.)</small>
            </div>

            <div class="form-group">
                <label for="tipoAlimento">Tipo de Alimento:</label>
                <select id="tipoAlimento" name="tipoAlimento" class="form-select" required>
                    <option value="">-- Selecciona un tipo de alimento --</option>
                    <c:forEach var="entry" items="${requestScope.tiposAlimento}">
                        <option value="${entry.key}"
                            ${requestScope.tipoAlimento eq entry.key ? 'selected' : ''}>
                            ${entry.key.replace('_', ' ').toLowerCase()} (<c:out value="${String.format('%.0f', entry.value)}" /> kcal/100g)
                        </option>
                    </c:forEach>
                </select>
            </div>

            <button type="submit" class="btn btn-success btn-lg">Calcular Ración</button>
        </form>

        <%-- Sección para mostrar resultados --%>
        <c:if test="${requestScope.resultadosCalculados}">
            <div class="results-section">
                <h2>Resultados del Cálculo para <c:out value="${requestScope.mascotaSeleccionada.nombre}" /></h2>
                <p><strong>Peso Ideal/Objetivo Utilizado:</strong> <c:out value="${requestScope.pesoObjetivoKg}" /> kg</p>
                <p><strong>Nivel de Actividad:</strong> <c:out value="${requestScope.nivelActividad.replace('_', ' ').toLowerCase()}" /></p>
                <p><strong>Objetivo de Peso:</strong> <c:out value="${requestScope.objetivoPeso.replace('_', ' ').toLowerCase()}" /></p>
                <p><strong>Estado Reproductor/Especial:</strong> <c:out value="${requestScope.estadoReproductor.replace('_', ' ').toLowerCase()}" /></p>
                <c:if test="${requestScope.estadoReproductor eq 'LACTANCIA'}">
                    <p><strong>Número de Cachorros:</strong> <c:out value="${requestScope.numCachorros}" /></p>
                </c:if>
                <p><strong>Tiene Enfermedad que afecte metabolismo:</strong> <c:out value="${requestScope.tieneEnfermedad ? 'Sí' : 'No'}" /></p>
                <p><strong>Tipo de Alimento Seleccionado:</strong> <c:out value="${requestScope.tipoAlimento.replace('_', ' ').toLowerCase()}" /></p>
                <hr>
                <p><strong>MER (Metabolismo Energético en Reposo):</strong> <c:out value="${requestScope.mer}" /> kcal/día</p>
                <p><strong>DER (Demanda Energética Diaria Estimada):</strong> <c:out value="${requestScope.der}" /> kcal/día</p>
                <p><strong>Ración Diaria de Comida:</strong> <c:out value="${requestScope.gramosComida}" /> gramos/día</p>
                <p><strong>Número de Comidas Recomendadas al Día:</strong> <c:out value="${requestScope.numComidas}" /></p>

                ${requestScope.recomendaciones}
            </div>
        </c:if>

        <p><a href="${pageContext.request.contextPath}/MascotaServlet" class="back-button">Volver al Panel de Mascotas</a></p>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Función para mostrar/ocultar el campo de número de cachorros
        function toggleCachorrosField() {
            var estadoReproductor = document.querySelector('input[name="estadoReproductor"]:checked').value;
            var numCachorrosGroup = document.getElementById('numCachorrosGroup');
            var numCachorrosInput = document.getElementById('numCachorros');

            if (estadoReproductor === 'LACTANCIA') {
                numCachorrosGroup.style.display = 'block';
                numCachorrosInput.setAttribute('required', 'required'); // Hacerlo requerido si es lactancia
            } else {
                numCachorrosGroup.style.display = 'none';
                numCachorrosInput.removeAttribute('required'); // No requerido si no es lactancia
                numCachorrosInput.value = ''; // Limpiar el valor si se oculta
            }
        }

        // Ejecutar al cargar la página para el estado inicial
        document.addEventListener('DOMContentLoaded', function() {
            toggleCachorrosField(); // Llamar al cargar para establecer el estado inicial
        });
    </script>
</body>
</html>