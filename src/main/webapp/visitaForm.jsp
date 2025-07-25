<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.calculadoraperros.web.modelo.Mascota" %>
<%@ page import="com.calculadoraperros.web.modelo.Usuario" %>
<%@ page import="com.calculadoraperros.web.modelo.VisitaVeterinario" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.math.BigDecimal" %>
<%@ page import="java.util.Locale" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("isEditMode") != null && (Boolean)request.getAttribute("isEditMode") ? "Editar Visita" : "Añadir Nueva Visita" %> - Calculadora Perros</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
</head>
<%
    Usuario usuarioActual = (Usuario) session.getAttribute("usuario");
    if (usuarioActual == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }

    Mascota mascota = (Mascota) request.getAttribute("mascota");
    if (mascota == null) {
        // Esto no debería pasar si el servlet redirige correctamente con idMascota
        session.setAttribute("message", "No se ha especificado una mascota para registrar la visita.");
        session.setAttribute("messageType", "danger");
        response.sendRedirect(request.getContextPath() + "/MascotaServlet");
        return;
    }

    Boolean isEditModeObj = (Boolean) request.getAttribute("isEditMode");
    boolean isEditMode = (isEditModeObj != null) ? isEditModeObj.booleanValue() : false;

    VisitaVeterinario visita = (VisitaVeterinario) request.getAttribute("visita"); // Null para nuevas, poblado para edición/error

    String formTitle = isEditMode ? "Editar Visita para " + mascota.getNombre() : "Añadir Nueva Visita para " + mascota.getNombre();
    String action = isEditMode ? "actualizar" : "insertar";

    // Recuperar valores de la visita o establecer como vacío/defecto
    String fechaVisitaStr = "";
    if (visita != null && visita.getFechaVisita() != null) {
        fechaVisitaStr = new SimpleDateFormat("yyyy-MM-dd").format(visita.getFechaVisita());
    } else if (request.getParameter("fechaVisita") != null) {
        fechaVisitaStr = request.getParameter("fechaVisita");
    } else {
        fechaVisitaStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date()); // Fecha actual por defecto
    }

    String motivo = (visita != null && visita.getMotivo() != null) ? visita.getMotivo() : (request.getParameter("motivo") != null ? request.getParameter("motivo") : "");
    String diagnostico = (visita != null && visita.getDiagnostico() != null) ? visita.getDiagnostico() : (request.getParameter("diagnostico") != null ? request.getParameter("diagnostico") : "");
    String tratamiento = (visita != null && visita.getTratamiento() != null) ? visita.getTratamiento() : (request.getParameter("tratamiento") != null ? request.getParameter("tratamiento") : "");
    // CORRECCIÓN: Cambiado getMedicetamentosRecetados() a getMedicamentosRecetados()
    String medicamentosRecetados = (visita != null && visita.getMedicamentosRecetados() != null) ? visita.getMedicamentosRecetados() : (request.getParameter("medicamentosRecetados") != null ? request.getParameter("medicamentosRecetados") : "");
    
    BigDecimal costo = (visita != null && visita.getCosto() != null) ? visita.getCosto() : null;
    String costoStr = (request.getParameter("costo") != null && !request.getParameter("costo").isEmpty()) ? request.getParameter("costo") : (costo != null ? String.format(Locale.US, "%.2f", costo) : "");

    String observaciones = (visita != null && visita.getObservaciones() != null) ? visita.getObservaciones() : (request.getParameter("observaciones") != null ? request.getParameter("observaciones") : "");

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
<body class="dog-theme"> <%-- Puedes ajustar el tema si lo deseas, o hacerlo dinámico --%>
    <header class="app-header">
        <div class="text-center">
            <span class="pet-icon">🩺</span> <%-- Icono para visitas veterinarias --%>
            <h1>Gestión de Visitas Veterinarias</h1>
            <p>Registra y consulta el historial médico de tu mascota.</p>
        </div>
    </header>

    <main class="container mt-5">
        <h1 class="text-center mb-4"><%= formTitle %></h1>

        <div class="form-card mx-auto" style="max-width: 700px;">
            <form action="<%= request.getContextPath() %>/VisitaVeterinarioServlet" method="post" id="visitaForm" novalidate>
                <input type="hidden" name="action" value="<%= action %>">
                <input type="hidden" name="idMascota" value="<%= mascota.getIdMascota() %>">
                <% if (isEditMode && visita != null) { %>
                    <input type="hidden" name="idVisita" value="<%= visita.getIdVisita() %>">
                <% } %>

                <div class="mb-3">
                    <label for="fechaVisita" class="form-label">Fecha de la Visita:</label>
                    <input type="date" class="form-control" id="fechaVisita" name="fechaVisita" value="<%= fechaVisitaStr %>" required>
                    <div class="invalid-feedback">La fecha de la visita es obligatoria y no puede ser futura.</div>
                </div>

                <div class="mb-3">
                    <label for="motivo" class="form-label">Motivo de la Visita:</label>
                    <input type="text" class="form-control" id="motivo" name="motivo" value="<%= motivo %>" placeholder="Ej: Chequeo anual, Vacunación, Cojera" required>
                    <div class="invalid-feedback">El motivo de la visita es obligatorio.</div>
                </div>

                <div class="mb-3">
                    <label for="diagnostico" class="form-label">Diagnóstico (opcional):</label>
                    <textarea class="form-control" id="diagnostico" name="diagnostico" rows="3" placeholder="Ej: Otitis, Alergia estacional"><%= diagnostico %></textarea>
                </div>

                <div class="mb-3">
                    <label for="tratamiento" class="form-label">Tratamiento (opcional):</label>
                    <textarea class="form-control" id="tratamiento" name="tratamiento" rows="3" placeholder="Ej: Antibióticos, Antiinflamatorios, Dieta especial"><%= tratamiento %></textarea>
                </div>

                <div class="mb-3">
                    <label for="medicamentosRecetados" class="form-label">Medicamentos Recetados (opcional):</label>
                    <textarea class="form-control" id="medicamentosRecetados" name="medicamentosRecetados" rows="3" placeholder="Ej: Amoxicilina 250mg, Prednisona 5mg"><%= medicamentosRecetados %></textarea>
                </div>

                <div class="mb-3">
                    <label for="costo" class="form-label">Costo (€ - opcional):</label>
                    <input type="number" class="form-control" id="costo" name="costo" step="0.01" min="0" value="<%= costoStr %>" placeholder="Ej: 50.00">
                    <div class="invalid-feedback">El costo debe ser un número positivo.</div>
                </div>

                <div class="mb-3">
                    <label for="observaciones" class="form-label">Observaciones Adicionales (opcional):</label>
                    <textarea class="form-control" id="observaciones" name="observaciones" rows="3" placeholder="Cualquier nota adicional de la visita."><%= observaciones %></textarea>
                </div>

                <div class="d-flex justify-content-end gap-2 mt-4">
                    <button type="submit" class="btn btn-primary btn-lg">
                        <%= isEditMode ? "Actualizar Visita" : "Añadir Visita" %>
                    </button>
                    <a href="<%= request.getContextPath() %>/VisitaVeterinarioServlet?action=listar&idMascota=<%= mascota.getIdMascota() %>" class="btn btn-secondary btn-lg">
                        Cancelar
                    </a>
                </div>
            </form>
        </div>
    </main>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const visitaForm = document.getElementById('visitaForm');
            const fechaVisitaInput = document.getElementById('fechaVisita');
            const costoInput = document.getElementById('costo');

            // Validación del formulario en el lado del cliente
            visitaForm.addEventListener('submit', function(event) {
                let isValid = true;

                // Limpiar validaciones previas
                document.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
                document.querySelectorAll('.invalid-feedback').forEach(el => el.style.display = 'none');

                // Validar campos obligatorios
                const requiredInputs = visitaForm.querySelectorAll('input[required], select[required], textarea[required]');
                requiredInputs.forEach(input => {
                    if (input.value.trim() === '') {
                        isValid = false;
                        input.classList.add('is-invalid');
                        const feedbackDiv = input.nextElementSibling;
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.style.display = 'block';
                        }
                    }
                });

                // Validar fecha no futura
                if (fechaVisitaInput) {
                    const selectedDate = new Date(fechaVisitaInput.value);
                    const today = new Date();
                    today.setHours(0, 0, 0, 0); // Comparar solo la fecha, no la hora
                    if (selectedDate > today) {
                        isValid = false;
                        fechaVisitaInput.classList.add('is-invalid');
                        const feedbackDiv = fechaVisitaInput.nextElementSibling;
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.textContent = 'La fecha no puede ser futura.';
                            feedbackDiv.style.display = 'block';
                        }
                    }
                }

                // Validar costo (si tiene valor, que sea positivo)
                if (costoInput && costoInput.value.trim() !== '') {
                    const costoValue = parseFloat(costoInput.value.replace(',', '.'));
                    if (isNaN(costoValue) || costoValue < 0) {
                        isValid = false;
                        costoInput.classList.add('is-invalid');
                        const feedbackDiv = costoInput.nextElementSibling;
                        if (feedbackDiv && feedbackDiv.classList.contains('invalid-feedback')) {
                            feedbackDiv.textContent = 'El costo debe ser un número positivo.';
                            feedbackDiv.style.display = 'block';
                        }
                    }
                }


                if (!isValid) {
                    event.preventDefault(); // Detener el envío del formulario
                    const firstInvalid = document.querySelector('.is-invalid');
                    if (firstInvalid) {
                        firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    }
                }
            });

            // Eliminar clases de validación al escribir
            document.querySelectorAll('.form-control, .form-select, .form-check-input').forEach(input => {
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
        });
    </script>
</body>
</html>
