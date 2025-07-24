<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Locale" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("isEditMode") != null && (Boolean)request.getAttribute("isEditMode") ? "Editar Mascota" : "Añadir Nueva Mascota" %> - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <!-- Se asume que style.css contiene todos los estilos, incluidos los del formulario -->
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<body>
    <header class="app-header">
        <div class="text-center">
            <span class="dog-icon">🐾</span>
            <h1>Calculadora de Perros y Gatos</h1>
            <p>Herramientas útiles para el cuidado de tu mascota.</p>
        </div>
    </header>

    <main class="container mt-5">
        <%
            Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
            if (usuarioActual == null) {
                response.sendRedirect(request.getContextPath() + "/login.jsp");
                return;
            }

            // Determinar si es un formulario de edición o nuevo basado en el atributo de request
            Boolean isEditModeObj = (Boolean) request.getAttribute("isEditMode");
            boolean isEditMode = (isEditModeObj != null) ? isEditModeObj.booleanValue() : false;

            Mascota mascota = (Mascota) request.getAttribute("mascota"); // Esto será null para nuevos formularios, o poblado en edición/error
            
            String formTitle = isEditMode ? "Editar Mascota" : "Añadir Nueva Mascota";
            String action = isEditMode ? "actualizar" : "insertar";

            // Recuperar valores de la mascota o establecer como vacío si es null
            String nombre = (mascota != null && mascota.getNombre() != null) ? mascota.getNombre() : "";
            String raza = (mascota != null && mascota.getRaza() != null) ? mascota.getRaza() : "";
            String sexo = (mascota != null && mascota.getSexo() != null) ? mascota.getSexo() : "";
            Date fechaNacimiento = (mascota != null) ? mascota.getFechaNacimiento() : null;
            double peso = (mascota != null) ? mascota.getPeso() : 0.0; // Primitive, cannot be null
            boolean esterilizado = (mascota != null) ? mascota.isEsterilizado() : false; // Primitive, cannot be null
            String tipo = (mascota != null && mascota.getTipo() != null) ? mascota.getTipo() : "";
            String nivelActividad = (mascota != null && mascota.getNivelActividad() != null) ? mascota.getNivelActividad() : "";
            String condicionSalud = (mascota != null && mascota.getCondicionSalud() != null) ? mascota.getCondicionSalud() : "";
            String imagen = (mascota != null && mascota.getImagen() != null) ? mascota.getImagen() : "";
            
            String color = (mascota != null && mascota.getColor() != null) ? mascota.getColor() : "";
            String chipID = (mascota != null && mascota.getChipID() != null) ? mascota.getChipID() : "";
            String observaciones = (mascota != null && mascota.getObservaciones() != null) ? mascota.getObservaciones() : "";

            String objetivoPeso = (mascota != null && mascota.getObjetivoPeso() != null) ? mascota.getObjetivoPeso() : "";
            String estadoReproductor = (mascota != null && mascota.getEstadoReproductor() != null) ? mascota.getEstadoReproductor() : "";
            Integer numCachorros = (mascota != null) ? mascota.getNumCachorros() : null; // Can be null
            String tipoAlimentoPredeterminado = (mascota != null && mascota.getTipoAlimentoPredeterminado() != null) ? mascota.getTipoAlimentoPredeterminado() : "";
            Double kcalPor100gAlimentoPredeterminado = (mascota != null) ? mascota.getKcalPor100gAlimentoPredeterminado() : null; // Can be null

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fechaNacimientoStr = (fechaNacimiento != null) ? sdf.format(fechaNacimiento) : "";

            // Mensaje de éxito o error (si existe)
            String message = (String) session.getAttribute("message");
            String messageType = (String) session.getAttribute("messageType");
            if (message != null && !message.isEmpty()) { %>
                <div class="alert alert-<%= messageType %> alert-dismissible fade show" role="alert">
                    <%= message %>
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            <%
                session.removeAttribute("message");
                session.removeAttribute("messageType");
            }

            // Mapa de tipos de alimento y sus kcal/100g para el dropdown
            Map<String, Double> tiposAlimentoMap = new LinkedHashMap<>();
            tiposAlimentoMap.put("PIENSO_SECO_MANTENIMIENTO", 350.0);
            tiposAlimentoMap.put("PIENSO_SECO_ALTA_ENERGIA", 400.0);
            tiposAlimentoMap.put("PIENSO_SECO_LIGHT", 300.0);
            tiposAlimentoMap.put("COMIDA_HUMEDA_LATA", 100.0);
            tiposAlimentoMap.put("DIETA_BARF_CRUDA", 180.0);
            tiposAlimentoMap.put("PIENSO_VETERINARIO_RENAL", 320.0);
            tiposAlimentoMap.put("PIENSO_VETERINARIO_DIABETICO", 340.0);
        %>

        <!-- Tarjeta de bienvenida similar a la imagen -->
        <div class="welcome-card mx-auto" style="max-width: 700px;">
            <span class="dog-illustration">🐶</span> <!-- Puedes reemplazar esto con un SVG real del perro -->
            ¡Bienvenido/a, <%= usuarioActual.getNombre() %>!
        </div>

        <h1 class="text-center mb-4"><%= formTitle %></h1>

        <div class="form-card mx-auto" style="max-width: 700px;">
            <form action="<%= request.getContextPath() %>/MascotaServlet" method="post" enctype="multipart/form-data" id="mascotaForm" novalidate>
                <input type="hidden" name="action" value="<%= action %>">
                <% if (isEditMode && mascota != null) { %> <%-- Solo mostrar idMascota y imagenExistente si realmente está en modo edición y el objeto mascota existe --%>
                    <input type="hidden" name="idMascota" value="<%= mascota.getIdMascota() %>">
                    <input type="hidden" name="imagenExistente" value="<%= mascota.getImagen() != null ? mascota.getImagen() : "" %>">
                <% } %>

                <div class="mb-3">
                    <label for="nombre" class="form-label">Nombre:</label>
                    <input type="text" class="form-control" id="nombre" name="nombre" value="<%= nombre %>" required>
                    <div class="invalid-feedback">El nombre es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label for="raza" class="form-label">Raza:</label>
                    <input type="text" class="form-control" id="raza" name="raza" value="<%= raza %>" required>
                    <div class="invalid-feedback">La raza es obligatoria.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Sexo:</label>
                    <div class="d-flex gap-3">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="sexo" id="sexoMacho" value="MACHO" <%= "MACHO".equals(sexo) ? "checked" : "" %> required>
                            <label class="form-check-label" for="sexoMacho">Macho</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="sexo" id="sexoHembra" value="HEMBRA" <%= "HEMBRA".equals(sexo) ? "checked" : "" %>>
                            <label class="form-check-label" for="sexoHembra">Hembra</label>
                        </div>
                    </div>
                    <div class="invalid-feedback">El sexo es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label for="fechaNacimiento" class="form-label">Fecha de Nacimiento:</label>
                    <input type="date" class="form-control" id="fechaNacimiento" name="fechaNacimiento" value="<%= fechaNacimientoStr %>" required>
                    <div class="invalid-feedback">La fecha de nacimiento es obligatoria.</div>
                </div>

                <div class="mb-3">
                    <label for="peso" class="form-label">Peso (kg):</label>
                    <input type="number" class="form-control" id="peso" name="peso" step="0.1" min="0.1" value="<%= peso == 0.0 ? "" : String.format(Locale.US, "%.1f", peso) %>" required>
                    <div class="invalid-feedback">El peso es obligatorio y debe ser mayor que 0.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Esterilizado/a:</label>
                    <div class="d-flex gap-3">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="esterilizado" id="esterilizadoSi" value="true" <%= (mascota != null && mascota.isEsterilizado()) ? "checked" : "" %> required>
                            <label class="form-check-label" for="esterilizadoSi">Sí</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="esterilizado" id="esterilizadoNo" value="false" <%= (mascota == null || !mascota.isEsterilizado()) ? "checked" : "" %>>
                            <label class="form-check-label" for="esterilizadoNo">No</label>
                        </div>
                    </div>
                    <div class="invalid-feedback">Este campo es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label for="tipo" class="form-label">Tipo de Mascota:</label>
                    <select class="form-select" id="tipo" name="tipo" required onchange="togglePetSpecificFields()">
                        <option value="">-- Selecciona el tipo --</option>
                        <option value="Perro" <%= "Perro".equals(tipo) ? "selected" : "" %>>Perro</option>
                        <option value="Gato" <%= "Gato".equals(tipo) ? "selected" : "" %>>Gato</option>
                    </select>
                    <div class="invalid-feedback">El tipo de mascota es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Nivel de Actividad:</label>
                    <div class="d-flex gap-3">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="nivelActividad" id="actividadSedentario" value="SEDENTARIO" <%= "SEDENTARIO".equals(nivelActividad) ? "checked" : "" %> required>
                            <label class="form-check-label" for="actividadSedentario">Sedentario</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="nivelActividad" id="actividadModerado" value="MODERADO" <%= "MODERADO".equals(nivelActividad) ? "checked" : "" %>>
                            <label class="form-check-label" for="actividadModerado">Moderado</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="nivelActividad" id="actividadActivo" value="ACTIVO" <%= "ACTIVO".equals(nivelActividad) ? "checked" : "" %>>
                            <label class="form-check-label" for="actividadActivo">Activo</label>
                        </div>
                        <div class="form-check form-check-inline" id="nivelMuyActivoGroup"> <%-- ID para controlar visibilidad --%>
                            <input class="form-check-input" type="radio" name="nivelActividad" id="actividadMuyActivo" value="MUY_ACTIVO" <%= "MUY_ACTIVO".equals(nivelActividad) ? "checked" : "" %>>
                            <label class="form-check-label" for="actividadMuyActivo">Muy Activo</label>
                        </div>
                    </div>
                    <div class="invalid-feedback">El nivel de actividad es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label for="condicionSalud" class="form-label">Condición de Salud (opcional):</label>
                    <input type="text" class="form-control" id="condicionSalud" name="condicionSalud" value="<%= condicionSalud %>" placeholder="Ej: Normal, Sobrepeso, Senior, Enfermedad renal">
                    <small class="form-text text-muted">Añade cualquier condición médica o estado relevante.</small>
                </div>

                <div class="mb-3">
                    <label for="color" class="form-label">Color:</label>
                    <input type="text" class="form-control" id="color" name="color" value="<%= color %>">
                </div>

                <div class="mb-3">
                    <label for="chipID" class="form-label">Número de Chip (opcional):</label>
                    <input type="text" class="form-control" id="chipID" name="chipID" value="<%= chipID %>">
                    <small class="form-text text-muted">Introduce el número de identificación del microchip.</small>
                </div>

                <div class="mb-3">
                    <label for="observaciones" class="form-label">Observaciones (opcional):</label>
                    <textarea class="form-control" id="observaciones" name="observaciones" rows="3"><%= observaciones %></textarea>
                    <small class="form-text text-muted">Cualquier nota adicional sobre la mascota (ej: alergias, miedos, comportamientos).</small>
                </div>

                <div class="mb-3">
                    <label for="imagenFile" class="form-label">Subir Foto de la Mascota:</label>
                    <input class="form-control" type="file" id="imagenFile" name="imagenFile" accept="image/*">
                    <%-- Mostrar vista previa de la imagen actual solo si está en modo edición Y hay una imagen --%>
                    <% if (isEditMode && mascota != null && imagen != null && !imagen.isEmpty()) { %>
                        <div class="current-image-section">
                            <p class="mb-0">Imagen actual:</p>
                            <img src="<%= request.getContextPath() %>/uploads/<%= imagen %>" alt="Imagen actual de <%= nombre %>" class="img-thumbnail-preview">
                        </div>
                    <% } %>
                </div>

                <hr>
                <h4 class="mb-3">Preferencias de Alimentación (para calculadora)</h4>

                <div class="mb-3">
                    <label class="form-label">Objetivo de Peso Predeterminado:</label>
                    <div class="d-flex gap-3">
                        <div class="form-check form-check-inline">
                            <input type="radio" id="objetivoMantener" name="objetivoPeso" value="MANTENER"
                                <%= "MANTENER".equals(objetivoPeso) || "".equals(objetivoPeso) ? "checked" : "" %> class="form-check-input">
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
                </div>

                <div class="mb-3">
                    <label class="form-label">Estado Reproductor/Especial Predeterminado:</label>
                    <div class="d-flex gap-3">
                        <div class="form-check form-check-inline">
                            <input type="radio" id="estadoNinguno" name="estadoReproductor" value="NINGUNO"
                                <%= "NINGUNO".equals(estadoReproductor) || "".equals(estadoReproductor) ? "checked" : "" %>
                                class="form-check-input" onchange="toggleCachorrosField()">
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
                        <div class="form-check form-check-inline" id="estadoCachorroGroup"> <%-- ID para controlar visibilidad --%>
                            <input type="radio" id="estadoCachorro" name="estadoReproductor" value="CACHORRO"
                                <%= "CACHORRO".equals(estadoReproductor) ? "checked" : "" %>
                                class="form-check-input" onchange="toggleCachorrosField()">
                            <label class="form-check-label" for="estadoCachorro">Cachorro</label>
                        </div>
                    </div>
                </div>

                <div class="mb-3" id="numCachorrosGroup" style="display: none;"> <%-- Inicialmente oculto --%>
                    <label for="numCachorros" class="form-label">Número de cachorros (si está en lactancia):</label>
                    <input type="number" id="numCachorros" name="numCachorros" min="1" max="15" class="form-control"
                           value="<%= (numCachorros != null && numCachorros != 0) ? numCachorros : "" %>" placeholder="Ej: 4">
                    <div class="invalid-feedback">El número de cachorros es obligatorio y debe ser mayor que 0.</div>
                </div>

                <div class="mb-3">
                    <label for="tipoAlimentoPredeterminado" class="form-label">Tipo de Alimento Predeterminado:</label>
                    <select id="tipoAlimentoPredeterminado" name="tipoAlimentoPredeterminado" class="form-select" onchange="updateKcalPredeterminado()">
                        <option value="">-- Selecciona un tipo de alimento --</option>
                        <%
                            for (Map.Entry<String, Double> entry : tiposAlimentoMap.entrySet()) {
                                String key = entry.getKey();
                                Double value = entry.getValue();
                        %>
                                <option value="<%= key %>"
                                    <%= (key.equals(tipoAlimentoPredeterminado)) ? "selected" : "" %>>
                                    <%= key.replace('_', ' ').toLowerCase() %> (<%= String.format(Locale.US, "%.0f", value) %> kcal/100g)
                                </option>
                        <%
                            }
                        %>
                    </select>
                </div>

                <div class="mb-3">
                    <label for="kcalPor100gAlimentoPredeterminado" class="form-label">Kcal por 100g de Alimento Predeterminado (Opcional, sobrescribe el valor del tipo):</label>
                    <input type="number" class="form-control" id="kcalPor100gAlimentoPredeterminado" name="kcalPor100gAlimentoPredeterminado" step="0.1" min="0.1"
                           value="<%= (kcalPor100gAlimentoPredeterminado != null) ? String.format(Locale.US, "%.1f", kcalPor100gAlimentoPredeterminado) : "" %>"
                           placeholder="Ej: 350.0">
                    <small class="form-text text-muted">Introduce las kilocalorías por 100 gramos si conoces el valor exacto de tu alimento predeterminado.</small>
                </div>

                <div class="d-flex justify-content-end gap-2 mt-4">
                    <button type="submit" class="btn btn-primary btn-lg">
                        <%= isEditMode ? "Actualizar Mascota" : "Añadir Mascota" %>
                    </button>
                    <a href="<%= request.getContextPath() %>/MascotaServlet" class="btn btn-secondary btn-lg">
                        Cancelar
                    </a>
                    <%-- Mostrar botón "Calcular Comida" solo si está en modo edición y la mascota existe --%>
                    <% if (isEditMode && mascota != null) { %>
                        <a href="<%= request.getContextPath() %>/CalculadoraComidaServlet?idMascota=<%= mascota.getIdMascota() %>" class="btn btn-info btn-lg">
                            Calcular Comida
                        </a>
                    <% } %>
                </div>
            </form>
        </div>
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
            const estadoReproductorRadios = document.querySelectorAll('input[name="estadoReproductor"]');
            let estadoReproductor = '';
            estadoReproductorRadios.forEach(radio => {
                if (radio.checked) {
                    estadoReproductor = radio.value;
                }
            });

            const numCachorrosGroup = document.getElementById('numCachorrosGroup');
            const numCachorrosInput = document.getElementById('numCachorros');

            if (estadoReproductor === 'LACTANCIA') {
                numCachorrosGroup.style.display = 'block';
                numCachorrosInput.setAttribute('required', 'required');
            } else {
                numCachorrosGroup.style.display = 'none';
                numCachorrosInput.removeAttribute('required');
                numCachorrosInput.value = ''; // Limpiar el valor cuando se oculta
                // Asegurarse de que no tenga la clase is-invalid si se oculta
                numCachorrosInput.classList.remove('is-invalid');
                const feedbackDiv = numCachorrosInput.nextElementSibling;
                if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                    feedbackDiv.style.display = 'none';
                }
            }
        }

        // Función para actualizar el campo de Kcal por 100g cuando cambia el tipo de alimento predeterminado
        function updateKcalPredeterminado() {
            const tipoAlimentoSelect = document.getElementById('tipoAlimentoPredeterminado');
            const kcalInput = document.getElementById('kcalPor100gAlimentoPredeterminado');
            const selectedType = tipoAlimentoSelect.value;

            if (selectedType && jsTiposAlimento[selectedType] !== undefined) { // Usar !== undefined para manejar 0.0 correctamente
                kcalInput.value = jsTiposAlimento[selectedType];
            } else {
                kcalInput.value = '';
            }
        }

        // Función para mostrar/ocultar campos específicos de perro/gato
        function togglePetSpecificFields() {
            const tipoSelect = document.getElementById('tipo');
            const selectedTipo = tipoSelect.value; // Obtener el valor del select

            const nivelMuyActivoGroup = document.getElementById('nivelMuyActivoGroup');
            const actividadMuyActivoRadio = document.getElementById('actividadMuyActivo');
            const actividadModeradoRadio = document.getElementById('actividadModerado');

            const estadoCachorroGroup = document.getElementById('estadoCachorroGroup');
            const estadoCachorroRadio = document.getElementById('estadoCachorro');
            const estadoNingunoRadio = document.getElementById('estadoNinguno');
            
            if (selectedTipo === 'Perro') {
                // Es un perro
                nivelMuyActivoGroup.style.display = 'inline-block'; // Mostrar "Muy Activo"
                actividadMuyActivoRadio.setAttribute('required', 'required'); // Hacerlo requerido para perros
                
                estadoCachorroGroup.style.display = 'inline-block'; // Mostrar "Cachorro" en estado reproductor
                estadoCachorroRadio.setAttribute('required', 'required'); // Hacerlo requerido para perros
                
            } else if (selectedTipo === 'Gato') {
                // Es un gato
                nivelMuyActivoGroup.style.display = 'none'; // Ocultar "Muy Activo"
                actividadMuyActivoRadio.removeAttribute('required'); // Quitar requerido
                // Si "Muy Activo" estaba seleccionado, deseleccionarlo y seleccionar "Moderado"
                if (actividadMuyActivoRadio && actividadMuyActivoRadio.checked) {
                    if (actividadModeradoRadio) {
                        actividadModeradoRadio.checked = true;
                    } else {
                        // Fallback si Moderado no existe (debería existir)
                        document.getElementById('actividadSedentario').checked = true;
                    }
                }
                // Limpiar validación si estaba marcada
                actividadMuyActivoRadio.classList.remove('is-invalid');
                const muyActivoFeedback = nivelMuyActivoGroup.querySelector('.invalid-feedback');
                if (muyActivoFeedback) muyActivoFeedback.style.display = 'none';


                estadoCachorroGroup.style.display = 'none'; // Ocultar "Cachorro" en estado reproductor
                estadoCachorroRadio.removeAttribute('required'); // Quitar requerido
                // Si "Cachorro" estaba seleccionado, deseleccionarlo y seleccionar "Ninguno"
                if (estadoCachorroRadio && estadoCachorroRadio.checked) {
                    if (estadoNingunoRadio) {
                        estadoNingunoRadio.checked = true;
                    } else {
                        // Fallback si Ninguno no existe (debería existir)
                        document.getElementById('estadoGestacion').checked = true; // O alguna otra opción por defecto
                    }
                }
                // Limpiar validación si estaba marcada
                estadoCachorroRadio.classList.remove('is-invalid');
                const cachorroFeedback = estadoCachorroGroup.querySelector('.invalid-feedback');
                if (cachorroFeedback) cachorroFeedback.style.display = 'none';

            } else {
                // Si no se ha seleccionado ningún tipo (opción "-- Selecciona el tipo --")
                nivelMuyActivoGroup.style.display = 'inline-block'; // Mantener visible por defecto
                actividadMuyActivoRadio.setAttribute('required', 'required'); // Mantener requerido
                
                estadoCachorroGroup.style.display = 'inline-block'; // Mantener visible por defecto
                estadoCachorroRadio.setAttribute('required', 'required'); // Mantener requerido
            }
            // Asegurarse de que el campo de cachorros se ajuste después de cambiar el tipo de mascota
            toggleCachorrosField();
        }


        // Validación del formulario en el lado del cliente
        document.addEventListener('DOMContentLoaded', function() {
            // Inicializar el estado de los campos dependientes al cargar la página
            toggleCachorrosField();
            togglePetSpecificFields(); // Llamar al inicio para ajustar según el tipo precargado

            const mascotaForm = document.getElementById('mascotaForm');

            mascotaForm.addEventListener('submit', function(event) {
                let isValid = true;

                // Limpiar todos los mensajes de validación y clases 'is-invalid' antes de revalidar
                document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
                document.querySelectorAll('.invalid-feedback').forEach(el => el.style.display = 'none');

                // Validar campos de texto, número, fecha y select con el atributo 'required'
                const requiredInputs = mascotaForm.querySelectorAll('input[type="text"][required], input[type="number"][required], input[type="date"][required], select[required]');
                requiredInputs.forEach(input => {
                    if (input.value.trim() === '' || (input.type === 'number' && parseFloat(input.value) <= 0)) {
                        isValid = false;
                        input.classList.add('is-invalid');
                        const feedbackDiv = input.nextElementSibling; // Asume que el div invalid-feedback es el siguiente hermano
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.style.display = 'block';
                        }
                    }
                });

                // Validar grupos de radio buttons (Sexo, Esterilizado/a, Nivel de Actividad, Objetivo de Peso, Estado Reproductor)
                // Se añade 'objetivoPeso' y 'estadoReproductor' a la lista de grupos a validar
                const radioGroups = ['sexo', 'esterilizado', 'nivelActividad', 'objetivoPeso', 'estadoReproductor'];
                radioGroups.forEach(groupName => {
                    const radiosInGroup = document.querySelectorAll(`input[name="${groupName}"]:not([disabled])`); // Ignorar radios deshabilitados
                    const isAnyRadioChecked = Array.from(radiosInGroup).some(radio => radio.checked);

                    // Solo validar si el grupo no está oculto o si alguna opción está visible y marcada como requerida
                    const parentDiv = radiosInGroup.length > 0 ? radiosInGroup[0].closest('.mb-3') : null;
                    const isGroupVisible = parentDiv ? parentDiv.style.display !== 'none' : true; // Asume visible por defecto

                    if (isGroupVisible && !isAnyRadioChecked) {
                        isValid = false;
                        radiosInGroup.forEach(radio => radio.classList.add('is-invalid'));
                        const feedbackDiv = radiosInGroup[0].closest('.mb-3').querySelector('.invalid-feedback');
                        if (feedbackDiv) {
                            feedbackDiv.style.display = 'block';
                        }
                    }
                });


                // Validación específica para numCachorros si está visible
                const numCachorrosInput = document.getElementById('numCachorros');
                const numCachorrosGroup = document.getElementById('numCachorrosGroup');
                if (numCachorrosGroup.style.display === 'block') {
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

                if (!isValid) {
                    event.preventDefault(); // Detener el envío del formulario
                    // Desplazarse al primer campo con error
                    const firstInvalid = document.querySelector('.is-invalid');
                    if (firstInvalid) {
                        firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                }
            });

            // Event listeners para limpiar la validación al escribir/cambiar
            document.querySelectorAll('.form-control, .form-select').forEach(input => {
                input.addEventListener('input', function() {
                    if (this.classList.contains('is-invalid')) {
                        this.classList.remove('is-invalid');
                        const feedbackDiv = this.nextElementSibling;
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
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
                    // El feedback div para radios está en el padre .mb-3
                    const feedbackDiv = this.closest('.mb-3').querySelector('.invalid-feedback');
                    if (feedbackDiv) feedbackDiv.style.display = 'none';
                });
            });
        });
    </script>
</body>
</html>
