package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.MascotaDAO;
import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.modelo.Usuario;
import com.calculadoraperros.web.util.CalculadoraNutricional;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap; 

/**
 * Servlet para manejar la calculadora avanzada de ración de comida para mascotas.
 */
@WebServlet("/CalculadoraComidaServlet")
public class CalculadoraComidaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MascotaDAO mascotaDAO;
    private CalculadoraNutricional calculadoraNutricional;

    public void init() {
        mascotaDAO = new MascotaDAO();
        calculadoraNutricional = new CalculadoraNutricional();
        System.out.println("CalculadoraComidaServlet inicializado.");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        if (usuarioActual == null) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login.jsp");
            System.out.println("CalculadoraComidaServlet - doGet: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        try {
            // Cargar las mascotas del usuario para el selector en el formulario
            List<Mascota> mascotas = mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario());
            request.setAttribute("listaMascotas", mascotas);

            // Cargar los tipos de alimento y sus kcal/100g para el selector
            Map<String, Double> tiposAlimento = CalculadoraNutricional.getKcalPor100gAlimento();
            request.setAttribute("tiposAlimento", tiposAlimento);

            // --- LÓGICA: Precargar mascota si se pasa un ID ---
            String idMascotaParam = request.getParameter("idMascota");
            if (idMascotaParam != null && !idMascotaParam.isEmpty()) {
                try {
                    int idMascota = Integer.parseInt(idMascotaParam);
                    Mascota mascotaParaPrecargar = mascotaDAO.obtenerMascotaPorId(idMascota);
                    if (mascotaParaPrecargar != null && mascotaParaPrecargar.getIdUsuario() == usuarioActual.getIdUsuario()) {
                        request.setAttribute("mascotaSeleccionada", mascotaParaPrecargar);
                        
                        // Precargar los valores del formulario con los de la mascota
                        request.setAttribute("pesoObjetivoKg", mascotaParaPrecargar.getPeso());
                        request.setAttribute("nivelActividad", mascotaParaPrecargar.getNivelActividad());
                        request.setAttribute("objetivoPeso", mascotaParaPrecargar.getObjetivoPeso()); 
                        request.setAttribute("estadoReproductor", mascotaParaPrecargar.getEstadoReproductor()); 
                        request.setAttribute("numCachorros", mascotaParaPrecargar.getNumCachorros()); 
                        request.setAttribute("tipoAlimento", mascotaParaPrecargar.getTipoAlimentoPredeterminado()); 
                        request.setAttribute("kcalPor100gAlimento", mascotaParaPrecargar.getKcalPor100gAlimentoPredeterminado()); 
                        // El campo 'tieneEnfermedad' no se persiste en Mascota, así que se deja su valor por defecto (false)
                        // request.setAttribute("tieneEnfermedad", mascotaParaPrecargar.isTieneEnfermedad()); // Si existiera
                    } else {
                        session.setAttribute("message", "Mascota no encontrada o no tienes permiso para precargarla.");
                        session.setAttribute("messageType", "warning");
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("message", "ID de mascota inválido para precargar.");
                    session.setAttribute("messageType", "warning");
                }
            }
            // --- FIN LÓGICA DE PRECARGA ---

            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.out.println("CalculadoraComidaServlet - doGet: Mostrando formulario de calculadora para usuario ID: " + usuarioActual.getIdUsuario());

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos al cargar las mascotas: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.setAttribute("listaMascotas", new ArrayList<Mascota>()); // Asegurarse de que la lista no sea null
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doGet: Error SQL al cargar datos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado al cargar los datos para la calculadora.");
            request.setAttribute("messageType", "danger");
            request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doGet: Error inesperado al cargar datos: " + e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        if (usuarioActual == null) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login.jsp");
            System.out.println("CalculadoraComidaServlet - doPost: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        // Declarar variables para mantener los valores del formulario en caso de error
        String idMascotaParam = request.getParameter("idMascota");
        String pesoObjetivoKgStr = request.getParameter("pesoObjetivoKg");
        String nivelActividad = request.getParameter("nivelActividad");
        String objetivoPeso = request.getParameter("objetivoPeso");
        String estadoReproductor = request.getParameter("estadoReproductor");
        String numCachorrosStr = request.getParameter("numCachorros");
        boolean tieneEnfermedad = "true".equalsIgnoreCase(request.getParameter("tieneEnfermedad"));
        String tipoAlimento = request.getParameter("tipoAlimento");
        String kcalPor100gAlimentoStr = request.getParameter("kcalPor100gAlimento");

        Mascota mascotaSeleccionada = null; // Para mantener la mascota en el request en caso de error

        try {
            // Validar que se haya seleccionado una mascota
            if (idMascotaParam == null || idMascotaParam.isEmpty()) {
                throw new IllegalArgumentException("Por favor, selecciona una mascota para calcular la ración.");
            }
            int idMascota = Integer.parseInt(idMascotaParam);
            mascotaSeleccionada = mascotaDAO.obtenerMascotaPorId(idMascota);

            if (mascotaSeleccionada == null || mascotaSeleccionada.getIdUsuario() != usuarioActual.getIdUsuario()) {
                throw new IllegalArgumentException("Mascota no encontrada o no tienes permiso para acceder a ella.");
            }

            // --- Parsear y validar parámetros del formulario ---
            double pesoObjetivoKg;
            if (pesoObjetivoKgStr == null || pesoObjetivoKgStr.trim().isEmpty()) {
                throw new IllegalArgumentException("El peso objetivo no puede estar vacío.");
            }
            try {
                pesoObjetivoKg = Double.parseDouble(pesoObjetivoKgStr.replace(',', '.')); // Soporte para coma decimal
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El peso objetivo debe ser un valor numérico válido.");
            }
            if (pesoObjetivoKg <= 0) {
                throw new IllegalArgumentException("El peso objetivo debe ser un valor positivo.");
            }

            Integer numCachorros = null; // Usar Integer para permitir null
            if ("LACTANCIA".equalsIgnoreCase(estadoReproductor) && numCachorrosStr != null && !numCachorrosStr.trim().isEmpty()) {
                try {
                    numCachorros = Integer.parseInt(numCachorrosStr);
                    if (numCachorros <= 0) {
                        throw new IllegalArgumentException("El número de cachorros debe ser un valor positivo.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("El número de cachorros debe ser un valor numérico válido.");
                }
            } else if ("LACTANCIA".equalsIgnoreCase(estadoReproductor) && (numCachorrosStr == null || numCachorrosStr.trim().isEmpty())) {
                throw new IllegalArgumentException("El número de cachorros es obligatorio para el estado de lactancia.");
            }


            Double kcalPor100gAlimentoManual = null;
            if (kcalPor100gAlimentoStr != null && !kcalPor100gAlimentoStr.trim().isEmpty()) {
                try {
                    kcalPor100gAlimentoManual = Double.parseDouble(kcalPor100gAlimentoStr.replace(',', '.')); // Soporte para coma decimal
                    if (kcalPor100gAlimentoManual <= 0) {
                        throw new IllegalArgumentException("Las Kcal por 100g de alimento deben ser un valor positivo.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Las Kcal por 100g de alimento deben ser un valor numérico válido.");
                }
            }
            
            // --- Sobrescribir los valores de la mascota con los del formulario para el cálculo ---
            // Esto asegura que el cálculo use los valores que el usuario acaba de introducir,
            // no necesariamente los que están persistidos en la DB para la mascota.
            mascotaSeleccionada.setPeso(pesoObjetivoKg); // Usar el peso objetivo del formulario para el cálculo
            mascotaSeleccionada.setNivelActividad(nivelActividad);
            mascotaSeleccionada.setObjetivoPeso(objetivoPeso);
            mascotaSeleccionada.setEstadoReproductor(estadoReproductor);
            mascotaSeleccionada.setNumCachorros(numCachorros); // Puede ser null
            // Si tienes un campo 'condicionSalud' en el formulario para 'tieneEnfermedad', actualízalo
            // mascotaSeleccionada.setCondicionSalud(tieneEnfermedad ? "Enfermo" : "Normal"); // Esto dependerá de cómo manejes 'tieneEnfermedad' en Mascota

            // Priorizar kcal/100g manuales del formulario si se introdujeron
            if (kcalPor100gAlimentoManual != null) {
                mascotaSeleccionada.setKcalPor100gAlimentoPredeterminado(kcalPor100gAlimentoManual);
            } else {
                // Si no se introdujeron manuales, usar el tipo de alimento seleccionado para buscar en el mapa
                // o usar el predeterminado de la mascota si no se seleccionó un tipo nuevo.
                // La CalculadoraNutricional ya maneja la lógica de fallback si kcalPor100gPredeterminado es null
                mascotaSeleccionada.setTipoAlimentoPredeterminado(tipoAlimento);
                // Si el tipo de alimento predeterminado de la mascota no tiene un valor de kcal,
                // la CalculadoraNutricional lanzará una IllegalArgumentException.
            }

            // 3. Realizar los cálculos usando CalculadoraNutricional
            double mer = calculadoraNutricional.calcularMER(mascotaSeleccionada.getPeso()); // Usar el peso del objeto mascota (actualizado)
            
            double factorAjuste = calculadoraNutricional.calcularFactorDER(
                mascotaSeleccionada, // Pasar el objeto mascota actualizado con todos los datos
                nivelActividad,
                objetivoPeso,
                estadoReproductor,
                numCachorros != null ? numCachorros : 0, // Pasar 0 si es null, CalculadoraNutricional lo manejará
                tieneEnfermedad
            );

            double der = calculadoraNutricional.calcularDER(mer, factorAjuste);
            
            // Pasar el objeto mascota completo a calcularGramosComida
            double gramosComida = calculadoraNutricional.calcularGramosComida(der, mascotaSeleccionada); 
            
            LocalDate fechaNacimientoLocal = mascotaSeleccionada.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate fechaActual = LocalDate.now();
            Period periodo = Period.between(fechaNacimientoLocal, fechaActual);
            int totalMeses = periodo.getYears() * 12 + periodo.getMonths();
            
            int numComidas = calculadoraNutricional.recomendarNumeroComidas(totalMeses);
            String recomendaciones = calculadoraNutricional.obtenerRecomendaciones(der, gramosComida, numComidas, objetivoPeso);

            // 4. Poner los resultados y los datos de entrada en el request para mostrarlos en la JSP
            request.setAttribute("mascotaSeleccionada", mascotaSeleccionada);
            request.setAttribute("pesoObjetivoKg", pesoObjetivoKg);
            request.setAttribute("nivelActividad", nivelActividad);
            request.setAttribute("objetivoPeso", objetivoPeso);
            request.setAttribute("estadoReproductor", estadoReproductor);
            request.setAttribute("numCachorros", numCachorros);
            request.setAttribute("tieneEnfermedad", tieneEnfermedad);
            request.setAttribute("tipoAlimento", tipoAlimento);
            request.setAttribute("kcalPor100gAlimento", kcalPor100gAlimentoManual); // Mostrar el valor manual si se introdujo

            request.setAttribute("mer", mer);
            request.setAttribute("der", der);
            request.setAttribute("gramosComida", gramosComida);
            request.setAttribute("numComidas", numComidas);
            request.setAttribute("recomendaciones", recomendaciones);
            request.setAttribute("resultadosCalculados", true);

            // Recargar mascotas y tipos de alimento para el formulario (en caso de que el usuario quiera calcular de nuevo)
            List<Mascota> mascotasDisponibles = mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario());
            request.setAttribute("listaMascotas", mascotasDisponibles);
            Map<String, Double> tiposAlimentoDisponibles = CalculadoraNutricional.getKcalPor100gAlimento();
            request.setAttribute("tiposAlimento", tiposAlimentoDisponibles);

            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.out.println("CalculadoraComidaServlet - doPost: Cálculo exitoso para mascota ID: " + idMascota);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos al realizar el cálculo: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            // Recargar datos necesarios para que el formulario se muestre correctamente
            try {
                request.setAttribute("listaMascotas", mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario()));
                if (idMascotaParam != null && !idMascotaParam.isEmpty()) {
                    request.setAttribute("mascotaSeleccionada", mascotaDAO.obtenerMascotaPorId(Integer.parseInt(idMascotaParam)));
                }
            } catch (SQLException | NumberFormatException ex) {
                System.err.println("Error al recargar mascotas tras SQLException: " + ex.getMessage());
                request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            }
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            // Asegurarse de que los valores del formulario se mantengan en caso de error de DB
            request.setAttribute("pesoObjetivoKg", pesoObjetivoKgStr);
            request.setAttribute("nivelActividad", nivelActividad);
            request.setAttribute("objetivoPeso", objetivoPeso);
            request.setAttribute("estadoReproductor", estadoReproductor);
            request.setAttribute("numCachorros", numCachorrosStr);
            request.setAttribute("tieneEnfermedad", tieneEnfermedad);
            request.setAttribute("tipoAlimento", tipoAlimento);
            request.setAttribute("kcalPor100gAlimento", kcalPor100gAlimentoStr);
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doPost: Error SQL: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            request.setAttribute("message", "Error en los datos de entrada: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            // Recargar datos necesarios para que el formulario se muestre correctamente
            try {
                request.setAttribute("listaMascotas", mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario()));
                if (idMascotaParam != null && !idMascotaParam.isEmpty()) {
                    request.setAttribute("mascotaSeleccionada", mascotaDAO.obtenerMascotaPorId(Integer.parseInt(idMascotaParam)));
                }
            } catch (SQLException | NumberFormatException ex) {
                System.err.println("Error al recargar mascotas tras IllegalArgumentException: " + ex.getMessage());
                request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            }
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            // Asegurarse de que los valores del formulario se mantengan en caso de error de argumento
            request.setAttribute("pesoObjetivoKg", pesoObjetivoKgStr);
            request.setAttribute("nivelActividad", nivelActividad);
            request.setAttribute("objetivoPeso", objetivoPeso);
            request.setAttribute("estadoReproductor", estadoReproductor);
            request.setAttribute("numCachorros", numCachorrosStr);
            request.setAttribute("tieneEnfermedad", tieneEnfermedad);
            request.setAttribute("tipoAlimento", tipoAlimento);
            request.setAttribute("kcalPor100gAlimento", kcalPor100gAlimentoStr);
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doPost: Error de argumento inválido: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado al calcular la ración de comida.");
            request.setAttribute("messageType", "danger");
            // Recargar datos necesarios para que el formulario se muestre correctamente
            try {
                request.setAttribute("listaMascotas", mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario()));
                if (idMascotaParam != null && !idMascotaParam.isEmpty()) {
                    request.setAttribute("mascotaSeleccionada", mascotaDAO.obtenerMascotaPorId(Integer.parseInt(idMascotaParam)));
                }
            } catch (SQLException | NumberFormatException ex) {
                System.err.println("Error al recargar mascotas tras Exception: " + ex.getMessage());
                request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            }
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            // Asegurarse de que los valores del formulario se mantengan en caso de error inesperado
            request.setAttribute("pesoObjetivoKg", pesoObjetivoKgStr);
            request.setAttribute("nivelActividad", nivelActividad);
            request.setAttribute("objetivoPeso", objetivoPeso);
            request.setAttribute("estadoReproductor", estadoReproductor);
            request.setAttribute("numCachorros", numCachorrosStr);
            request.setAttribute("tieneEnfermedad", tieneEnfermedad);
            request.setAttribute("tipoAlimento", tipoAlimento);
            request.setAttribute("kcalPor100gAlimento", kcalPor100gAlimentoStr);
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doPost: Error inesperado: " + e.getMessage());
        }
    }
}
