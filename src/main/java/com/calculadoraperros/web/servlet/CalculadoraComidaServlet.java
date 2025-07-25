package com.calculadoraperros.web.servlet;

import com.calculadoraperros.web.dao.MascotaDAO;
import com.calculadoraperros.web.modelo.Mascota;
import com.calculadoraperros.web.modelo.Usuario;
import com.calculadoraperros.web.util.CalculadoraNutricional;
import com.calculadoraperros.web.util.CalculadoraNutricional.ResultadosCalculo; // Importar la clase anidada

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Servlet para manejar la calculadora avanzada de ración de comida para mascotas.
 */
@WebServlet("/CalculadoraComidaServlet")
public class CalculadoraComidaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private MascotaDAO mascotaDAO;
    private CalculadoraNutricional calculadoraNutricional;

    public void init() {
        this.mascotaDAO = new MascotaDAO();
        this.calculadoraNutricional = new CalculadoraNutricional();
        System.out.println("CalculadoraComidaServlet inicializado.");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Usuario usuarioActual = (Usuario) session.getAttribute("usuario");

        if (usuarioActual == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            System.out.println("CalculadoraComidaServlet - doGet: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        // --- SIEMPRE RESETEAR LOS RESULTADOS AL INICIO DE CADA SOLICITUD GET ---
        // Esto asegura que la sección de resultados no se muestre a menos que se haya hecho un POST de cálculo.
        request.setAttribute("resultadosCalculados", false); 
        request.setAttribute("resultadosCalculo", null); 
        // Limpiar también los atributos de precarga en GET para asegurar un estado limpio al inicio
        // (Serán rellenados si se selecciona una mascota válida)
        request.setAttribute("mascotaSeleccionada", null);
        request.setAttribute("pesoObjetivoKg", null);
        request.setAttribute("nivelActividad", null);
        request.setAttribute("objetivoPeso", null);
        request.setAttribute("estadoReproductor", null);
        request.setAttribute("numCachorros", null);
        request.setAttribute("condicionSalud", null);
        request.setAttribute("tipoAlimento", null);
        request.setAttribute("kcalPor100gAlimento", null);
        session.removeAttribute("message"); // Limpiar mensajes de sesión
        session.removeAttribute("messageType");

        Mascota mascotaParaPrecargar = null;
        String idMascotaParam = request.getParameter("idMascota");

        try {
            // Cargar las mascotas del usuario para el selector en el formulario
            List<Mascota> mascotas = mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario());
            request.setAttribute("listaMascotas", mascotas);

            // Cargar los tipos de alimento y sus kcal/100g para el selector
            Map<String, Double> tiposAlimento = CalculadoraNutricional.getKcalPor100gAlimento();
            request.setAttribute("tiposAlimento", tiposAlimento);

            // --- LÓGICA: Precargar mascota si se pasa un ID válido ---
            if (idMascotaParam != null && !idMascotaParam.isEmpty()) {
                try {
                    int idMascota = Integer.parseInt(idMascotaParam);
                    mascotaParaPrecargar = mascotaDAO.obtenerMascotaPorId(idMascota);

                    if (mascotaParaPrecargar != null && mascotaParaPrecargar.getIdUsuario() == usuarioActual.getIdUsuario()) {
                        request.setAttribute("mascotaSeleccionada", mascotaParaPrecargar);
                        
                        // Precargar los valores del formulario con los de la mascota
                        request.setAttribute("pesoObjetivoKg", mascotaParaPrecargar.getPeso());
                        request.setAttribute("nivelActividad", mascotaParaPrecargar.getNivelActividad());
                        request.setAttribute("objetivoPeso", mascotaParaPrecargar.getObjetivoPeso());
                        request.setAttribute("estadoReproductor", mascotaParaPrecargar.getEstadoReproductor());
                        request.setAttribute("numCachorros", mascotaParaPrecargar.getNumCachorros());
                        request.setAttribute("condicionSalud", mascotaParaPrecargar.getCondicionSalud());
                        request.setAttribute("tipoAlimento", mascotaParaPrecargar.getTipoAlimentoPredeterminado());
                        request.setAttribute("kcalPor100gAlimento", mascotaParaPrecargar.getKcalPor100gAlimentoPredeterminado());
                        
                        System.out.println("CalculadoraComidaServlet - doGet: Precargando formulario para mascota ID " + idMascota);
                        
                    } else {
                        session.setAttribute("message", "Mascota no encontrada o no tienes permiso para precargarla.");
                        session.setAttribute("messageType", "warning");
                        System.out.println("CalculadoraComidaServlet - doGet: Mascota ID " + idMascotaParam + " no encontrada o sin permiso.");
                    }
                } catch (NumberFormatException e) {
                    session.setAttribute("message", "ID de mascota inválido para precargar.");
                    session.setAttribute("messageType", "warning");
                    System.out.println("CalculadoraComidaServlet - doGet: ID de mascota inválido: " + idMascotaParam);
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
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            System.out.println("CalculadoraComidaServlet - doPost: Usuario no logueado, redirigiendo a login.jsp");
            return;
        }

        // Determinar si la solicitud es para "calcular" o solo para "precargar" por cambio de mascota
        String action = request.getParameter("action"); // El botón "Calcular" tendrá name="action" value="calcular"

        // Declarar variables para mantener los valores del formulario en caso de error o precarga
        String idMascotaParam = request.getParameter("idMascota");
        String pesoObjetivoKgStr = request.getParameter("pesoObjetivoKg");
        String nivelActividad = request.getParameter("nivelActividad");
        String objetivoPeso = request.getParameter("objetivoPeso");
        String estadoReproductor = request.getParameter("estadoReproductor");
        String numCachorrosStr = request.getParameter("numCachorros");
        String condicionSaludStr = request.getParameter("condicionSalud");
        String tipoAlimento = request.getParameter("tipoAlimento");
        String kcalPor100gAlimentoStr = request.getParameter("kcalPor100gAlimento");

        Mascota mascotaSeleccionada = null;
        ResultadosCalculo resultados = null;
        boolean calculoExitoso = false; // Flag para controlar si el cálculo fue exitoso

        try {
            // 1. Validar y obtener la mascota seleccionada (necesario tanto para precarga como para cálculo)
            if (idMascotaParam == null || idMascotaParam.isEmpty()) {
                throw new IllegalArgumentException("Por favor, selecciona una mascota.");
            }
            int idMascota = parseInteger(idMascotaParam, -1);
            mascotaSeleccionada = mascotaDAO.obtenerMascotaPorId(idMascota);

            if (mascotaSeleccionada == null || mascotaSeleccionada.getIdUsuario() != usuarioActual.getIdUsuario()) {
                throw new SecurityException("Mascota no encontrada o no tienes permiso para acceder a ella.");
            }

            // --- Lógica condicional: Si es una acción de CALCULAR ---
            if ("calcular".equals(action)) {
                // 2. Preparar los parámetros para el cálculo (priorizando los del formulario)
                String tipoMascota = mascotaSeleccionada.getTipo();

                double pesoParaCalculo = parseDouble(pesoObjetivoKgStr, mascotaSeleccionada.getPeso());
                if (pesoParaCalculo <= 0) {
                    throw new IllegalArgumentException("El peso objetivo debe ser un valor positivo.");
                }

                Date fechaNacimientoParaCalculo = mascotaSeleccionada.getFechaNacimiento();
                if (fechaNacimientoParaCalculo == null) {
                    throw new IllegalArgumentException("La fecha de nacimiento de la mascota no está definida.");
                }

                boolean esterilizadoParaCalculo = mascotaSeleccionada.isEsterilizado();

                String nivelActividadParaCalculo = (nivelActividad != null && !nivelActividad.isEmpty()) ? nivelActividad : mascotaSeleccionada.getNivelActividad();
                if (nivelActividadParaCalculo == null || nivelActividadParaCalculo.isEmpty()) {
                     throw new IllegalArgumentException("El nivel de actividad es obligatorio.");
                }

                String condicionSaludParaCalculo = (condicionSaludStr != null && !condicionSaludStr.isEmpty()) ? condicionSaludStr : mascotaSeleccionada.getCondicionSalud();

                String objetivoPesoParaCalculo = (objetivoPeso != null && !objetivoPeso.isEmpty()) ? objetivoPeso : mascotaSeleccionada.getObjetivoPeso();
                if (objetivoPesoParaCalculo == null || objetivoPesoParaCalculo.isEmpty()) {
                    throw new IllegalArgumentException("El objetivo de peso es obligatorio.");
                }

                String estadoReproductorParaCalculo = (estadoReproductor != null && !estadoReproductor.isEmpty()) ? estadoReproductor : mascotaSeleccionada.getEstadoReproductor();
                if (estadoReproductorParaCalculo == null || estadoReproductorParaCalculo.isEmpty()) {
                     throw new IllegalArgumentException("El estado reproductor es obligatorio.");
                }

                Integer numCachorrosParaCalculo = null;
                if ("LACTANCIA".equalsIgnoreCase(estadoReproductorParaCalculo)) {
                    numCachorrosParaCalculo = parseInteger(numCachorrosStr, null);
                    if (numCachorrosParaCalculo == null || numCachorrosParaCalculo <= 0) {
                        throw new IllegalArgumentException("El número de cachorros es obligatorio y debe ser positivo para el estado de lactancia.");
                    }
                }
                
                String tipoAlimentoParaCalculo = (tipoAlimento != null && !tipoAlimento.isEmpty()) ? tipoAlimento : mascotaSeleccionada.getTipoAlimentoPredeterminado();
                
                Double kcalPor100gAlimentoParaCalculo = parseDouble(kcalPor100gAlimentoStr, null);
                if (kcalPor100gAlimentoParaCalculo == null || kcalPor100gAlimentoParaCalculo <= 0) {
                    if (tipoAlimentoParaCalculo != null && !tipoAlimentoParaCalculo.isEmpty()) {
                        Double defaultKcal = CalculadoraNutricional.getKcalPor100gAlimento().get(tipoAlimentoParaCalculo);
                        if (defaultKcal != null) {
                            kcalPor100gAlimentoParaCalculo = defaultKcal;
                        } else {
                            throw new IllegalArgumentException("El tipo de alimento '" + tipoAlimentoParaCalculo + "' no es reconocido y no se proporcionaron Kcal por 100g manualmente.");
                        }
                    } else {
                        throw new IllegalArgumentException("El tipo de alimento es obligatorio y las Kcal por 100g deben ser un valor positivo válido.");
                    }
                }
                if (kcalPor100gAlimentoParaCalculo <= 0) {
                    throw new IllegalArgumentException("Las Kcal por 100g de alimento deben ser un valor positivo válido.");
                }


                // 3. Realizar el cálculo
                resultados = calculadoraNutricional.calcularComida(
                    tipoMascota,
                    pesoParaCalculo,
                    fechaNacimientoParaCalculo,
                    esterilizadoParaCalculo,
                    nivelActividadParaCalculo,
                    condicionSaludParaCalculo,
                    objetivoPesoParaCalculo,
                    estadoReproductorParaCalculo,
                    numCachorrosParaCalculo,
                    kcalPor100gAlimentoParaCalculo
                );

                // 4. Poner los resultados y los datos de entrada en el request para mostrarlos en la JSP
                request.setAttribute("resultadosCalculo", resultados);
                request.setAttribute("resultadosCalculados", true); // Indicar que se han calculado resultados

                // Precargar los valores del formulario con los que se usaron para el cálculo (como Double/Integer)
                request.setAttribute("pesoObjetivoKg", pesoParaCalculo);
                request.setAttribute("nivelActividad", nivelActividadParaCalculo);
                request.setAttribute("objetivoPeso", objetivoPesoParaCalculo);
                request.setAttribute("estadoReproductor", estadoReproductorParaCalculo);
                request.setAttribute("numCachorros", numCachorrosParaCalculo);
                request.setAttribute("condicionSalud", condicionSaludParaCalculo);
                request.setAttribute("tipoAlimento", tipoAlimentoParaCalculo);
                request.setAttribute("kcalPor100gAlimento", kcalPor100gAlimentoParaCalculo);

                session.setAttribute("message", "Cálculo realizado exitosamente.");
                session.setAttribute("messageType", "success");
                calculoExitoso = true; // El cálculo fue exitoso
                System.out.println("CalculadoraComidaServlet - doPost: Cálculo exitoso para mascota ID: " + idMascota);

            } else { // --- Lógica condicional: Si NO es una acción de CALCULAR (es decir, es un cambio de mascota) ---
                // Solo precargar los datos de la mascota y resetear los resultados
                request.setAttribute("resultadosCalculados", false);
                request.setAttribute("resultadosCalculo", null);
                
                // Precargar los valores del formulario con los de la mascota seleccionada
                request.setAttribute("pesoObjetivoKg", mascotaSeleccionada.getPeso());
                request.setAttribute("nivelActividad", mascotaSeleccionada.getNivelActividad());
                request.setAttribute("objetivoPeso", mascotaSeleccionada.getObjetivoPeso());
                request.setAttribute("estadoReproductor", mascotaSeleccionada.getEstadoReproductor());
                request.setAttribute("numCachorros", mascotaSeleccionada.getNumCachorros());
                request.setAttribute("condicionSalud", mascotaSeleccionada.getCondicionSalud());
                request.setAttribute("tipoAlimento", mascotaSeleccionada.getTipoAlimentoPredeterminado());
                request.setAttribute("kcalPor100gAlimento", mascotaSeleccionada.getKcalPor100gAlimentoPredeterminado());

                System.out.println("CalculadoraComidaServlet - doPost: Precargando formulario por cambio de mascota ID: " + idMascota);
            }

        } catch (NumberFormatException e) {
            request.setAttribute("message", "Error en el formato de un número: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            e.printStackTrace();
        } catch (IllegalArgumentException | SecurityException e) {
            request.setAttribute("message", "Error en los datos de entrada: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            e.printStackTrace();
        } catch (SQLException e) {
            request.setAttribute("message", "Error de base de datos al realizar la operación: " + e.getMessage());
            request.setAttribute("messageType", "danger");
            e.printStackTrace();
        } catch (Exception e) {
            request.setAttribute("message", "Ocurrió un error inesperado al procesar la solicitud.");
            request.setAttribute("messageType", "danger");
            e.printStackTrace();
        } finally {
            // Asegurarse de que las mascotas y tipos de alimento se recarguen siempre para el formulario
            try {
                request.setAttribute("listaMascotas", mascotaDAO.obtenerTodasMascotasPorUsuario(usuarioActual.getIdUsuario()));
            } catch (SQLException e) {
                System.err.println("Error al recargar mascotas en finally: " + e.getMessage());
                request.setAttribute("listaMascotas", new ArrayList<Mascota>());
            }
            request.setAttribute("tiposAlimento", CalculadoraNutricional.getKcalPor100gAlimento());

            // Siempre se debe establecer la mascota seleccionada
            request.setAttribute("mascotaSeleccionada", mascotaSeleccionada);

            // Si el cálculo NO fue exitoso O NO FUE UNA ACCIÓN DE CALCULAR,
            // precargar los valores del formulario con los STRINGS originales del POST.
            // Esto es CRÍTICO para evitar ClassCastException si hubo un error antes de parsear los valores
            // O para mantener los valores del formulario si solo fue una precarga.
            if (!calculoExitoso || !"calcular".equals(action)) {
                // Solo si no fue un cálculo exitoso O si la acción no fue "calcular",
                // entonces usamos los strings originales del request para precargar los campos.
                // Si fue un cálculo exitoso, ya se usaron los Double/Integer parseados para precargar.
                if (!calculoExitoso) { // Si hubo un error en el cálculo
                    request.setAttribute("pesoObjetivoKg", pesoObjetivoKgStr);
                    request.setAttribute("nivelActividad", nivelActividad);
                    request.setAttribute("objetivoPeso", objetivoPeso);
                    request.setAttribute("estadoReproductor", estadoReproductor);
                    request.setAttribute("numCachorros", numCachorrosStr);
                    request.setAttribute("condicionSalud", condicionSaludStr);
                    request.setAttribute("tipoAlimento", tipoAlimento);
                    request.setAttribute("kcalPor100gAlimento", kcalPor100gAlimentoStr);
                    request.setAttribute("resultadosCalculados", false); // No mostrar resultados si hubo error
                    request.setAttribute("resultadosCalculo", null); // Asegurar que no se muestren resultados
                }
            }

            request.getRequestDispatcher("/calculadoraComida.jsp").forward(request, response);
        }
    }

    // --- Métodos auxiliares para parsear parámetros de forma segura ---

    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.replace(',', '.')); // Soporte para coma decimal
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico inválido para: " + value, e);
        }
    }

    private Double parseDouble(String value, Double defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value.replace(',', '.')); // Soporte para coma decimal
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico inválido para: " + value, e);
        }
    }

    private int parseInteger(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico entero inválido para: " + value, e);
        }
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Formato numérico entero inválido para: " + value, e);
        }
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value);
    }
}
