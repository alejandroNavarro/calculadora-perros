<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Period" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Locale" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Calculadora de Ración de Comida - CalculadoraPerros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <%!
    // Función auxiliar para calcular la edad a partir de la fecha de nacimiento
    public String calculateAgeDisplay(java.util.Date birthDate) {
        if (birthDate == null) {
            return "N/A";
        }
        LocalDate fechaNacimientoLocal = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fechaActual = LocalDate.now();
        Period periodo = Period.between(fechaNacimientoLocal, fechaActual);
        int anos = periodo.getYears();
        int meses = periodo.getMonths();
        int dias = periodo.getDays();

        if (anos > 0) {
            String display = anos + " año" + (anos == 1 ? "" : "s");
            if (meses > 0) {
                display += ", " + meses + " mes" + (meses == 1 ? "" : "es");
            }
            return display;
        } else if (meses > 0) {
            String display = meses + " mes" + (meses == 1 ? "" : "es");
            if (dias > 0) {
                display += ", " + dias + " día" + (dias == 1 ? "" : "s");
            }
            return display;
        } else if (dias > 0) {
            return dias + " día" + (dias == 1 ? "" : "s");
        } else {
            return "Recién nacido";
        }
    }
    %>

    <%
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
        if (usuarioActual == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            
            return;
        }

        // Recuperar datos del request para precargar el formulario después de un POST o GET con idMascota
        List<Mascota> listaMascotas = (List<Mascota>) request.getAttribute("listaMascotas");
        if (listaMascotas == null) {
            listaMascotas = new ArrayList<>(); // Asegurarse de que no sea null
        }
        Mascota mascotaSeleccionada = (Mascota) request.getAttribute("mascotaSeleccionada"); // Esta es la mascota precargada desde el servlet
        
        // Valores para precargar los campos del formulario, priorizando los del request (post)
        Double pesoObjetivoKg = (Double) request.getAttribute("pesoObjetivoKg");
        String nivelActividad = (String) request.getAttribute("nivelActividad");
        String objetivoPeso = (String) request.getAttribute("objetivoPeso");
        String estadoReproductor = (String) request.getAttribute("estadoReproductor");
        Integer numCachorros = (Integer) request.getAttribute("numCachorros");
        Boolean tieneEnfermedad = (Boolean) request.getAttribute("tieneEnfermedad");
        String tipoAlimento = (String) request.getAttribute("tipoAlimento");
        Double kcalPor100gAlimento = (Double) request.getAttribute("kcalPor100gAlimento"); // Valor manual o de precarga

        // Si no hay valores en el request (primer GET), intentar usar los de la mascota seleccionada
        if (mascotaSeleccionada != null) {
            // Si la mascota viene precargada, usamos su peso actual como peso objetivo inicial
            if (pesoObjetivoKg == null) pesoObjetivoKg = mascotaSeleccionada.getPeso();
            if (nivelActividad == null) nivelActividad = mascotaSeleccionada.getNivelActividad();
            if (objetivoPeso == null) objetivoPeso = mascotaSeleccionada.getObjetivoPeso();
            if (estadoReproductor == null) estadoReproductor = mascotaSeleccionada.getEstadoReproductor();
            if (numCachorros == null) numCachorros = mascotaSeleccionada.getNumCachorros();
            // 'tieneEnfermedad' no está en Mascota directamente, así que se mantiene del request o default false
            if (tipoAlimento == null) tipoAlimento = mascotaSeleccionada.getTipoAlimentoPredeterminado();
            if (kcalPor100gAlimento == null) kcalPor100gAlimento = mascotaSeleccionada.getKcalPor100gAlimentoPredeterminado();
        }

        Map<String, Double> tiposAlimentoMap = (Map<String, Double>) request.getAttribute("tiposAlimento");
        if (tiposAlimentoMap == null) {
            // Fallback si por alguna razón no se cargan los tipos de alimento del servlet
            tiposAlimentoMap = new LinkedHashMap<>();
            tiposAlimentoMap.put("PIENSO_SECO_MANTENIMIENTO", 350.0);
            tiposAlimentoMap.put("PIENSO_SECO_ALTA_ENERGIA", 400.0);
            tiposAlimentoMap.put("PIENSO_SECO_LIGHT", 300.0);
            tiposAlimentoMap.put("COMIDA_HUMEDA_LATA", 100.0);
            tiposAlimentoMap.put("DIETA_BARF_CRUDA", 180.0);
            tiposAlimentoMap.put("PIENSO_VETERINARIO_RENAL", 320.0);
            tiposAlimentoMap.put("PIENSO_VETERINARIO_DIABETICO", 340.0);
        }

        Boolean resultadosCalculados = (Boolean) request.getAttribute("resultadosCalculados");
        Double mer = (Double) request.getAttribute("mer");
        Double der = (Double) request.getAttribute("der");
        Double gramosComida = (Double) request.getAttribute("gramosComida");
        Integer numComidas = (Integer) request.getAttribute("numComidas");
        String recomendaciones = (String) request.getAttribute("recomendaciones");

        // Mensajes de sesión (usados para redirecciones)
        String mensajeError = (String) session.getAttribute("mensajeError");
        String mensajeExito = (String) session.getAttribute("mensajeExito");
        session.removeAttribute("mensajeError"); // Limpiar después de mostrar
        session.removeAttribute("mensajeExito"); // Limpiar después de mostrar
    %>

    <div class="top-bar"> <%-- Barra superior --%>
        <button class="sidebar-toggle" id="sidebarToggle">
            <i class="fas fa-bars"></i>
        </button>
        <span class="top-bar-title">Calculadora de Ración de Comida</span>
        <div class="top-bar-user-info">
            <span>Hola, <%= usuarioActual.getNombre() %></span>
            <a href="<%= request.getContextPath() %>/LogoutServlet" class="btn btn-sm btn-outline-secondary ms-2">Salir</a>
        </div>
    </div>

    <aside class="sidebar" id="sidebar"> <%-- Sidebar lateral --%>
        <div class="sidebar-header">
            <h3>Menú</h3>
        </div>
        <ul class="sidebar-nav">
            <li><a href="<%= request.getContextPath() %>/MascotaServlet"><i class="fas fa-home"></i> Mis Mascotas</a></li>
            <li><a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario"><i class="fas fa-plus-circle"></i> Añadir Mascota</a></li>
            <li><a href="<%= request.getContextPath() %>/CalculadoraComidaServlet"><i class="fas fa-calculator"></i> Calculadora de Comida</a></li>
            <li><a href="<%= request.getContextPath() %>/LogoutServlet"><i class="fas fa-sign-out-alt"></i> Cerrar Sesión</a></li>
        </ul>
    </aside>

    <div class="backdrop" id="sidebarBackdrop"></div> <%-- Telón de fondo para el sidebar --%>

    <main class="main-content panel-container"> <%-- Contenido principal con el estilo de panel --%>
        <h1 class="text-center mb-4">Calculadora Avanzada de Ración de Comida</h1>

        <%-- Mostrar mensajes (éxito/error) --%>
        <% if (mensajeError != null) { %>
            <div class="alert alert-danger" role="alert">
                <%= mensajeError %>
            </div>
        <% } %>
        <% if (mensajeExito != null) { %>
            <div class="alert alert-success" role="alert">
                <%= mensajeExito %>
            </div>
        <% } %>

        <div class="calculator-section form-card"> <%-- Usamos form-card para el estilo de tarjeta --%>
            <form action="<%= request.getContextPath() %>/CalculadoraComidaServlet" method="post">
                <div class="mb-3">
                    <label for="idMascota" class="form-label">Selecciona una mascota:</label>
                    <select id="idMascota" name="idMascota" class="form-select" required>
                        <option value="">-- Selecciona una mascota --</option>
                        <%
                            for (Mascota mascota : listaMascotas) {
                                String edadDisplay = calculateAgeDisplay(mascota.getFechaNacimiento());
                                long fechaNacimientoMillis = (mascota.getFechaNacimiento() != null) ? mascota.getFechaNacimiento().getTime() : 0;
                        %>
                                <option value="<%= mascota.getIdMascota() %>"
                                        data-peso="<%= String.format(Locale.US, "%.1f", mascota.getPeso()) %>"
                                        data-nivel-actividad="<%= mascota.getNivelActividad() != null ? mascota.getNivelActividad() : "" %>"
                                        data-objetivo-peso="<%= mascota.getObjetivoPeso() != null ? mascota.getObjetivoPeso() : "" %>"
                                        data-estado-reproductor="<%= mascota.getEstadoReproductor() != null ? mascota.getEstadoReproductor() : "" %>"
                                        data-num-cachorros="<%= mascota.getNumCachorros() != null ? mascota.getNumCachorros() : "" %>"
                                        data-tipo-alimento-predeterminado="<%= mascota.getTipoAlimentoPredeterminado() != null ? mascota.getTipoAlimentoPredeterminado() : "" %>"
                                        data-kcal-alimento-predeterminado="<%= mascota.getKcalPor100gAlimentoPredeterminado() != null ? String.format(Locale.US, "%.1f", mascota.getKcalPor100gAlimentoPredeterminado()) : "0.0" %>"
                                        data-fecha-nacimiento-millis="<%= fechaNacimientoMillis %>"
                                        <%= (mascotaSeleccionada != null && mascotaSeleccionada.getIdMascota() == mascota.getIdMascota()) ? "selected" : "" %>>
                                    <%= mascota.getNombre() %> (Raza: <%= mascota.getRaza() %>, <%= edadDisplay %>, <%= String.format(Locale.US, "%.1f", mascota.getPeso()) %> kg)
                                </option>
                        <%
                            }
                        %>
                    </select>
                    <% if (listaMascotas.isEmpty()) { %>
                        <p class="text-muted mt-2">No tienes mascotas registradas. Puedes <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario">añadir una aquí</a>.</p>
                    <% } %>
                </div>

                <%-- Campos de la mascota seleccionada (solo lectura) --%>
                <div class="mb-3">
                    <label for="nombreMascotaDisplay" class="form-label">Nombre de la Mascota:</label>
                    <input type="text" class="form-control" id="nombreMascotaDisplay" name="nombreMascotaDisplay"
                           value="<%= mascotaSeleccionada != null ? mascotaSeleccionada.getNombre() : "" %>" readonly>
                </div>
                <div class="mb-3">
                    <label for="pesoActualDisplay" class="form-label">Peso Actual (kg):</label>
                    <input type="text" class="form-control" id="pesoActualDisplay" name="pesoActualDisplay"
                           value="<%= mascotaSeleccionada != null ? String.format(Locale.US, "%.1f", mascotaSeleccionada.getPeso()) : "" %>" readonly>
                </div>
                <div class="mb-3">
                    <label for="edadDisplay" class="form-label">Edad:</label>
                    <input type="text" class="form-control" id="edadDisplay" name="edadDisplay"
                           value="<%= mascotaSeleccionada != null ? calculateAgeDisplay(mascotaSeleccionada.getFechaNacimiento()) : "" %>" readonly>
                </div>


                <div class="mb-3">
                    <label for="pesoObjetivoKg" class="form-label">Peso Ideal/Objetivo (kg):</label>
                    <input type="number" class="form-control" id="pesoObjetivoKg" name="pesoObjetivoKg" step="0.1" min="0.1" required
                           value="<%= (pesoObjetivoKg != null) ? String.format(Locale.US, "%.1f", pesoObjetivoKg) : "" %>">
                    <small class="form-text text-muted">Introduce el peso que debería tener tu perro, o su peso actual si es ideal.</small>
                </div>

                <div class="mb-3">
                    <label class="form-label">Nivel de Actividad:</label>
                    <div class="radio-group">
                        <div class="form-check form-check-inline">
                            <input type="radio" id="actividadSedentario" name="nivelActividad" value="SEDENTARIO"
                                <%= ("SEDENTARIO".equals(nivelActividad)) ? "checked" : "" %> class="form-check-input" required>
                            <label class="form-check-label" for="actividadSedentario">Sedentario</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input type="radio" id="actividadModerado" name="nivelActividad" value="MODERADO"
                                <%= ("MODERADO".equals(nivelActividad)) ? "checked" : "" %> class="form-check-input">
                            <label class="form-check-label" for="actividadModerado">Moderado</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input type="radio" id="actividadActivo" name="nivelActividad" value="ACTIVO"
                                <%= ("ACTIVO".equals(nivelActividad)) ? "checked" : "" %> class="form-check-input">
                            <label class="form-check-label" for="actividadActivo">Activo</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input type="radio" id="actividadMuyActivo" name="nivelActividad" value="MUY_ACTIVO"
                                <%= ("MUY_ACTIVO".equals(nivelActividad)) ? "checked" : "" %> class="form-check-input">
                            <label class="form-check-label" for="actividadMuyActivo">Muy Activo</label>
                        </div>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Objetivo de Peso:</label>
                    <div class="radio-group">
                        <div class="form-check form-check-inline">
                            <input type="radio" id="objetivoMantener" name="objetivoPeso" value="MANTENER"
                                <%= ("MANTENER".equals(objetivoPeso) || objetivoPeso == null) ? "checked" : "" %> class="form-check-input" required>
                            <label class="form-check-label" for="objetivoMantener">Mantener</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input type="radio" id="objetivoPerder" name="objetivoPeso" value="PERDER"
                                <%= ("PERDER".equals(objetivoPeso)) ? "checked" : "" %> class="form-check-input">
                            <label class="form-check-label" for="objetivoPerder">Perder</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input type="radio" id="objetivoGanar" name="objetivoPeso" value="GANAR"
                                <%= ("GANAR".equals(objetivoPeso)) ? "checked" : "" %> class="form-check-input">
                            <label class="form-check-label" for="objetivoGanar">Ganar</label>
                        </div>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Estado Reproductor/Especial:</label>
                    <div class="radio-group">
                        <div class="form-check form-check-inline">
                            <input type="radio" id="estadoNinguno" name="estadoReproductor" value="NINGUNO"
                                <%= ("NINGUNO".equals(estadoReproductor) || estadoReproductor == null) ? "checked" : "" %>
                                onchange="toggleCachorrosField()" class="form-check-input">
                            <label class="form-check-label" for="estadoNinguno">Ninguno</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input type="radio" id="estadoGestacion" name="estadoReproductor" value="GESTACION"
                                <%= ("GESTACION".equals(estadoReproductor)) ? "checked" : "" %>
                                onchange="toggleCachorrosField()" class="form-check-input">
                            <label class="form-check-label" for="estadoGestacion">Gestación</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input type="radio" id="estadoLactancia" name="estadoReproductor" value="LACTANCIA"
                                <%= ("LACTANCIA".equals(estadoReproductor)) ? "checked" : "" %>
                                onchange="toggleCachorrosField()" class="form-check-input">
                            <label class="form-check-label" for="estadoLactancia">Lactancia</label>
                        </div>
                    </div>
                </div>

                <div class="mb-3" id="numCachorrosGroup">
                    <label for="numCachorros" class="form-label">Número de cachorros (si está en lactancia):</label>
                    <input type="number" id="numCachorros" name="numCachorros" min="1" max="15" class="form-control"
                           value="<%= (numCachorros != null && numCachorros != 0) ? numCachorros : "" %>" placeholder="Ej: 4">
                </div>

                <div class="mb-3" id="tieneEnfermedadGroup">
                    <div class="form-check">
                        <input type="checkbox" id="tieneEnfermedad" name="tieneEnfermedad" value="true"
                            <%= (tieneEnfermedad != null && tieneEnfermedad) ? "checked" : "" %> class="form-check-input">
                        <label class="form-check-label" for="tieneEnfermedad">
                            ¿Tiene alguna enfermedad que afecte significativamente su metabolismo?
                        </label>
                    </div>
                    <small class="form-text text-muted">(Ej: hipotiroidismo no controlado, ciertas recuperaciones, etc. Consulta a tu veterinario.)</small>
                </div>

                <div class="mb-3">
                    <label for="tipoAlimento" class="form-label">Tipo de Alimento:</label>
                    <select id="tipoAlimento" name="tipoAlimento" class="form-select" required onchange="updateKcalInput()">
                        <option value="">-- Selecciona un tipo de alimento --</option>
                        <%
                            for (Map.Entry<String, Double> entry : tiposAlimentoMap.entrySet()) {
                                String key = entry.getKey();
                                Double value = entry.getValue();
                        %>
                                <option value="<%= key %>"
                                    <%= (key.equals(tipoAlimento)) ? "selected" : "" %>>
                                    <%= key.replace('_', ' ').toLowerCase() %> (<%= String.format(Locale.US, "%.0f", value) %> kcal/100g)
                                </option>
                        <%
                            }
                        %>
                    </select>
                </div>

                <div class="mb-3">
                    <label for="kcalPor100gAlimento" class="form-label">Kcal por 100g de Alimento (Opcional, sobrescribe el valor predeterminado):</label>
                    <input type="number" class="form-control" id="kcalPor100gAlimento" name="kcalPor100gAlimento" step="0.1" min="0.1"
                           value="<%= (kcalPor100gAlimento != null) ? String.format(Locale.US, "%.1f", kcalPor100gAlimento) : "" %>"
                           placeholder="Ej: 350.0">
                    <small class="form-text text-muted">Introduce las kilocalorías por 100 gramos si conoces el valor exacto de tu alimento.</small>
                </div>

                <button type="submit" class="btn btn-success btn-lg">Calcular Ración</button>
            </form>
        </div>

        <%-- Sección para mostrar resultados --%>
        <% if (resultadosCalculados != null && resultadosCalculados) { %>
            <div class="results-section mt-5 form-card"> <%-- También usamos form-card aquí --%>
                <h2 class="text-center mb-4">Resultados del Cálculo para <%= mascotaSeleccionada.getNombre() %></h2>
                <p><strong>Peso Ideal/Objetivo Utilizado:</strong> <%= String.format(Locale.US, "%.1f", pesoObjetivoKg) %> kg</p>
                <p><strong>Nivel de Actividad:</strong> <%= nivelActividad.replace('_', ' ').toLowerCase() %></p>
                <p><strong>Objetivo de Peso:</strong> <%= objetivoPeso.replace('_', ' ').toLowerCase() %></p>
                <p><strong>Estado Reproductor/Especial:</strong> <%= estadoReproductor.replace('_', ' ').toLowerCase() %></p>
                <% if ("LACTANCIA".equals(estadoReproductor)) { %>
                    <p><strong>Número de Cachorros:</strong> <%= numCachorros %></p>
                <% } %>
                <p><strong>Tiene Enfermedad que afecte metabolismo:</strong> <%= (tieneEnfermedad != null && tieneEnfermedad) ? "Sí" : "No" %></p>
                <p><strong>Tipo de Alimento Seleccionado:</strong> <%= tipoAlimento.replace('_', ' ').toLowerCase() %></p>
                <hr>
                <p><strong>MER (Metabolismo Energético en Reposo):</strong> <%= String.format(Locale.US, "%.0f", mer) %> kcal/día</p>
                <p><strong>DER (Demanda Energética Diaria Estimada):</strong> <%= String.format(Locale.US, "%.0f", der) %> kcal/día</p>
                <p><strong>Ración Diaria de Comida:</strong> <%= String.format(Locale.US, "%.0f", gramosComida) %> gramos/día</p>
                <p><strong>Número de Comidas Recomendadas al Día:</strong> <%= numComidas %></p>

                <%= recomendaciones != null ? recomendaciones : "" %>
            </div>
        <% } %>

        <p class="mt-4"><a href="<%= request.getContextPath() %>/MascotaServlet" class="btn btn-secondary">Volver al Panel de Mascotas</a></p>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Mapa de tipos de alimento y sus kcal/100g para usar en JavaScript
        const jsTiposAlimento = {
            <% 
                boolean first = true;
                for (Map.Entry<String, Double> entry : tiposAlimentoMap.entrySet()) {
                    if (!first) { out.print(","); }
                    out.print("'" + entry.getKey() + "': ");
                    if (entry.getValue() != null) {
                        out.print(String.format(Locale.US, "%.1f", entry.getValue()));
                    } else {
                        out.print("0.0"); // Default to 0.0 if value is null
                    }
                    first = false;
                }
            %>
        };

        // Función para mostrar/ocultar el campo de número de cachorros
        function toggleCachorrosField() {
            var estadoReproductorRadios = document.querySelectorAll('input[name="estadoReproductor"]');
            let estadoReproductor = '';
            estadoReproductorRadios.forEach(radio => {
                if (radio.checked) {
                    estadoReproductor = radio.value;
                }
            });

            var numCachorrosGroup = document.getElementById('numCachorrosGroup');
            var numCachorrosInput = document.getElementById('numCachorros');

            if (estadoReproductor === 'LACTANCIA') {
                numCachorrosGroup.style.display = 'block';
                numCachorrosInput.setAttribute('required', 'required');
            } else {
                numCachorrosGroup.style.display = 'none';
                numCachorrosInput.removeAttribute('required');
                numCachorrosInput.value = ''; // Limpiar el valor cuando se oculta
            }
        }

        // Función para actualizar el campo de Kcal por 100g cuando cambia el tipo de alimento
        function updateKcalInput() {
            const tipoAlimentoSelect = document.getElementById('tipoAlimento');
            const kcalInput = document.getElementById('kcalPor100gAlimento');
            const selectedType = tipoAlimentoSelect.value;

            if (selectedType && jsTiposAlimento[selectedType] !== undefined) { // Check for undefined
                kcalInput.value = jsTiposAlimento[selectedType];
            } else {
                kcalInput.value = '';
            }
        }

        // Event listener para cuando se selecciona una mascota (o se precarga al inicio)
        document.addEventListener('DOMContentLoaded', function() {
            const selectMascota = document.getElementById('idMascota');
            const nombreMascotaDisplay = document.getElementById('nombreMascotaDisplay');
            const pesoActualDisplay = document.getElementById('pesoActualDisplay');
            const edadDisplay = document.getElementById('edadDisplay');
            const pesoObjetivoKgInput = document.getElementById('pesoObjetivoKg');
            const nivelActividadRadios = document.querySelectorAll('input[name="nivelActividad"]');
            const objetivoPesoRadios = document.querySelectorAll('input[name="objetivoPeso"]');
            const estadoReproductorRadios = document.querySelectorAll('input[name="estadoReproductor"]');
            const tieneEnfermedadCheckbox = document.getElementById('tieneEnfermedad');
            const numCachorrosInput = document.getElementById('numCachorros');
            const tipoAlimentoSelect = document.getElementById('tipoAlimento');
            const kcalPor100gAlimentoInput = document.getElementById('kcalPor100gAlimento');

            // Función para aplicar los datos de la mascota seleccionada (o precargada) al formulario
            function aplicarDatosMascota(selectedOption) {
                if (!selectedOption || selectedOption.value === "") {
                    // Limpiar todos los campos si no hay mascota seleccionada
                    nombreMascotaDisplay.value = '';
                    pesoActualDisplay.value = '';
                    edadDisplay.value = '';
                    pesoObjetivoKgInput.value = '';
                    nivelActividadRadios.forEach(radio => radio.checked = false);
                    objetivoPesoRadios.forEach(radio => radio.checked = false);
                    estadoReproductorRadios.forEach(radio => radio.checked = false);
                    tieneEnfermedadCheckbox.checked = false;
                    numCachorrosInput.value = '';
                    tipoAlimentoSelect.value = '';
                    kcalPor100gAlimentoInput.value = '';
                    toggleCachorrosField();
                    return;
                }

                // Recuperar datos de los data-atributos de la opción seleccionada
                const nombre = selectedOption.textContent.split('(')[0].trim(); // Extraer nombre antes del paréntesis
                const peso = selectedOption.getAttribute('data-peso');
                const edad = selectedOption.textContent.split(',')[1].trim(); // Extraer edad después del peso
                const nivelActividad = selectedOption.getAttribute('data-nivel-actividad');
                const objetivoPesoMascota = selectedOption.getAttribute('data-objetivo-peso');
                const estadoReproductorMascota = selectedOption.getAttribute('data-estado-reproductor');
                const numCachorrosMascota = selectedOption.getAttribute('data-num-cachorros');
                const tipoAlimentoMascota = selectedOption.getAttribute('data-tipo-alimento-predeterminado');
                const kcalAlimentoMascota = selectedOption.getAttribute('data-kcal-alimento-predeterminado');

                // Rellenar campos de solo lectura
                nombreMascotaDisplay.value = nombre;
                pesoActualDisplay.value = peso;
                edadDisplay.value = edad; // Usar el texto de edad ya formateado

                // Rellenar campos editables
                pesoObjetivoKgInput.value = peso; // Por defecto, el peso objetivo es el actual

                // Seleccionar nivel de actividad
                nivelActividadRadios.forEach(radio => {
                    radio.checked = (radio.value === nivelActividad);
                });

                // Seleccionar objetivo de peso
                objetivoPesoRadios.forEach(radio => {
                    radio.checked = (radio.value === objetivoPesoMascota);
                });

                // Seleccionar estado reproductor
                estadoReproductorRadios.forEach(radio => {
                    radio.checked = (radio.value === estadoReproductorMascota);
                });

                // Rellenar número de cachorros
                numCachorrosInput.value = numCachorrosMascota;

                // Seleccionar tipo de alimento
                tipoAlimentoSelect.value = tipoAlimentoMascota;

                // Rellenar kcal por 100g de alimento
                kcalPor100gAlimentoInput.value = kcalAlimentoMascota;

                // Re-evaluar el estado de los campos dependientes
                toggleCachorrosField();
                updateKcalInput(); // Asegurarse de que las kcal se actualicen si el tipo de alimento predeterminado tiene un valor
            }

            // Event listener para cuando se selecciona una mascota del desplegable
            selectMascota.addEventListener('change', function() {
                aplicarDatosMascota(this.options[this.selectedIndex]);
            });

            // Al cargar la página, si ya hay una mascota seleccionada (por el servlet), aplicar sus datos
            const initialSelectedMascotaId = "<%= mascotaSeleccionada != null ? mascotaSeleccionada.getIdMascota() : "" %>";
            if (initialSelectedMascotaId !== "") {
                // Encontrar la opción correspondiente en el select y simular un cambio
                for (let i = 0; i < selectMascota.options.length; i++) {
                    if (selectMascota.options[i].value === initialSelectedMascotaId) {
                        selectMascota.selectedIndex = i;
                        aplicarDatosMascota(selectMascota.options[i]);
                        break;
                    }
                }
            } else {
                // Si no hay mascota precargada, asegúrate de que el campo de cachorros esté oculto inicialmente
                toggleCachorrosField();
            }

            // Sidebar toggle logic
            const sidebar = document.getElementById('sidebar');
            const sidebarToggle = document.getElementById('sidebarToggle');
            const sidebarBackdrop = document.getElementById('sidebarBackdrop');

            function toggleSidebar() {
                sidebar.classList.toggle('active');
                sidebarBackdrop.classList.toggle('active');
                document.body.classList.toggle('no-scroll', sidebar.classList.contains('active'));
            }

            if (sidebarToggle) {
                sidebarToggle.addEventListener('click', toggleSidebar);
            }

            if (sidebarBackdrop) {
                sidebarBackdrop.addEventListener('click', toggleSidebar);
            }
        });
    </script>
</body>
</html>
