package com.calculadoraperros.web.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Servlet principal para la aplicación Calculadora de Perros.
 * Maneja tanto la calculadora de comida como la de edad.
 *
 * La anotación @WebServlet mapea este servlet a la URL /CalculadoraServlet.
 * Es importante que esta anotación sea la única forma de mapeo para evitar duplicidades
 * con la configuración en web.xml (si existiera).
 */
@WebServlet("/CalculadoraServlet")
public class CalculadoraServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto.
     */
    public CalculadoraServlet() {
        super();
    }

    /**
     * Maneja las solicitudes GET. En este caso, simplemente redirige a la página principal.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Redirige a la página principal (index.jsp) para mostrar los formularios.
        // Esto también ayuda a limpiar la URL si alguien intenta acceder directamente al servlet.
        response.sendRedirect("index.jsp");
    }

    /**
     * Maneja las solicitudes POST de los formularios.
     * Determina qué calculadora se usó (comida o edad) y procesa los datos.
     * @param request Objeto HttpServletRequest que contiene la solicitud del cliente.
     * @param response Objeto HttpServletResponse que contiene la respuesta del servlet.
     * @throws ServletException Si ocurre un error específico del servlet.
     * @throws IOException Si ocurre un error de E/S.
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Obtiene la sesión actual del usuario. Si no existe, crea una nueva.
        HttpSession session = request.getSession();

        // Obtiene el tipo de acción (qué calculadora se está usando) del campo oculto del formulario.
        String actionType = request.getParameter("actionType");

        // Limpiar resultados anteriores de ambas calculadoras para evitar que se muestren resultados incorrectos
        // si el usuario cambia de calculadora sin recargar la página.
        session.removeAttribute("caloriasNecesarias"); // Nuevo atributo para calorías
        session.removeAttribute("gramosComida");       // Nuevo atributo para gramos
        session.removeAttribute("tipoComidaResult");   // Nuevo atributo para el tipo de comida
        session.removeAttribute("comidaResult");       // El antiguo atributo por si acaso
        session.removeAttribute("edadResult");

        if ("comida".equals(actionType)) {
            // Lógica para la calculadora de comida
            try {
                double peso = Double.parseDouble(request.getParameter("peso"));
                int edadAnos = Integer.parseInt(request.getParameter("edadAnos"));
                int edadMeses = edadAnos * 12;

                String actividad = request.getParameter("actividad");
                String razaComida = request.getParameter("razaComida"); // No usado aún, pero útil en futuras mejoras
                String tipoComida = request.getParameter("tipoComida");
                String esterilizado = request.getParameter("esterilizado");

                // Paso 1: Calcular MER (Metabolismo Energético en Reposo)
                double mer = 70 * Math.pow(peso, 0.75);

                // Paso 2: Factor de actividad ajustado
                double factorActividad = 1.0;
                switch (actividad) {
                    case "baja": factorActividad = 1.2; break;
                    case "media": factorActividad = 1.6; break;
                    case "alta": factorActividad = 2.0; break;
                }

                // Ajuste si está esterilizado
                if ("si".equalsIgnoreCase(esterilizado)) {
                    factorActividad *= 0.9; // Reducción del 10%
                }

                // Ajustes por edad
                if (edadMeses < 12) { // Cachorro
                    factorActividad *= 1.5;
                } else if (edadMeses > 96) { // Senior (8+ años)
                    factorActividad *= 0.85;
                }

                // Paso 3: Calorías diarias necesarias
                double caloriasNecesarias = mer * factorActividad;

                // Paso 4: Convertir kcal a gramos según tipo de comida
                double densidadCalorica = switch (tipoComida) {
                    case "humeda" -> 1.2;   // kcal/g aproximado
                    case "casera" -> 2.5;
                    default -> 3.5;         // pienso seco
                };
                double gramosComida = caloriasNecesarias / densidadCalorica;

                // Guardar los resultados en la sesión para que index.jsp los pueda mostrar
                session.setAttribute("caloriasNecesarias", (int) caloriasNecesarias);
                session.setAttribute("gramosComida", (int) gramosComida);
                session.setAttribute("tipoComidaResult", tipoComida);


            } catch (NumberFormatException e) {
                session.setAttribute("comidaResult", "Error: Por favor, introduce un peso y edad válidos.");
            }
        } else if ("edad".equals(actionType)) {
            // Lógica para la calculadora de edad
            try {
                int edadPerro = Integer.parseInt(request.getParameter("edad")); // Edad en años
                String raza = request.getParameter("raza"); // Este campo 'raza' es para la calculadora de edad

                int edadHumana;

                // Calcular la edad humana basada en la edad del perro y el tamaño de la raza
                if (edadPerro == 1) {
                    if ("pequeña".equals(raza)) {
                        edadHumana = 15;
                    } else if ("mediana".equals(raza)) {
                        edadHumana = 15;
                    } else { // grande
                        edadHumana = 14;
                    }
                } else if (edadPerro == 2) {
                    if ("pequeña".equals(raza)) {
                        edadHumana = 24;
                    } else if ("mediana".equals(raza)) {
                        edadHumana = 24;
                    } else { // grande
                        edadHumana = 22;
                    }
                } else {
                    // Para edades mayores a 2 años, se añade un factor por año
                    // Se asume que 2 años son 24 años humanos para razas pequeñas/medianas y 22 para grandes.
                    // Luego se añaden 4-5 años humanos por cada año adicional del perro.
                    if ("pequeña".equals(raza)) {
                        edadHumana = 24 + (edadPerro - 2) * 4;
                    } else if ("mediana".equals(raza)) {
                        edadHumana = 24 + (edadPerro - 2) * 5;
                    } else { // grande
                        edadHumana = 22 + (edadPerro - 2) * 5;
                    }
                }

                // Guardar el resultado en la sesión para que index.jsp lo pueda mostrar
                session.setAttribute("edadResult", "La edad de tu perro en años humanos es aproximadamente " + edadHumana + " años.");

            } catch (NumberFormatException e) {
                session.setAttribute("edadResult", "Error: Por favor, introduce una edad válida.");
            }
        }

        // Redirigir de vuelta a la página principal (index.jsp) para mostrar los resultados.
        // Esto evita el problema de reenvío de formulario al recargar la página.
        response.sendRedirect("index.jsp");
    }
}