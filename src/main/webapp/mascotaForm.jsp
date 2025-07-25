<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
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
<%
    // Recuperar atributos de tema y icono del request (establecidos por el Servlet)
    String petThemeClass = (String) request.getAttribute("petThemeClass");
    String petIcon = (String) request.getAttribute("petIcon");

    // Fallback si los atributos no están establecidos (ej. acceso directo a la JSP o error en servlet)
    if (petThemeClass == null || petThemeClass.isEmpty()) {
        petThemeClass = "dog-theme"; // Tema por defecto
    }
    if (petIcon == null || petIcon.isEmpty()) {
        petIcon = "🐾"; // Icono por defecto
    }
%>
<body class="<%= petThemeClass %>">
    <header class="app-header">
        <div class="text-center">
            <span class="pet-icon"><%= petIcon %></span>
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
            // Estos valores pueden venir del objeto 'mascota' (si es edición o error precargado)
            // O del request.getParameter (si hubo un error de validación y se reenvía el form)
            String nombre = (mascota != null && mascota.getNombre() != null) ? mascota.getNombre() : (request.getParameter("nombre") != null ? request.getParameter("nombre") : "");
            String raza = (mascota != null && mascota.getRaza() != null) ? mascota.getRaza() : (request.getParameter("raza") != null ? request.getParameter("raza") : "");
            String sexo = (mascota != null && mascota.getSexo() != null) ? mascota.getSexo() : (request.getParameter("sexo") != null ? request.getParameter("sexo") : "");
            Date fechaNacimiento = (mascota != null) ? mascota.getFechaNacimiento() : null;
            String fechaNacimientoStr = (fechaNacimiento != null) ? new SimpleDateFormat("yyyy-MM-dd").format(fechaNacimiento) : (request.getParameter("fechaNacimiento") != null ? request.getParameter("fechaNacimiento") : "");
            
            double peso = (mascota != null) ? mascota.getPeso() : 0.0; // Primitive, cannot be null
            String pesoStr = (request.getParameter("peso") != null && !request.getParameter("peso").isEmpty()) ? request.getParameter("peso") : (peso == 0.0 ? "" : String.format(Locale.US, "%.1f", peso));
            
            boolean esterilizado = (mascota != null) ? mascota.isEsterilizado() : false; // Primitive, cannot be null
            String esterilizadoStr = (request.getParameter("esterilizado") != null) ? request.getParameter("esterilizado") : String.valueOf(esterilizado);

            String tipo = (mascota != null && mascota.getTipo() != null) ? mascota.getTipo() : (request.getParameter("tipo") != null ? request.getParameter("tipo") : "");
            String nivelActividad = (mascota != null && mascota.getNivelActividad() != null) ? mascota.getNivelActividad() : (request.getParameter("nivelActividad") != null ? request.getParameter("nivelActividad") : "");
            String condicionSalud = (mascota != null && mascota.getCondicionSalud() != null) ? mascota.getCondicionSalud() : (request.getParameter("condicionSalud") != null ? request.getParameter("condicionSalud") : "");
            String imagen = (mascota != null && mascota.getImagen() != null) ? mascota.getImagen() : (request.getParameter("imagenExistente") != null ? request.getParameter("imagenExistente") : ""); // Usar imagenExistente para precargar si se reenvía por error
            
            String color = (mascota != null && mascota.getColor() != null) ? mascota.getColor() : (request.getParameter("color") != null ? request.getParameter("color") : "");
            String chipID = (mascota != null && mascota.getChipID() != null) ? mascota.getChipID() : (request.getParameter("chipID") != null ? request.getParameter("chipID") : "");
            String observaciones = (mascota != null && mascota.getObservaciones() != null) ? mascota.getObservaciones() : (request.getParameter("observaciones") != null ? request.getParameter("observaciones") : "");

            String objetivoPeso = (mascota != null && mascota.getObjetivoPeso() != null) ? mascota.getObjetivoPeso() : (request.getParameter("objetivoPeso") != null ? request.getParameter("objetivoPeso") : "");
            String estadoReproductor = (mascota != null && mascota.getEstadoReproductor() != null) ? mascota.getEstadoReproductor() : (request.getParameter("estadoReproductor") != null ? request.getParameter("estadoReproductor") : "");
            Integer numCachorros = (mascota != null) ? mascota.getNumCachorros() : null; // Can be null
            String numCachorrosStr = (request.getParameter("numCachorros") != null && !request.getParameter("numCachorros").isEmpty()) ? request.getParameter("numCachorros") : (numCachorros != null ? String.valueOf(numCachorros) : "");

            String tipoAlimentoPredeterminado = (mascota != null && mascota.getTipoAlimentoPredeterminado() != null) ? mascota.getTipoAlimentoPredeterminado() : (request.getParameter("tipoAlimentoPredeterminado") != null ? request.getParameter("tipoAlimentoPredeterminado") : "");
            Double kcalPor100gAlimentoPredeterminado = (mascota != null) ? mascota.getKcalPor100gAlimentoPredeterminado() : null; // Can be null
            String kcalPor100gAlimentoPredeterminadoStr = (request.getParameter("kcalPor100gAlimentoPredeterminado") != null && !request.getParameter("kcalPor100gAlimentoPredeterminado").isEmpty()) ? request.getParameter("kcalPor100gAlimentoPredeterminado") : (kcalPor100gAlimentoPredeterminado != null ? String.format(Locale.US, "%.1f", kcalPor100gAlimentoPredeterminado) : "");

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

            // Mapas y listas para dropdowns (ahora cargados desde el servlet)
            Map<String, Double> tiposAlimentoMap = (Map<String, Double>) request.getAttribute("tiposAlimento");
            if (tiposAlimentoMap == null) { // Fallback si el servlet no lo carga
                tiposAlimentoMap = new LinkedHashMap<>();
                tiposAlimentoMap.put("PIENSO_SECO_MANTENIMIENTO", 350.0);
                tiposAlimentoMap.put("PIENSO_SECO_ALTA_ENERGIA", 400.0);
                tiposAlimentoMap.put("PIENSO_SECO_LIGHT", 300.0);
                tiposAlimentoMap.put("COMIDA_HUMEDA_LATA", 100.0);
                tiposAlimentoMap.put("DIETA_BARF_CRUDA", 180.0);
                tiposAlimentoMap.put("PIENSO_VETERINARIO_RENAL", 320.0);
                tiposAlimentoMap.put("PIENSO_VETERINARIO_DIABETICO", 340.0);
            }

            // --- INICIALIZACIÓN ROBUSTA DE LISTAS ---
            final List<String> listaTiposMascota = (List<String>) request.getAttribute("listaTiposMascota") != null ?
                                                  (List<String>) request.getAttribute("listaTiposMascota") : Arrays.asList("Perro", "Gato");

            final List<String> listaRazasPerro = (List<String>) request.getAttribute("listaRazasPerro") != null ?
                                                (List<String>) request.getAttribute("listaRazasPerro") : new ArrayList<>();
            
            final List<String> listaRazasGato = (List<String>) request.getAttribute("listaRazasGato") != null ?
                                               (List<String>) request.getAttribute("listaRazasGato") : new ArrayList<>();
            
            final List<String> listaNivelActividad = (List<String>) request.getAttribute("listaNivelActividad") != null ?
                                                    (List<String>) request.getAttribute("listaNivelActividad") : Arrays.asList("SEDENTARIO", "MODERADO", "ACTIVO", "MUY_ACTIVO");

            final List<String> listaObjetivoPeso = (List<String>) request.getAttribute("listaObjetivoPeso") != null ?
                                                  (List<String>) request.getAttribute("listaObjetivoPeso") : Arrays.asList("MANTENER", "PERDER", "GANAR");

            final List<String> listaEstadoReproductor = (List<String>) request.getAttribute("listaEstadoReproductor") != null ?
                                                       (List<String>) request.getAttribute("listaEstadoReproductor") : Arrays.asList("NINGUNO", "GESTACION", "LACTANCIA", "CACHORRO");
            // --- FIN INICIALIZACIÓN ROBUSTA DE LISTAS ---
        %>

        <!-- Tarjeta de bienvenida similar a la imagen -->
        <div class="welcome-card mx-auto" style="max-width: 700px;">
            <span class="pet-illustration"></span> <%-- CAMBIO: Usar pet-illustration para que sea dinámico --%>
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
                    <label for="tipo" class="form-label">Tipo de Mascota:</label>
                    <select class="form-select" id="tipo" name="tipo" required onchange="updateRazaOptions(); togglePetSpecificFields();">
                        <option value="">-- Selecciona el tipo --</option>
                        <% if (listaTiposMascota != null) {
                            for (String t : listaTiposMascota) { %>
                                <option value="<%= t %>" <%= t.equals(tipo) ? "selected" : "" %>><%= t %></option>
                            <% }
                        } %>
                    </select>
                    <div class="invalid-feedback">El tipo de mascota es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label for="raza" class="form-label">Raza:</label>
                    <select class="form-select" id="raza" name="raza" required>
                        <option value="">-- Selecciona la raza --</option>
                        <!-- Las opciones se cargarán dinámicamente con JavaScript -->
                    </select>
                    <div class="invalid-feedback">La raza es obligatoria.</div>
                </div>

                <div class="mb-3">
                    <label for="sexo" class="form-label">Sexo:</label>
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
                    <input type="number" class="form-control" id="peso" name="peso" step="0.1" min="0.1" value="<%= pesoStr %>" required>
                    <div class="invalid-feedback">El peso es obligatorio y debe ser mayor que 0.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Esterilizado/a:</label>
                    <div class="d-flex gap-3">
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="esterilizado" id="esterilizadoSi" value="true" <%= "true".equals(esterilizadoStr) ? "checked" : "" %> required>
                            <label class="form-check-label" for="esterilizadoSi">Sí</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="esterilizado" id="esterilizadoNo" value="false" <%= "false".equals(esterilizadoStr) ? "checked" : "" %>>
                            <label class="form-check-label" for="esterilizadoNo">No</label>
                        </div>
                    </div>
                    <div class="invalid-feedback">Este campo es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Nivel de Actividad:</label>
                    <div class="d-flex gap-3">
                        <% if (listaNivelActividad != null) {
                            for (String na : listaNivelActividad) { %>
                                <div class="form-check form-check-inline" id="nivel<%= na.replace("_", "") %>Group" <%= "MUY_ACTIVO".equals(na) && !"Perro".equals(tipo) ? "style='display: none;'" : "" %>>
                                    <input class="form-check-input" type="radio" name="nivelActividad" id="actividad<%= na.replace("_", "") %>" value="<%= na %>" <%= na.equals(nivelActividad) ? "checked" : "" %> <%= "MUY_ACTIVO".equals(na) && !"Perro".equals(tipo) ? "disabled" : "" %> required>
                                    <label class="form-check-label" for="actividad<%= na.replace("_", "") %>"><%= na.replace('_', ' ').toLowerCase() %></label>
                                </div>
                            <% }
                        } %>
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
                            <img src="<%= request.getContextPath() %>/uploads/<%= imagen %>" alt="Imagen actual de <%= nombre %>" class="img-thumbnail-preview" onerror="this.onerror=null;this.src='https://placehold.co/150x150/cccccc/000000?text=No+Image';">
                        </div>
                    <% } %>
                    <div id="imagePreviewContainer" class="mt-2" style="display: <%= (imagen != null && !imagen.isEmpty()) ? "block" : "none" %>;">
                        <p class="text-muted">Vista previa de la nueva imagen:</p>
                        <img id="imagePreview" src="<%= imagen != null && !imagen.isEmpty() ? request.getContextPath() + "/uploads/" + imagen : "" %>" alt="Vista previa de la mascota" class="img-thumbnail-preview" onerror="this.onerror=null;this.src='https://placehold.co/150x150/cccccc/000000?text=No+Image';">
                    </div>
                </div>

                <hr>
                <h4 class="mb-3">Preferencias de Alimentación (para calculadora)</h4>

                <div class="mb-3">
                    <label class="form-label">Objetivo de Peso Predeterminado:</label>
                    <div class="d-flex gap-3">
                        <% if (listaObjetivoPeso != null) {
                            for (String op : listaObjetivoPeso) { %>
                                <div class="form-check form-check-inline">
                                    <input type="radio" id="objetivo<%= op %>" name="objetivoPeso" value="<%= op %>"
                                        <%= op.equals(objetivoPeso) || ("MANTENER".equals(op) && "".equals(objetivoPeso)) ? "checked" : "" %> class="form-check-input" required>
                                    <label class="form-check-label" for="objetivo<%= op %>"><%= op.toLowerCase() %></label>
                                </div>
                            <% }
                        } %>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label">Estado Reproductor/Especial Predeterminado:</label>
                    <div class="d-flex gap-3">
                        <% if (listaEstadoReproductor != null) {
                            for (String er : listaEstadoReproductor) { %>
                                <div class="form-check form-check-inline" id="estado<%= er.replace("_", "") %>Group" <%= "CACHORRO".equals(er) && !"Perro".equals(tipo) ? "style='display: none;'" : "" %>>
                                    <input type="radio" id="estado<%= er.replace("_", "") %>" name="estadoReproductor" value="<%= er %>"
                                        <%= er.equals(estadoReproductor) || ("NINGUNO".equals(er) && "".equals(estadoReproductor)) ? "checked" : "" %>
                                        class="form-check-input" onchange="toggleCachorrosField()" <%= "CACHORRO".equals(er) && !"Perro".equals(tipo) ? "disabled" : "" %> required>
                                    <label class="form-check-label" for="estado<%= er.replace("_", "") %>"><%= er.replace('_', ' ').toLowerCase() %></label>
                                </div>
                            <% }
                        } %>
                    </div>
                </div>

                <div class="mb-3" id="numCachorrosGroup" style="display: none;">
                    <label for="numCachorros" class="form-label">Número de cachorros (si está en lactancia):</label>
                    <input type="number" id="numCachorros" name="numCachorros" min="1" max="15" class="form-control"
                            value="<%= numCachorrosStr %>" placeholder="Ej: 4">
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
                            value="<%= kcalPor100gAlimentoPredeterminadoStr %>"
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
        // Datos de razas para cargar dinámicamente (ahora precargados desde JSP)
        const razasPerro = [
            <% List<String> rp = (List<String>) request.getAttribute("listaRazasPerro");
               if (rp != null) {
                   for (int i = 0; i < rp.size(); i++) {
                       out.print("'" + rp.get(i) + "'" + (i < rp.size() - 1 ? "," : ""));
                   }
               } %>
        ];
        const razasGato = [
            <% List<String> rg = (List<String>) request.getAttribute("listaRazasGato");
               if (rg != null) {
                   for (int i = 0; i < rg.size(); i++) {
                       out.print("'" + rg.get(i) + "'" + (i < rg.size() - 1 ? "," : ""));
                   }
               } %>
        ];

        // Mapa de tipos de alimento y sus kcal/100g para usar en JavaScript (precargado desde JSP)
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

        document.addEventListener('DOMContentLoaded', function() {
            const tipoSelect = document.getElementById('tipo');
            const razaSelect = document.getElementById('raza');
            // const estadoReproductorSelect = document.getElementById('estadoReproductor'); // ESTO ERA EL PROBLEMA: No existe un elemento con este ID
            const numCachorrosGroup = document.getElementById('numCachorrosGroup');
            const numCachorrosInput = document.getElementById('numCachorros');
            const fotoUrlInput = document.getElementById('imagenFile');
            const imagePreview = document.getElementById('imagePreview');
            const imagePreviewContainer = document.getElementById('imagePreviewContainer');
            const tipoAlimentoPredeterminadoSelect = document.getElementById('tipoAlimentoPredeterminado');
            const kcalPor100gAlimentoPredeterminadoInput = document.getElementById('kcalPor100gAlimentoPredeterminado');
            const body = document.body;
            const petIconElement = document.querySelector('.pet-icon');
            const petIllustrationElement = document.querySelector('.pet-illustration');

            const nivelMuyActivoGroup = document.getElementById('nivelMUYACTIVOGroup');
            const actividadMuyActivoRadio = document.getElementById('actividadMuyActivo');
            const actividadModeradoRadio = document.getElementById('actividadModerado');

            const estadoCachorroGroup = document.getElementById('estadoCACHORROGroup');
            const estadoCachorroRadio = document.getElementById('estadoCachorro');
            const estadoNingunoRadio = document.getElementById('estadoNinguno');

            // --- DEBUGGING CONSOLE LOGS ---
            console.log("--- DEBUGGING mascotaForm.jsp ---");
            console.log("razasPerro (JS):", razasPerro);
            console.log("razasGato (JS):", razasGato);
            console.log("Tipo de mascota inicial (JSP):", "<%= tipo %>");
            console.log("Raza inicial (JSP):", "<%= raza %>");
            // --- FIN DEBUGGING CONSOLE LOGS ---


            // Función para actualizar las opciones de raza según el tipo de mascota
            function updateRazaOptions() {
                const selectedTipo = tipoSelect.value;
                razaSelect.innerHTML = '<option value="">-- Selecciona la raza --</option>';

                let razas = [];
                if (selectedTipo === 'Perro') {
                    razas = razasPerro;
                } else if (selectedTipo === 'Gato') {
                    razas = razasGato;
                }

                razas.forEach(raza => {
                    const option = document.createElement('option');
                    option.value = raza;
                    option.textContent = raza;
                    razaSelect.appendChild(option);
                });

                // Seleccionar la raza actual si existe y coincide con el nuevo tipo
                // Esta variable 'currentRaza' viene precargada de la JSP si estamos en modo edición
                const currentRaza = "<%= raza %>"; 
                console.log("updateRazaOptions - selectedTipo:", selectedTipo);
                console.log("updateRazaOptions - currentRaza (from JSP):", currentRaza);
                console.log("updateRazaOptions - available razas:", razas);

                if (currentRaza && razas.includes(currentRaza)) {
                    razaSelect.value = currentRaza;
                    console.log("updateRazaOptions - Raza seleccionada:", currentRaza);
                } else {
                    // Si la raza precargada no coincide con el nuevo tipo, o no hay raza precargada,
                    // asegurarse de que el select de raza esté en la opción por defecto.
                    razaSelect.value = ""; 
                    console.log("updateRazaOptions - Raza no encontrada o no coincide, reseteando selección.");
                }
            }

            // Función para mostrar/ocultar el campo de número de cachorros
            function toggleCachorrosField() {
                // Obtener el valor del botón de radio seleccionado para "estadoReproductor"
                const estadoReproductorRadios = document.querySelectorAll('input[name="estadoReproductor"]');
                let estadoReproductorValue = '';
                for (const radio of estadoReproductorRadios) {
                    if (radio.checked) {
                        estadoReproductorValue = radio.value;
                        break;
                    }
                }

                if (numCachorrosGroup) {
                    if (estadoReproductorValue === 'LACTANCIA') {
                        numCachorrosGroup.style.display = 'block';
                        numCachorrosInput.setAttribute('required', 'required');
                    } else {
                        numCachorrosGroup.style.display = 'none';
                        numCachorrosInput.removeAttribute('required');
                        numCachorrosInput.value = '';
                        numCachorrosInput.classList.remove('is-invalid');
                        const feedbackDiv = numCachorrosInput.nextElementSibling;
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.style.display = 'none';
                        }
                    }
                }
            }

            // Función para actualizar el campo de Kcal por 100g cuando cambia el tipo de alimento predeterminado
            function updateKcalPredeterminado() {
                const selectedType = tipoAlimentoPredeterminadoSelect.value;
                const currentKcalValue = kcalPor100gAlimentoPredeterminadoInput.value.trim();
                const defaultKcalForCurrentType = jsTiposAlimento[selectedType] !== undefined ? jsTiposAlimento[selectedType].toString() : '';

                if (currentKcalValue === '' || currentKcalValue === defaultKcalForCurrentType) {
                    if (selectedType && jsTiposAlimento[selectedType] !== undefined) {
                        kcalPor100gAlimentoPredeterminadoInput.value = jsTiposAlimento[selectedType];
                    } else {
                        kcalPor100gAlimentoPredeterminadoInput.value = '';
                    }
                }
            }

            // Función para actualizar la vista previa de la imagen
            function updateImagePreview() {
                const file = fotoUrlInput.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        imagePreview.src = e.target.result;
                        imagePreviewContainer.style.display = 'block';
                    };
                    reader.readAsDataURL(file);
                } else {
                    const existingImageUrl = "<%= request.getContextPath() %>/uploads/<%= imagen %>";
                    // Asegurarse de que la URL no sea vacía, "null" o "uploads/"
                    if (existingImageUrl && !existingImageUrl.endsWith("null") && !existingImageUrl.endsWith("uploads/")) {
                         imagePreview.src = existingImageUrl;
                         imagePreviewContainer.style.display = 'block';
                    } else {
                        imagePreview.src = ""; // Limpiar src si no hay imagen
                        imagePreviewContainer.style.display = 'none';
                    }
                }
            }

            // Función para ajustar la visibilidad de los campos específicos de perro/gato y el tema
            function togglePetSpecificFields() {
                const selectedTipo = tipoSelect.value;
                
                // Actualizar clase del body para el tema
                body.classList.remove('dog-theme', 'cat-theme');
                if (selectedTipo === 'Gato') {
                    body.classList.add('cat-theme');
                    if (petIconElement) petIconElement.textContent = '🐱';
                    if (petIllustrationElement) petIllustrationElement.textContent = '🐱';
                } else { // Por defecto o "Perro"
                    body.classList.add('dog-theme');
                    if (petIconElement) petIconElement.textContent = '�';
                    if (petIllustrationElement) petIllustrationElement.textContent = '🐶';
                }

                // Ajustar visibilidad y atributos para "Muy Activo" (solo Perros)
                if (nivelMuyActivoGroup && actividadMuyActivoRadio) {
                    if (selectedTipo === 'Perro') {
                        nivelMuyActivoGroup.style.display = 'inline-block';
                        actividadMuyActivoRadio.removeAttribute('disabled');
                        actividadMuyActivoRadio.setAttribute('required', 'required');
                    } else {
                        nivelMuyActivoGroup.style.display = 'none';
                        actividadMuyActivoRadio.setAttribute('disabled', 'true');
                        actividadMuyActivoRadio.removeAttribute('required');
                        if (actividadMuyActivoRadio.checked) {
                            if (actividadModeradoRadio) {
                                actividadModeradoRadio.checked = true;
                            } else {
                                document.getElementById('actividadSedentario').checked = true;
                            }
                        }
                        actividadMuyActivoRadio.classList.remove('is-invalid');
                        const muyActivoFeedback = nivelMuyActivoGroup.querySelector('.invalid-feedback');
                        if (muyActivoFeedback) muyActivoFeedback.style.display = 'none';
                    }
                }

                // Ajustar visibilidad y atributos para "Cachorro" (solo Perros)
                if (estadoCachorroGroup && estadoCachorroRadio) {
                    if (selectedTipo === 'Perro') {
                        estadoCachorroGroup.style.display = 'inline-block';
                        estadoCachorroRadio.removeAttribute('disabled');
                        estadoCachorroRadio.setAttribute('required', 'required');
                    } else {
                        estadoCachorroGroup.style.display = 'none';
                        estadoCachorroRadio.setAttribute('disabled', 'true');
                        estadoCachorroRadio.removeAttribute('required');
                        if (estadoCachorroRadio.checked) {
                            if (estadoNingunoRadio) {
                                estadoNingunoRadio.checked = true;
                            } else {
                                document.getElementById('estadoGestacion').checked = true;
                            }
                        }
                        estadoCachorroRadio.classList.remove('is-invalid');
                        const cachorroFeedback = estadoCachorroGroup.querySelector('.invalid-feedback');
                        if (cachorroFeedback) cachorroFeedback.style.display = 'none';
                    }
                }
                
                // Asegurarse de que toggleCachorrosField se llama DESPUÉS de que los radios de estadoReproductor
                // hayan sido ajustados por togglePetSpecificFields, si es necesario.
                // Sin embargo, en la carga inicial, se llama directamente.
                toggleCachorrosField(); 
            }


            // Validación del formulario en el lado del cliente
            const mascotaForm = document.getElementById('mascotaForm');

            mascotaForm.addEventListener('submit', function(event) {
                let isValid = true;

                document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
                document.querySelectorAll('.invalid-feedback').forEach(el => el.style.display = 'none');

                const requiredInputs = mascotaForm.querySelectorAll('input[required]:not([disabled]), select[required]:not([disabled]), textarea[required]:not([disabled])');
                requiredInputs.forEach(input => {
                    if (input.value.trim() === '') {
                        isValid = false;
                        input.classList.add('is-invalid');
                        const feedbackDiv = input.nextElementSibling;
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.style.display = 'block';
                        }
                    } else if (input.type === 'number' && parseFloat(input.value) <= 0) {
                        isValid = false;
                        input.classList.add('is-invalid');
                        const feedbackDiv = input.nextElementSibling;
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.textContent = 'El valor debe ser mayor que 0.';
                            feedbackDiv.style.display = 'block';
                        }
                    } else if (input.type === 'date') {
                        const selectedDate = new Date(input.value);
                        const today = new Date();
                        today.setHours(0, 0, 0, 0);
                        if (selectedDate > today) {
                            isValid = false;
                            input.classList.add('is-invalid');
                            const feedbackDiv = input.nextElementSibling;
                            if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                                feedbackDiv.textContent = 'La fecha no puede ser futura.';
                                feedbackDiv.style.display = 'block';
                            }
                        }
                    }
                });

                const radioGroups = ['sexo', 'esterilizado', 'nivelActividad', 'objetivoPeso', 'estadoReproductor'];
                radioGroups.forEach(groupName => {
                    const radiosInGroup = document.querySelectorAll(`input[name="${groupName}"]:not([disabled])`);
                    const isAnyRadioChecked = Array.from(radiosInGroup).some(radio => radio.checked);
                    
                    const parentDiv = radiosInGroup.length > 0 ? radiosInGroup[0].closest('.mb-3') : null;
                    const isGroupVisible = parentDiv ? (parentDiv.style.display !== 'none') : true;

                    if (isGroupVisible && !isAnyRadioChecked) {
                        isValid = false;
                        radiosInGroup.forEach(radio => radio.classList.add('is-invalid'));
                        const feedbackDiv = radiosInGroup[0].closest('.mb-3').querySelector('.invalid-feedback');
                        if (feedbackDiv) {
                            feedbackDiv.style.display = 'block';
                        }
                    }
                });

                if (numCachorrosGroup.style.display === 'block' && numCachorrosInput.hasAttribute('required')) {
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
                
                // No se necesita validar fotoUrlInput como URL, ya que es un input type="file"
                // y su valor no es una URL de red. La validación se hace a nivel de servidor si es necesario.

                if (!isValid) {
                    event.preventDefault();
                    const firstInvalid = document.querySelector('.is-invalid');
                    if (firstInvalid) {
                        firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                }
            });

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
                    const closestMb3 = this.closest('.mb-3');
                    if (closestMb3) {
                        const feedbackDiv = closestMb3.querySelector('.invalid-feedback');
                        if (feedbackDiv) feedbackDiv.style.display = 'none';
                    }
                });
            });

            fotoUrlInput.addEventListener('change', updateImagePreview);

            // Inicializar la vista previa de la imagen al cargar la página
            updateImagePreview();
            // Inicializar el tema y los campos específicos al cargar la página
            togglePetSpecificFields();
            // Inicializar las opciones de raza al cargar la página
            updateRazaOptions();
            // Inicializar la visibilidad del campo numCachorros
            // Esta llamada se hace al final para asegurar que todos los elementos estén disponibles
            // y que togglePetSpecificFields haya tenido la oportunidad de ajustar los radios.
            toggleCachorrosField(); 
        });
    </script>
</body>
</html>
