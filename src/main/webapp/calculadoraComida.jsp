<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.util.CalculadoraNutricional.ResultadosCalculo" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="java.time.Period" %>
<%@ page import="java.time.ZoneId" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashMap" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Calculadora de Comida - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
    <!-- Google Fonts - Inter -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
</head>
<%
    // Determinar el tema de la mascota y el icono
    String petThemeClass = "dog-theme"; // Tema por defecto
    String petIcon = "🐾"; // Icono por defecto (pata de perro)

    Mascota currentMascotaForTheme = (Mascota) request.getAttribute("mascotaSeleccionada");
    if (currentMascotaForTheme != null) {
        if ("Gato".equals(currentMascotaForTheme.getTipo())) {
            petThemeClass = "cat-theme";
            petIcon = "🐱"; // Cara de gato
        } else {
            petThemeClass = "dog-theme";
            petIcon = "🐾"; // Pata de perro
        }
    }
%>
<body class="<%= petThemeClass %>">
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
        } else {
            return dias + " día" + (dias == 1 ? "" : "s"); // Para recién nacidos o días sueltos
        }
    }
    %>

    <%
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
        if (usuarioActual == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // Recuperar atributos del request (precargados por el Servlet)
        List<Mascota> listaMascotas = (List<Mascota>) request.getAttribute("listaMascotas");
        if (listaMascotas == null) {
            listaMascotas = new ArrayList<>(); // Asegurarse de que no sea null
        }
        Mascota mascotaSeleccionada = (Mascota) request.getAttribute("mascotaSeleccionada");
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

        ResultadosCalculo resultadosCalculo = (ResultadosCalculo) request.getAttribute("resultadosCalculo");
        Boolean resultadosCalculados = (Boolean) request.getAttribute("resultadosCalculados");
        if (resultadosCalculados == null) resultadosCalculados = false; // Por defecto, no hay resultados

        // Recuperar valores del formulario para precargar en caso de error o recálculo
        // Estos atributos son establecidos por el servlet. Pueden ser Double/Integer (POST exitoso) o String (POST con error) o null (GET)
        Object pesoObjetivoKgObj = request.getAttribute("pesoObjetivoKg");
        Double pesoObjetivoKg = null;
        if (pesoObjetivoKgObj instanceof Double) {
            pesoObjetivoKg = (Double) pesoObjetivoKgObj;
        } else if (pesoObjetivoKgObj instanceof String && !((String)pesoObjetivoKgObj).isEmpty()) {
            try { pesoObjetivoKg = Double.parseDouble(((String)pesoObjetivoKgObj).replace(',', '.')); } catch (NumberFormatException e) { /* ignore */ }
        }


        String nivelActividad = (String) request.getAttribute("nivelActividad");
        String objetivoPeso = (String) request.getAttribute("objetivoPeso");
        String estadoReproductor = (String) request.getAttribute("estadoReproductor");
        
        Object numCachorrosObj = request.getAttribute("numCachorros");
        Integer numCachorros = null;
        if (numCachorrosObj instanceof Integer) {
            numCachorros = (Integer) numCachorrosObj;
        } else if (numCachorrosObj instanceof String && !((String)numCachorrosObj).isEmpty()) {
            try { numCachorros = Integer.parseInt((String)numCachorrosObj); } catch (NumberFormatException e) { /* ignore */ }
        }

        String condicionSalud = (String) request.getAttribute("condicionSalud");
        String tipoAlimentoSeleccionado = (String) request.getAttribute("tipoAlimento");
        
        Object kcalPor100gAlimentoObj = request.getAttribute("kcalPor100gAlimento");
        Double kcalPor100gAlimento = null;
        if (kcalPor100gAlimentoObj instanceof Double) {
            kcalPor100gAlimento = (Double) kcalPor100gAlimentoObj;
        } else if (kcalPor100gAlimentoObj instanceof String && !((String)kcalPor100gAlimentoObj).isEmpty()) {
            try { kcalPor100gAlimento = Double.parseDouble(((String)kcalPor100gAlimentoObj).replace(',', '.')); } catch (NumberFormatException e) { /* ignore */ }
        }


        // Mensaje de éxito o error (si existe)
        String message = (String) session.getAttribute("message");
        String messageType = (String) session.getAttribute("messageType");
        if (message != null && !message.isEmpty()) { %>
            <div class="alert alert-<%= messageType %> alert-dismissible fade show fixed-top-alert" role="alert">
                <%= message %>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        <%
            session.removeAttribute("message");
            session.removeAttribute("messageType");
        }
    %>

    <header class="app-header">
        <div class="text-center">
            <span class="pet-icon">
                <%= petIcon %>
            </span>
            <h1>Calculadora de Comida</h1>
            <p>Calcula la ración diaria ideal para tu mascota.</p>
        </div>
    </header>

    <main class="container mt-5">
        <div class="form-card mx-auto">
            <h2 class="text-center mb-4">Parámetros de Cálculo</h2>
            <form action="<%= request.getContextPath() %>/CalculadoraComidaServlet" method="post" id="calculadoraForm" novalidate>
                <div class="mb-3">
                    <label for="idMascota" class="form-label">Seleccionar Mascota:</label>
                    <select class="form-select" id="idMascota" name="idMascota" required onchange="this.form.submit()">
                        <option value="">-- Selecciona una mascota --</option>
                        <% if (listaMascotas != null) {
                            for (Mascota m : listaMascotas) {
                                String edadDisplay = calculateAgeDisplay(m.getFechaNacimiento());
                        %>
                                <option value="<%= m.getIdMascota() %>"
                                    <%= (mascotaSeleccionada != null && m.getIdMascota() == mascotaSeleccionada.getIdMascota()) ? "selected" : "" %>>
                                    <%= m.getNombre() %> (<%= m.getTipo() %>, <%= edadDisplay %>, <%= String.format(Locale.US, "%.1f", m.getPeso()) %> kg)
                                </option>
                            <% }
                        } %>
                    </select>
                    <div class="invalid-feedback">Por favor, selecciona una mascota.</div>
                    <% if (listaMascotas.isEmpty()) { %>
                        <p class="text-muted mt-2">No tienes mascotas registradas. Puedes <a href="<%= request.getContextPath() %>/MascotaServlet?action=mostrarFormulario">añadir una aquí</a>.</p>
                    <% } %>
                </div>

                <% if (mascotaSeleccionada != null) { %>
                    <hr>
                    <h4 class="mb-3">Datos de <%= mascotaSeleccionada.getNombre() %></h4>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <p><strong>Tipo:</strong> <%= mascotaSeleccionada.getTipo() %></p>
                            <p><strong>Raza:</strong> <%= mascotaSeleccionada.getRaza() %></p>
                            <p><strong>Sexo:</strong> <%= mascotaSeleccionada.getSexo() %></p>
                            <p><strong>Fecha Nacimiento:</strong> <%= new SimpleDateFormat("dd/MM/yyyy").format(mascotaSeleccionada.getFechaNacimiento()) %></p>
                            <p><strong>Edad:</strong> <%= calculateAgeDisplay(mascotaSeleccionada.getFechaNacimiento()) %></p>
                        </div>
                        <div class="col-md-6">
                            <p><strong>Esterilizado:</strong> <%= mascotaSeleccionada.isEsterilizado() ? "Sí" : "No" %></p>
                            <p><strong>Peso Actual:</strong> <%= String.format(Locale.US, "%.1f", mascotaSeleccionada.getPeso()) %> kg</p>
                            <p><strong>Nivel Actividad (Pred.):</strong> <%= mascotaSeleccionada.getNivelActividad() %></p>
                            <p><strong>Objetivo Peso (Pred.):</strong> <%= mascotaSeleccionada.getObjetivoPeso() %></p>
                            <p><strong>Estado Reprod. (Pred.):</strong> <%= mascotaSeleccionada.getEstadoReproductor() %></p>
                            <p><strong>Kcal/100g (Pred.):</strong> <%= mascotaSeleccionada.getKcalPor100gAlimentoPredeterminado() != null ? String.format(Locale.US, "%.1f", mascotaSeleccionada.getKcalPor100gAlimentoPredeterminado()) : "No definido" %></p>
                        </div>
                    </div>

                    <hr>
                    <h4 class="mb-3">Ajustar Parámetros para el Cálculo</h4>

                    <div class="mb-3">
                        <label for="pesoObjetivoKg" class="form-label">Peso Actual/Objetivo (kg):</label>
                        <input type="number" class="form-control" id="pesoObjetivoKg" name="pesoObjetivoKg" step="0.1" min="0.1"
                               value="<%= pesoObjetivoKg != null ? String.format(Locale.US, "%.1f", pesoObjetivoKg) : "" %>" required>
                        <div class="invalid-feedback">El peso es obligatorio y debe ser mayor que 0.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Nivel de Actividad:</label>
                        <div class="radio-group">
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="nivelActividad" id="actividadSedentario" value="SEDENTARIO"
                                    <%= "SEDENTARIO".equals(nivelActividad) ? "checked" : "" %> required>
                                <label class="form-check-label" for="actividadSedentario">Sedentario</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="nivelActividad" id="actividadModerado" value="MODERADO"
                                    <%= "MODERADO".equals(nivelActividad) ? "checked" : "" %>>
                                <label class="form-check-label" for="actividadModerado">Moderado</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input class="form-check-input" type="radio" name="nivelActividad" id="actividadActivo" value="ACTIVO"
                                    <%= "ACTIVO".equals(nivelActividad) ? "checked" : "" %>>
                                <label class="form-check-label" for="actividadActivo">Activo</label>
                            </div>
                            <div class="form-check form-check-inline" id="nivelMuyActivoGroup" style="display: <%= (mascotaSeleccionada != null && "Perro".equals(mascotaSeleccionada.getTipo())) ? "inline-block" : "none" %>;">
                                <input class="form-check-input" type="radio" name="nivelActividad" id="actividadMuyActivo" value="MUY_ACTIVO"
                                    <%= "MUY_ACTIVO".equals(nivelActividad) ? "checked" : "" %>>
                                <label class="form-check-label" for="actividadMuyActivo">Muy Activo</label>
                            </div>
                        </div>
                        <div class="invalid-feedback">El nivel de actividad es obligatorio.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Objetivo de Peso:</label>
                        <div class="radio-group">
                            <div class="form-check form-check-inline">
                                <input type="radio" id="objetivoMantener" name="objetivoPeso" value="MANTENER"
                                    <%= "MANTENER".equals(objetivoPeso) ? "checked" : "" %> class="form-check-input" required>
                                <label class="form-check-label" for="objetivoMantener">Mantener</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" id="objetivoPerder" name="objetivoPeso" value="PERDER"
                                    <%= "PERDER".equals(objetivoPeso) ? "checked" : "" %> class="form-check-input">
                                <label class="form-check-label" for="objetivoPerder">Perder</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" id="objetivoGanar" name="objetivoPeso" value="GANAR"
                                    <%= "GANAR".equals(objetivoPeso) ? "checked" : "" %> class="form-check-input">
                                <label class="form-check-label" for="objetivoGanar">Ganar</label>
                            </div>
                        </div>
                        <div class="invalid-feedback">El objetivo de peso es obligatorio.</div>
                    </div>

                    <div class="mb-3">
                        <label class="form-label">Estado Reproductor/Especial:</label>
                        <div class="radio-group">
                            <div class="form-check form-check-inline">
                                <input type="radio" id="estadoNinguno" name="estadoReproductor" value="NINGUNO"
                                    <%= "NINGUNO".equals(estadoReproductor) ? "checked" : "" %>
                                    class="form-check-input" onchange="toggleCachorrosField()" required>
                                <label class="form-check-label" for="estadoNinguno">Ninguno</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" id="estadoGestacion" name="estadoReproductor" value="GESTACION"
                                    <%= "GESTACION".equals(estadoReproductor) ? "checked" : "" %>
                                    class="form-check-input" onchange="toggleCachorrosField()">
                                <label class="form-check-label" for="estadoGestacion">Gestación</label>
                            </div>
                            <div class="form-check form-check-inline">
                                <input type="radio" id="estadoLactancia" name="estadoReproductor" value="LACTANCIA"
                                    <%= "LACTANCIA".equals(estadoReproductor) ? "checked" : "" %>
                                    class="form-check-input" onchange="toggleCachorrosField()">
                                <label class="form-check-label" for="estadoLactancia">Lactancia</label>
                            </div>
                            <div class="form-check form-check-inline" id="estadoCachorroGroup" style="display: <%= (mascotaSeleccionada != null && "Perro".equals(mascotaSeleccionada.getTipo())) ? "inline-block" : "none" %>;">
                                <input type="radio" id="estadoCachorro" name="estadoReproductor" value="CACHORRO"
                                    <%= "CACHORRO".equals(estadoReproductor) ? "checked" : "" %>
                                    class="form-check-input" onchange="toggleCachorrosField()">
                                <label class="form-check-label" for="estadoCachorro">Cachorro</label>
                            </div>
                        </div>
                        <div class="invalid-feedback">El estado reproductor/especial es obligatorio.</div>
                    </div>

                    <div class="mb-3" id="numCachorrosGroup" style="display: <%= "LACTANCIA".equals(estadoReproductor) ? "block" : "none" %>;">
                        <label for="numCachorros" class="form-label">Número de cachorros (si está en lactancia):</label>
                        <input type="number" id="numCachorros" name="numCachorros" min="1" max="15" class="form-control"
                               value="<%= (numCachorros != null && numCachorros != 0) ? numCachorros : "" %>"
                               <%= "LACTANCIA".equals(estadoReproductor) ? "required" : "" %> placeholder="Ej: 4">
                        <div class="invalid-feedback">El número de cachorros es obligatorio y debe ser mayor que 0.</div>
                    </div>

                    <div class="mb-3">
                        <label for="condicionSalud" class="form-label">Condición de Salud (opcional):</label>
                        <input type="text" class="form-control" id="condicionSalud" name="condicionSalud"
                               value="<%= condicionSalud != null ? condicionSalud : "" %>"
                               placeholder="Ej: Normal, Sobrepeso, Senior, Enfermedad renal, Hipotiroidismo">
                        <small class="form-text text-muted">Añade cualquier condición médica o estado relevante. Esto puede influir en el cálculo.</small>
                    </div>

                    <div class="mb-3">
                        <label for="tipoAlimento" class="form-label">Tipo de Alimento:</label>
                        <select id="tipoAlimento" name="tipoAlimento" class="form-select" onchange="updateKcalFromType()" required>
                            <option value="">-- Selecciona un tipo de alimento --</option>
                            <% if (tiposAlimentoMap != null) {
                                for (Map.Entry<String, Double> entry : tiposAlimentoMap.entrySet()) {
                                    String key = entry.getKey();
                                    Double value = entry.getValue();
                            %>
                                    <option value="<%= key %>"
                                        <%= (key.equals(tipoAlimentoSeleccionado)) ? "selected" : "" %>>
                                        <%= key.replace('_', ' ').toLowerCase() %> (<%= String.format(Locale.US, "%.0f", value) %> kcal/100g)
                                    </option>
                            <%
                                }
                            }
                            %>
                        </select>
                        <div class="invalid-feedback">El tipo de alimento es obligatorio.</div>
                    </div>

                    <div class="mb-3">
                        <label for="kcalPor100gAlimento" class="form-label">Kcal por 100g de Alimento (Opcional, sobrescribe el valor del tipo):</label>
                        <input type="number" class="form-control" id="kcalPor100gAlimento" name="kcalPor100gAlimento" step="0.1" min="0.1"
                               value="<%= kcalPor100gAlimento != null ? String.format(Locale.US, "%.1f", kcalPor100gAlimento) : "" %>"
                               placeholder="Ej: 350.0">
                        <small class="form-text text-muted">Introduce las kilocalorías por 100 gramos si conoces el valor exacto de tu alimento. Si lo dejas vacío, se usará el valor predeterminado del tipo de alimento seleccionado.</small>
                        <div class="invalid-feedback">Las Kcal por 100g deben ser un número positivo.</div>
                    </div>

                    <div class="d-grid gap-2 mt-4">
                        <button type="submit" name="action" value="calcular" class="btn btn-primary btn-lg">
                            <i class="fas fa-calculator me-2"></i> Calcular Ración
                        </button>
                        <a href="<%= request.getContextPath() %>/MascotaServlet" class="btn btn-secondary btn-lg">
                            <i class="fas fa-arrow-left me-2"></i> Volver a Mis Mascotas
                        </a>
                    </div>
                <% } else { %>
                    <div class="alert alert-info text-center" role="alert">
                        Por favor, selecciona una mascota de la lista para comenzar a calcular su ración de comida.
                    </div>
                <% } %>
            </form>
        </div>

        <% if (resultadosCalculados && resultadosCalculo != null) { %>
            <div class="result-section mt-5">
                <h2 class="text-center mb-4"><i class="fas fa-chart-bar me-2"></i> Resultados del Cálculo</h2>
                <p><strong>MER (Metabolismo Energético en Reposo):</strong> <%= String.format(Locale.US, "%.2f", resultadosCalculo.getMer()) %> Kcal/día</p>
                <p><strong>DER (Demanda Energética Diaria):</strong> <%= String.format(Locale.US, "%.2f", resultadosCalculo.getDer()) %> Kcal/día</p>
                <p><strong>Ración Diaria Recomendada:</strong> <%= String.format(Locale.US, "%.2f", resultadosCalculo.getGramosComida()) %> gramos</p>
                <p><strong>Kcal/100g de Alimento Usadas:</strong> <%= String.format(Locale.US, "%.1f", resultadosCalculo.getKcalPor100gAlimentoUsado()) %> Kcal</p>
                <p><strong>Número de Comidas Recomendadas:</strong> <%= resultadosCalculo.getNumComidas() %> al día</p>
                <div class="mt-4">
                    <%= resultadosCalculo.getRecomendaciones() %>
                </div>
            </div>
        <% } %>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        // Mapa de tipos de alimento y sus kcal/100g para usar en JavaScript
        const jsTiposAlimento = {
            <%
                boolean first = true;
                if (tiposAlimentoMap != null) {
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
                }
            %>
        };

        document.addEventListener('DOMContentLoaded', function() {
            const sidebarToggle = document.getElementById('sidebarToggle');
            const sidebar = document.getElementById('sidebar');
            const sidebarBackdrop = document.getElementById('sidebarBackdrop');
            const body = document.body;

            // Check if elements exist before adding listeners
            if (sidebarToggle) {
                sidebarToggle.addEventListener('click', function() {
                    if (sidebar) sidebar.classList.toggle('active');
                    if (sidebarBackdrop) sidebarBackdrop.classList.toggle('active');
                    if (body) body.classList.toggle('no-scroll');
                });
            }

            if (sidebarBackdrop) {
                sidebarBackdrop.addEventListener('click', function() {
                    if (sidebar) sidebar.classList.remove('active');
                    if (sidebarBackdrop) sidebarBackdrop.classList.remove('active');
                    if (body) body.classList.remove('no-scroll');
                });
            }

            // Elementos del formulario
            const mascotaSelect = document.getElementById('idMascota');
            const nivelMuyActivoGroup = document.getElementById('nivelMuyActivoGroup');
            const actividadMuyActivoRadio = document.getElementById('actividadMuyActivo');
            // FIX: Corrected typo 'document = document.getElementById' to 'const estadoCachorroGroup = document.getElementById'
            const estadoCachorroGroup = document.getElementById('estadoCachorroGroup'); 
            const estadoCachorroRadio = document.getElementById('estadoCachorro');
            const numCachorrosGroup = document.getElementById('numCachorrosGroup');
            const numCachorrosInput = document.getElementById('numCachorros');
            const tipoAlimentoSelect = document.getElementById('tipoAlimento');
            const kcalPor100gAlimentoInput = document.getElementById('kcalPor100gAlimento');

            // Función para mostrar/ocultar el campo de número de cachorros
            function toggleCachorrosField() {
                const estadoReproductorRadios = document.querySelectorAll('input[name="estadoReproductor"]');
                let estadoReproductor = '';
                estadoReproductorRadios.forEach(radio => {
                    if (radio.checked) {
                        estadoReproductor = radio.value;
                    }
                });

                if (numCachorrosGroup) { // Check if element exists
                    if (estadoReproductor === 'LACTANCIA') {
                        numCachorrosGroup.style.display = 'block';
                        if (numCachorrosInput) numCachorrosInput.setAttribute('required', 'required');
                    } else {
                        numCachorrosGroup.style.display = 'none';
                        if (numCachorrosInput) {
                            numCachorrosInput.removeAttribute('required');
                            numCachorrosInput.value = ''; // Limpiar el valor cuando se oculta
                            numCachorrosInput.classList.remove('is-invalid'); // Limpiar validación
                            const feedbackDiv = numCachorrosInput.nextElementSibling;
                            if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                                feedbackDiv.style.display = 'none';
                            }
                        }
                    }
                }
            }

            // Función para actualizar el campo de Kcal por 100g cuando cambia el tipo de alimento
            function updateKcalFromType() {
                if (!tipoAlimentoSelect || !kcalPor100gAlimentoInput) return; // Check if elements exist

                const selectedType = tipoAlimentoSelect.value;
                const currentKcalValue = kcalPor100gAlimentoInput.value.trim();
                const defaultKcalForCurrentType = jsTiposAlimento[selectedType] !== undefined ? jsTiposAlimento[selectedType].toString() : '';

                if (currentKcalValue === '' || currentKcalValue === defaultKcalForCurrentType) {
                    if (selectedType && jsTiposAlimento[selectedType] !== undefined) {
                        kcalPor100gAlimentoInput.value = jsTiposAlimento[selectedType];
                    } else {
                        kcalPor100gAlimentoInput.value = ''; // Limpiar si no hay valor predeterminado
                    }
                }
            }

            // Función para ajustar la visibilidad de los campos específicos de perro/gato
            function adjustPetSpecificFields() {
                if (!mascotaSelect || mascotaSelect.selectedIndex === -1) return; // Check if element exists and option is selected

                const selectedOption = mascotaSelect.options[mascotaSelect.selectedIndex];
                const petTypeTextMatch = selectedOption.textContent.match(/\(([^,]+),/);
                const petType = petTypeTextMatch ? petTypeTextMatch[1].trim() : "";

                // Update body class for theme
                if (body) {
                    body.classList.remove('dog-theme', 'cat-theme');
                    if (petType === 'Gato') {
                        body.classList.add('cat-theme');
                    } else { // Default to dog-theme for Perro or no selection
                        body.classList.add('dog-theme');
                    }
                }

                // Update pet icon
                const petIconElement = document.querySelector('.pet-icon');
                if (petIconElement) {
                    petIconElement.textContent = (petType === 'Gato') ? '🐱' : '🐾';
                }


                if (nivelMuyActivoGroup && actividadMuyActivoRadio) {
                    if (petType === 'Perro') {
                        nivelMuyActivoGroup.style.display = 'inline-block';
                        actividadMuyActivoRadio.removeAttribute('disabled');
                    } else {
                        nivelMuyActivoGroup.style.display = 'none';
                        actividadMuyActivoRadio.setAttribute('disabled', 'true');
                        if (actividadMuyActivoRadio.checked) {
                            document.getElementById('actividadModerado').checked = true; // Default to Moderate
                        }
                        actividadMuyActivoRadio.classList.remove('is-invalid');
                        const muyActivoFeedback = nivelMuyActivoGroup.querySelector('.invalid-feedback');
                        if (muyActivoFeedback) muyActivoFeedback.style.display = 'none';
                    }
                }

                if (estadoCachorroGroup && estadoCachorroRadio) {
                    if (petType === 'Perro') {
                        estadoCachorroGroup.style.display = 'inline-block';
                        estadoCachorroRadio.removeAttribute('disabled');
                    } else {
                        estadoCachorroGroup.style.display = 'none';
                        estadoCachorroRadio.setAttribute('disabled', 'true');
                        if (estadoCachorroRadio.checked) {
                            document.getElementById('estadoNinguno').checked = true; // Default to None
                        }
                        estadoCachorroRadio.classList.remove('is-invalid');
                        const cachorroFeedback = estadoCachorroGroup.querySelector('.invalid-feedback');
                        if (cachorroFeedback) cachorroFeedback.style.display = 'none';
                    }
                }
                toggleCachorrosField(); // Re-evaluate the puppy field after changing pet type
            }

            // Event listener for mascotaSelect change
            if (mascotaSelect) {
                mascotaSelect.addEventListener('change', adjustPetSpecificFields);
            }

            // Initial calls on page load
            adjustPetSpecificFields();
            toggleCachorrosField();

            // Client-side form validation
            const calculadoraForm = document.getElementById('calculadoraForm');
            if (calculadoraForm) { // Check if form exists
                calculadoraForm.addEventListener('submit', function(event) {
                    let isValid = true;

                    document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
                    document.querySelectorAll('.invalid-feedback').forEach(el => el.style.display = 'none');

                    if (mascotaSelect && mascotaSelect.value === "") {
                        isValid = false;
                        mascotaSelect.classList.add('is-invalid');
                        if (mascotaSelect.nextElementSibling) mascotaSelect.nextElementSibling.style.display = 'block';
                    }

                    if (mascotaSelect && mascotaSelect.value !== "") {
                        const requiredInputs = calculadoraForm.querySelectorAll('input[type="number"][required], input[type="text"][required], select[required]');
                        requiredInputs.forEach(input => {
                            if (input.closest('.mb-3').style.display !== 'none' && !input.disabled) {
                                if (input.value.trim() === '' || (input.type === 'number' && parseFloat(input.value) <= 0)) {
                                    isValid = false;
                                    input.classList.add('is-invalid');
                                    const feedbackDiv = input.nextElementSibling;
                                    if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                                        feedbackDiv.style.display = 'block';
                                    }
                                }
                            }
                        });

                        const radioGroups = ['nivelActividad', 'objetivoPeso', 'estadoReproductor'];
                        radioGroups.forEach(groupName => {
                            const radiosInGroup = document.querySelectorAll(`input[name="${groupName}"]:not([disabled])`);
                            const isAnyRadioChecked = Array.from(radiosInGroup).some(radio => radio.checked);

                            const parentDiv = radiosInGroup.length > 0 ? radiosInGroup[0].closest('.mb-3') : null;
                            const isGroupVisible = parentDiv ? parentDiv.style.display !== 'none' : true;

                            if (isGroupVisible && !isAnyRadioChecked) {
                                isValid = false;
                                radiosInGroup.forEach(radio => radio.classList.add('is-invalid'));
                                const feedbackDiv = radiosInGroup[0].closest('.mb-3').querySelector('.invalid-feedback');
                                if (feedbackDiv) {
                                    feedbackDiv.style.display = 'block';
                                }
                            }
                        });

                        if (numCachorrosGroup && numCachorrosGroup.style.display === 'block' && numCachorrosInput) {
                            if (numCachorrosInput.value.trim() === '' || parseInt(numCachorrosInput.value) <= 0) {
                                isValid = false;
                                numCachorrosInput.classList.add('is-invalid');
                                const feedbackDiv = numCachorrosInput.nextElementSibling;
                                if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                                    feedbackDiv.textContent = 'El número de cachorros es obligatorio y debe ser mayor que 0.';
                                    feedbackDiv.style.display = 'block';
                                }
                            }
                        }

                        if (tipoAlimentoSelect && kcalPor100gAlimentoInput) {
                            if (tipoAlimentoSelect.value === "") {
                                if (kcalPor100gAlimentoInput.value.trim() === "" || parseFloat(kcalPor100gAlimentoInput.value) <= 0) {
                                    isValid = false;
                                    kcalPor100gAlimentoInput.classList.add('is-invalid');
                                    const feedbackDiv = kcalPor100gAlimentoInput.nextElementSibling.nextElementSibling;
                                    if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                                        feedbackDiv.style.display = 'block';
                                    }
                                }
                            } else if (kcalPor100gAlimentoInput.value.trim() !== "" && parseFloat(kcalPor100gAlimentoInput.value) <= 0) {
                                isValid = false;
                                kcalPor100gAlimentoInput.classList.add('is-invalid');
                                const feedbackDiv = kcalPor100gAlimentoInput.nextElementSibling.nextElementSibling;
                                if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                                    feedbackDiv.style.display = 'block';
                                }
                            }
                        }
                    }

                    if (!isValid) {
                        event.preventDefault();
                        const firstInvalid = document.querySelector('.is-invalid');
                        if (firstInvalid) {
                            firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                        }
                    }
                });
            }

            document.querySelectorAll('.form-control, .form-select').forEach(input => {
                input.addEventListener('input', function() {
                    if (this.classList.contains('is-invalid')) {
                        this.classList.remove('is-invalid');
                        const feedbackDiv = this.nextElementSibling;
                        if (this.id === 'kcalPor100gAlimento') {
                            const actualFeedbackDiv = this.nextElementSibling.nextElementSibling;
                            if (actualFeedbackDiv && actualFeedbackDiv.classList.contains('invalid-feedback')) {
                                actualFeedbackDiv.style.display = 'none';
                            }
                        } else if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.style.display = 'none';
                        }
                    }
                });
            });

            document.querySelectorAll('input[type="radio"]').forEach(radio => {
                radio.addEventListener('change', function() {
                    const radioGroupName = this.name;
                    const radiosInGroup = document.querySelectorAll(`input[name="${radioGroupName}"]`);
                    radiosInGroup.forEach(r => r.classList.remove('is-invalid'));
                    const closestMb3 = this.closest('.mb-3');
                    if (closestMb3) {
                        const feedbackDiv = closestMb3.querySelector('.invalid-feedback');
                        if (feedbackDiv) feedbackDiv.style.display = 'none';
                    }
                });
            });
        });
    </script>
</body>
</html>
