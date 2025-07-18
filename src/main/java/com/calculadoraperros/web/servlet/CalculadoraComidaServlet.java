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
import java.sql.SQLException; // Importar SQLException
import java.time.LocalDate; // Para cálculo de edad
import java.time.Period;    // Para cálculo de edad
import java.time.ZoneId;    // Para conversión de Date a LocalDate
import java.util.ArrayList; // Para inicializar listas vacías en caso de error
import java.util.List;
import java.util.Map;

/**
 * Servlet para manejar la calculadora avanzada de ración de comida para perros.
 */
@WebServlet("/CalculadoraComidaServlet")
public class CalculadoraComidaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MascotaDAO mascotaDAO;
    private CalculadoraNutricional calculadoraNutricional;

    /**
     * Inicializa el Servlet y crea instancias de DAO y la calculadora.
     */
    public void init() {
        mascotaDAO = new MascotaDAO();
        calculadoraNutricional = new CalculadoraNutricional();
        System.out.println("CalculadoraComidaServlet inicializado.");
    }

    /**
     * Maneja las solicitudes GET: Muestra el formulario de la calculadora.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
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
            request.setAttribute("mascotas", mascotas);

            // Cargar los tipos de alimento y sus kcal/100g para el selector
            Map<String, Double> tiposAlimento = CalculadoraNutricional.getKcalPor100gAlimento();
            request.setAttribute("tiposAlimento", tiposAlimento);

            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.out.println("CalculadoraComidaServlet - doGet: Mostrando formulario de calculadora para usuario ID: " + usuarioActual.getIdUsuario());

        } catch (SQLException e) { // Captura específica para errores de base de datos
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos al cargar las mascotas: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            request.setAttribute("mascotas", new ArrayList<Mascota>()); // Asegurarse de que la lista no sea null
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento()); // Cargar los tipos de alimento
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doGet: Error SQL al cargar datos: " + e.getMessage());
        } catch (Exception e) { // Captura para cualquier otra excepción inesperada
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado al cargar los datos para la calculadora.");
            request.setAttribute("messageType", "danger");
            request.setAttribute("mascotas", new ArrayList<Mascota>()); // Asegurarse de que la lista no sea null
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento()); // Cargar los tipos de alimento
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doGet: Error inesperado al cargar datos: " + e.getMessage());
        }
    }

    /**
     * Maneja las solicitudes POST: Procesa los datos del formulario y realiza los cálculos.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
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

        try {
            // 1. Obtener parámetros del formulario
            int idMascota = Integer.parseInt(request.getParameter("idMascota"));
            String nivelActividad = request.getParameter("nivelActividad");
            String objetivoPeso = request.getParameter("objetivoPeso");
            String estadoReproductor = request.getParameter("estadoReproductor");
            int numCachorros = 0; // Solo relevante si estadoReproductor es LACTANCIA
            if ("LACTANCIA".equalsIgnoreCase(estadoReproductor)) {
                String numCachorrosStr = request.getParameter("numCachorros");
                if (numCachorrosStr != null && !numCachorrosStr.trim().isEmpty()) {
                    try {
                        numCachorros = Integer.parseInt(numCachorrosStr);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("El número de cachorros debe ser un valor numérico válido.");
                    }
                }
            }
            boolean tieneEnfermedad = "true".equalsIgnoreCase(request.getParameter("tieneEnfermedad"));
            String tipoAlimento = request.getParameter("tipoAlimento");

            // Validar pesoObjetivoKg antes de parsear
            String pesoObjetivoKgStr = request.getParameter("pesoObjetivoKg");
            double pesoObjetivoKg;
            if (pesoObjetivoKgStr == null || pesoObjetivoKgStr.trim().isEmpty()) {
                throw new IllegalArgumentException("El peso objetivo no puede estar vacío.");
            }
            try {
                pesoObjetivoKg = Double.parseDouble(pesoObjetivoKgStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("El peso objetivo debe ser un valor numérico válido.");
            }
            if (pesoObjetivoKg <= 0) {
                throw new IllegalArgumentException("El peso objetivo debe ser un valor positivo.");
            }

            // 2. Obtener la mascota seleccionada
            Mascota mascota = mascotaDAO.obtenerMascotaPorId(idMascota);

            if (mascota == null || mascota.getIdUsuario() != usuarioActual.getIdUsuario()) {
                session.setAttribute("message", "Mascota no encontrada o no tienes permiso para acceder a ella.");
                session.setAttribute("messageType", "danger");
                String contextPath = request.getContextPath();
                response.sendRedirect(contextPath + "/CalculadoraComidaServlet"); // Vuelve al formulario
                return;
            }

            // 3. Realizar los cálculos usando CalculadoraNutricional
            // La edad en meses se calcula internamente en CalculadoraNutricional.calcularFactorDER
            double mer = calculadoraNutricional.calcularMER(pesoObjetivoKg);
            
            // Se pasa el objeto Mascota completo a calcularFactorDER
            double factorAjuste = calculadoraNutricional.calcularFactorDER(
                mascota, // Pasa el objeto Mascota
                nivelActividad,
                objetivoPeso,
                estadoReproductor,
                numCachorros,
                tieneEnfermedad
            );

            double der = calculadoraNutricional.calcularDER(mer, factorAjuste);
            double gramosComida = calculadoraNutricional.calcularGramosComida(der, tipoAlimento);
            
            // La recomendación de número de comidas también usa la edad de la mascota
            LocalDate fechaNacimientoLocal = mascota.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate fechaActual = LocalDate.now();
            Period periodo = Period.between(fechaNacimientoLocal, fechaActual);
            int totalMeses = periodo.getYears() * 12 + periodo.getMonths(); // Calcular edad para numComidas
            
            int numComidas = calculadoraNutricional.recomendarNumeroComidas(totalMeses);
            String recomendaciones = calculadoraNutricional.obtenerRecomendaciones(der, gramosComida, numComidas, objetivoPeso);

            // 4. Poner los resultados y los datos de entrada en el request para mostrarlos en la JSP
            request.setAttribute("mascotaSeleccionada", mascota); // Para mostrar los detalles de la mascota
            request.setAttribute("pesoObjetivoKg", pesoObjetivoKg);
            request.setAttribute("nivelActividad", nivelActividad);
            request.setAttribute("objetivoPeso", objetivoPeso);
            request.setAttribute("estadoReproductor", estadoReproductor);
            request.setAttribute("numCachorros", numCachorros);
            request.setAttribute("tieneEnfermedad", tieneEnfermedad);
            request.setAttribute("tipoAlimento", tipoAlimento);

            request.setAttribute("mer", String.format("%.0f", mer)); // Formato sin decimales
            request.setAttribute("der", String.format("%.0f", der)); // Formato sin decimales
            request.setAttribute("gramosComida", String.format("%.0f", gramosComida)); // Formato sin decimales
            request.setAttribute("numComidas", numComidas);
            request.setAttribute("recomendaciones", recomendaciones);
            request.setAttribute("resultadosCalculados", true); // Bandera para que la JSP muestre los resultados

            // Recargar mascotas y tipos de alimento para el formulario (en caso de que el usuario quiera calcular de nuevo)
            List<Mascota> mascotasDisponibles = mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario());
            request.setAttribute("mascotas", mascotasDisponibles);
            Map<String, Double> tiposAlimentoDisponibles = CalculadoraNutricional.getKcalPor100gAlimento();
            request.setAttribute("tiposAlimento", tiposAlimentoDisponibles);

            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.out.println("CalculadoraComidaServlet - doPost: Cálculo exitoso para mascota ID: " + idMascota);

        } catch (SQLException e) { // Captura específica para errores de base de datos
            e.printStackTrace();
            request.setAttribute("message", "Error de base de datos al realizar el cálculo: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            // Recargar datos para el formulario antes de reenviar
            try {
                request.setAttribute("mascotas", mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario()));
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("mascotas", new ArrayList<Mascota>()); // Lista vacía si falla la recarga
            }
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doPost: Error SQL: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            request.setAttribute("message", "Error en los datos de entrada: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            // Recargar datos para el formulario antes de reenviar
            try {
                request.setAttribute("mascotas", mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario()));
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("mascotas", new ArrayList<Mascota>());
            }
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doPost: Error de argumento inválido: " + e.getMessage());
        } catch (Exception e) { // Captura para cualquier otra excepción inesperada
            e.printStackTrace();
            request.setAttribute("message", "Ocurrió un error inesperado al calcular la ración de comida.");
            request.setAttribute("messageType", "danger");
            // Recargar datos para el formulario antes de reenviar
            try {
                request.setAttribute("mascotas", mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario()));
            } catch (SQLException ex) {
                ex.printStackTrace();
                request.setAttribute("mascotas", new ArrayList<Mascota>());
            }
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());
            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
            System.err.println("CalculadoraComidaServlet - doPost: Error inesperado: " + e.getMessage());
        }
    }
}
