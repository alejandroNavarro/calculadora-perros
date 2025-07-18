package com.calculadoraperros.web.util; // O el paquete que uses para utilidades/lógica

import com.calculadoraperros.web.modelo.Mascota; // Importar la clase Mascota
import java.time.LocalDate; // Para cálculo de edad moderno
import java.time.Period;    // Para cálculo de edad moderno
import java.time.ZoneId;    // Para convertir Date a LocalDate
import java.util.HashMap;
import java.util.Map;

/**
 * Clase para realizar cálculos nutricionales avanzados para mascotas,
 * basándose en fórmulas de Metabolismo Energético en Reposo (MER)
 * y Demanda Energética Diaria (DER), ajustadas por diversos factores.
 *
 * Fuentes de Referencia para Factores y Fórmulas:
 * - Nutrient Requirements of Dogs and Cats (NRC, 2006) - National Research Council.
 * - WSAVA Global Nutrition Guidelines (World Small Animal Veterinary Association).
 * - FEDIAF Nutritional Guidelines for Cat and Dog Food (European Pet Food Industry Federation).
 * - Textos de nutrición veterinaria (ej. Small Animal Clinical Nutrition de Hand et al.).
 */
public class CalculadoraNutricional {

    // --- Constantes para Factores de Ajuste (DER) ---
    // Estos factores son multiplicadores del MER para obtener la DER.
    // Los valores son promedios y pueden variar ligeramente entre fuentes.
    private static final Map<String, Double> FACTORES_AJUSTE_DER = new HashMap<>();

    static {
        // Factores para perros ADULTOS
        // Perros Esterilizados/Castrados (menos activos metabólicamente)
        FACTORES_AJUSTE_DER.put("ADULTO_CASTRADO_BAJO", 1.4); // Perros con mínima actividad, en casa.
        FACTORES_AJUSTE_DER.put("ADULTO_CASTRADO_NORMAL", 1.6);    // Paseos diarios ligeros.
        FACTORES_AJUSTE_DER.put("ADULTO_CASTRADO_ACTIVO", 1.8);       // Paseos largos, juego regular.
        FACTORES_AJUSTE_DER.put("ADULTO_CASTRADO_MUY_ACTIVO", 2.0); // Perros de agility, canicross, etc.

        // Perros Intactos (No esterilizados/castrados)
        FACTORES_AJUSTE_DER.put("ADULTO_INTACTO_BAJO", 1.6);
        FACTORES_AJUSTE_DER.put("ADULTO_INTACTO_NORMAL", 1.8);
        FACTORES_AJUSTE_DER.put("ADULTO_INTACTO_ACTIVO", 2.2);
        FACTORES_AJUSTE_DER.put("ADULTO_INTACTO_MUY_ACTIVO", 2.5); // O incluso más para perros de trabajo extremo (3.0-5.0)

        // Factores para CACHORROS DE PERRO (dependen mucho de la edad y el ritmo de crecimiento)
        FACTORES_AJUSTE_DER.put("CACHORRO_PERRO_0_4_MESES", 3.0);       // Mayor demanda energética
        FACTORES_AJUSTE_DER.put("CACHORRO_PERRO_4_8_MESES", 2.5);       // Crecimiento activo pero más lento que recién nacidos
        FACTORES_AJUSTE_DER.put("CACHORRO_PERRO_8_12_MESES", 2.0);      // Etapa final de cachorro

        // Factores para GATOS ADULTOS
        FACTORES_AJUSTE_DER.put("GATO_CASTRADO_BAJO", 1.0);
        FACTORES_AJUSTE_DER.put("GATO_CASTRADO_NORMAL", 1.2);
        FACTORES_AJUSTE_DER.put("GATO_CASTRADO_ACTIVO", 1.4);

        FACTORES_AJUSTE_DER.put("GATO_INTACTO_BAJO", 1.0);
        FACTORES_AJUSTE_DER.put("GATO_INTACTO_NORMAL", 1.4);
        FACTORES_AJUSTE_DER.put("GATO_INTACTO_ACTIVO", 1.6);

        // Factores para CACHORROS DE GATO
        FACTORES_AJUSTE_DER.put("CACHORRO_GATO_0_4_MESES", 3.0);
        FACTORES_AJUSTE_DER.put("CACHORRO_GATO_4_8_MESES", 2.5);
        FACTORES_AJUSTE_DER.put("CACHORRO_GATO_8_12_MESES", 2.0);

        // Factores para Objetivos de Peso
        FACTORES_AJUSTE_DER.put("OBJETIVO_PERDIDA_PESO", 0.8); // Reducir un 20% de las calorías de mantenimiento
        FACTORES_AJUSTE_DER.put("OBJETIVO_GANANCIA_PESO", 1.2); // Aumentar un 20% de las calorías de mantenimiento

        // Factores para Estados Reproductivos/Especiales (Aplicables a ambos, perro y gato)
        FACTORES_AJUSTE_DER.put("GESTACION_PRIMER_TERCIO", 1.8);
        FACTORES_AJUSTE_DER.put("GESTACION_ULTIMO_TERCIO", 2.0);
        
        // Lactancia - Factores genéricos, se ajustarán en la lógica según el número de cachorros
        FACTORES_AJUSTE_DER.put("LACTANCIA_1_CACHORRO", 2.5); // Base para 1 cachorro
        FACTORES_AJUSTE_DER.put("LACTANCIA_2_CACHORROS", 3.0);
        FACTORES_AJUSTE_DER.put("LACTANCIA_3_4_CACHORROS", 4.0);
        FACTORES_AJUSTE_DER.put("LACTANCIA_MAS_4_CACHORROS", 5.0); // Puede ser hasta 8.0 para camadas muy grandes.

        // Factores para Enfermedades/Condiciones Médicas (Muy Variables)
        FACTORES_AJUSTE_DER.put("ENFERMEDAD_HIPOMETABOLICA", 0.9);
        FACTORES_AJUSTE_DER.put("ENFERMEDAD_HIPERMETABOLICA", 1.2);
    }

    // Calorías por 100 gramos (o por kilogramo, pero aquí usamos 100g para facilidad de cálculo)
    // Valores promedio, que pueden variar entre marcas y formulaciones.
    // EN UNA APLICACIÓN PROFESIONAL, ESTO DEBERÍA GESTIONARSE DESDE UNA BASE DE DATOS.
    private static final Map<String, Double> KCAL_POR_100G_ALIMENTO = new HashMap<>();
    static {
        KCAL_POR_100G_ALIMENTO.put("PIENSO_SECO_MANTENIMIENTO", 350.0);    // Pienso adulto estándar (300-400 kcal/100g)
        KCAL_POR_100G_ALIMENTO.put("PIENSO_SECO_ALTA_ENERGIA", 400.0);       // Pienso para cachorros, rendimiento (380-450 kcal/100g)
        KCAL_POR_100G_ALIMENTO.put("PIENSO_SECO_LIGHT", 300.0);             // Pienso bajo en calorías para control de peso (280-320 kcal/100g)
        KCAL_POR_100G_ALIMENTO.put("COMIDA_HUMEDA_LATA", 100.0);            // Comida húmeda (75-120 kcal/100g, alto contenido de agua)
        KCAL_POR_100G_ALIMENTO.put("DIETA_BARF_CRUDA", 180.0);              // Dieta cruda (muy variable, 150-250 kcal/100g dependiendo de la composición)
        KCAL_POR_100G_ALIMENTO.put("PIENSO_VETERINARIO_RENAL", 320.0);      // Ejemplo de dieta específica
        KCAL_POR_100G_ALIMENTO.put("PIENSO_VETERINARIO_DIABETICO", 340.0);
    }

    /**
     * Calcula el Metabolismo Energético en Reposo (MER) de una mascota.
     * Fórmula validada para animales de más de 2 kg: 70 * (peso_kg)^0.75
     * Para pesos muy pequeños (< 2 kg), la fórmula lineal (70 * peso_kg) a veces se prefiere,
     * pero la fórmula exponencial es más universalmente aceptada por su robustez.
     *
     * @param pesoKg Peso de la mascota en kilogramos. Para cálculos precisos, se recomienda el "peso ideal".
     * @return MER en Kilocalorías (kcal).
     * @throws IllegalArgumentException si el peso es cero o negativo.
     */
    public double calcularMER(double pesoKg) {
        if (pesoKg <= 0) {
            throw new IllegalArgumentException("El peso debe ser mayor que cero para calcular el MER.");
        }
        // Utiliza Math.pow para el exponente fraccionario.
        // Para pesos muy pequeños (<2kg), algunos usan RER = 70 * Peso en kg.
        if (pesoKg < 2) {
             return 70 * pesoKg;
        } else {
            return 70 * Math.pow(pesoKg, 0.75);
        }
    }

    /**
     * Calcula el factor de ajuste para la Demanda Energética Diaria (DER) basado en las características de la mascota.
     * Este método prioriza factores de edad (cachorro, gestación/lactancia) y condición de salud sobre los de adulto general.
     *
     * @param mascota Objeto Mascota con sus características.
     * @param nivelActividadForm Nivel de actividad seleccionado en el formulario ("BAJO", "NORMAL", "ACTIVO", "MUY_ACTIVO").
     * @param objetivoPesoForm Objetivo de peso seleccionado en el formulario ("MANTENER", "PERDER", "GANAR").
     * @param estadoReproductorForm Estado reproductor seleccionado en el formulario ("GESTACION", "LACTANCIA", "NINGUNO").
     * @param numCachorrosForm Si LACTANCIA, número de cachorros.
     * @param tieneEnfermedadForm True si se marcó que la mascota tiene una enfermedad que puede afectar su metabolismo.
     * @return Factor de ajuste para la DER.
     */
    public double calcularFactorDER(Mascota mascota, String nivelActividadForm, String objetivoPesoForm, String estadoReproductorForm, int numCachorrosForm, boolean tieneEnfermedadForm) {
        double merFactor = 0.0;
        boolean condicionEspecialAplicada = false;

        // Calcular edad en meses y años desde la fecha de nacimiento de la mascota
        LocalDate fechaNacimientoLocal = mascota.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fechaActual = LocalDate.now();
        Period periodo = Period.between(fechaNacimientoLocal, fechaActual);
        int edadTotalMeses = periodo.getYears() * 12 + periodo.getMonths();
        int edadAnos = periodo.getYears();

        String condicionSaludNormalizada = mascota.getCondicionSalud() != null ? mascota.getCondicionSalud().trim().toLowerCase() : "";

        if ("perro".equalsIgnoreCase(mascota.getTipo())) {
            // --- Prioridad ALTA para Condiciones de Salud/Etapas de Vida Específicas (Perros) ---
            if (condicionSaludNormalizada.contains("gestacion") || "GESTACION".equalsIgnoreCase(estadoReproductorForm)) {
                if (condicionSaludNormalizada.contains("temprana") || "GESTACION_TEMPRANA".equalsIgnoreCase(estadoReproductorForm)) {
                    merFactor = FACTORES_AJUSTE_DER.get("GESTACION_PRIMER_TERCIO");
                } else if (condicionSaludNormalizada.contains("tardia") || "GESTACION_TARDIA".equalsIgnoreCase(estadoReproductorForm)) {
                    merFactor = FACTORES_AJUSTE_DER.get("GESTACION_ULTIMO_TERCIO");
                } else {
                    merFactor = FACTORES_AJUSTE_DER.get("GESTACION_PRIMER_TERCIO"); // Default si es solo "GESTACION"
                }
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("lactancia") || "LACTANCIA".equalsIgnoreCase(estadoReproductorForm)) {
                if (numCachorrosForm == 1) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_1_CACHORRO");
                else if (numCachorrosForm == 2) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_2_CACHORROS");
                else if (numCachorrosForm >= 3 && numCachorrosForm <= 4) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_3_4_CACHORROS");
                else if (numCachorrosForm > 4) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_MAS_4_CACHORROS");
                else merFactor = FACTORES_AJUSTE_DER.get("ADULTO_INTACTO_NORMAL"); // Fallback si numCachorros no es válido
                condicionEspecialAplicada = true;
            } else if (edadTotalMeses < 12) { // Cachorros de perro
                if (edadTotalMeses <= 4) {
                    merFactor = FACTORES_AJUSTE_DER.get("CACHORRO_PERRO_0_4_MESES");
                } else if (edadTotalMeses <= 8) {
                    merFactor = FACTORES_AJUSTE_DER.get("CACHORRO_PERRO_4_8_MESES");
                } else { // 8-12 meses
                    merFactor = FACTORES_AJUSTE_DER.get("CACHORRO_PERRO_8_12_MESES");
                }
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("bajo peso") || "GANAR".equalsIgnoreCase(objetivoPesoForm)) {
                merFactor = FACTORES_AJUSTE_DER.get("OBJETIVO_GANANCIA_PESO");
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("obesidad") || "PERDER".equalsIgnoreCase(objetivoPesoForm)) {
                merFactor = FACTORES_AJUSTE_DER.get("OBJETIVO_PERDIDA_PESO");
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("senior") || edadAnos >= 7) { // Perros senior a partir de 7 años
                // Para seniors, el factor base es el de un adulto normal, luego se ajusta por actividad
                String claveActividadSenior = "ADULTO_" + (mascota.isEsterilizado() ? "CASTRADO_" : "INTACTO_") + nivelActividadForm.toUpperCase();
                // Los seniors suelen tener un metabolismo ligeramente más lento, se puede ajustar el factor base
                merFactor = FACTORES_AJUSTE_DER.getOrDefault(claveActividadSenior, 1.6) * 0.9; // Reducir un 10% para senior
                condicionEspecialAplicada = true;
            } else if (tieneEnfermedadForm) {
                // Esto es un placeholder. La lógica real de enfermedad es compleja.
                merFactor = FACTORES_AJUSTE_DER.getOrDefault("ENFERMEDAD_HIPERMETABOLICA", 1.2); // Default a un ligero aumento
                condicionEspecialAplicada = true;
            }

            // --- Lógica para Perros ADULTOS SALUDABLES (si no se aplicó una condición especial) ---
            if (!condicionEspecialAplicada) {
                String claveActividad = "ADULTO_" + (mascota.isEsterilizado() ? "CASTRADO_" : "INTACTO_") + nivelActividadForm.toUpperCase();
                merFactor = FACTORES_AJUSTE_DER.getOrDefault(claveActividad, 1.6); // Valor por defecto sensato
            }

        } else if ("gato".equalsIgnoreCase(mascota.getTipo())) {
            // --- Prioridad ALTA para Condiciones de Salud/Etapas de Vida Específicas (Gatos) ---
            if (condicionSaludNormalizada.contains("gestacion") || "GESTACION".equalsIgnoreCase(estadoReproductorForm)) {
                if (condicionSaludNormalizada.contains("temprana") || "GESTACION_TEMPRANA".equalsIgnoreCase(estadoReproductorForm)) {
                    merFactor = FACTORES_AJUSTE_DER.get("GESTACION_PRIMER_TERCIO");
                } else if (condicionSaludNormalizada.contains("tardia") || "GESTACION_TARDIA".equalsIgnoreCase(estadoReproductorForm)) {
                    merFactor = FACTORES_AJUSTE_DER.get("GESTACION_ULTIMO_TERCIO");
                } else {
                    merFactor = FACTORES_AJUSTE_DER.get("GESTACION_PRIMER_TERCIO"); // Default si es solo "GESTACION"
                }
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("lactancia") || "LACTANCIA".equalsIgnoreCase(estadoReproductorForm)) {
                if (numCachorrosForm == 1) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_1_CACHORRO");
                else if (numCachorrosForm == 2) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_2_CACHORROS");
                else if (numCachorrosForm >= 3 && numCachorrosForm <= 4) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_3_4_CACHORROS");
                else if (numCachorrosForm > 4) merFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_MAS_4_CACHORROS");
                else merFactor = FACTORES_AJUSTE_DER.get("GATO_INTACTO_NORMAL"); // Fallback
                condicionEspecialAplicada = true;
            } else if (edadTotalMeses < 12) { // Cachorros de gato
                if (edadTotalMeses <= 4) {
                    merFactor = FACTORES_AJUSTE_DER.get("CACHORRO_GATO_0_4_MESES");
                } else if (edadTotalMeses <= 8) {
                    merFactor = FACTORES_AJUSTE_DER.get("CACHORRO_GATO_4_8_MESES");
                } else { // 8-12 meses
                    merFactor = FACTORES_AJUSTE_DER.get("CACHORRO_GATO_8_12_MESES");
                }
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("bajo peso") || "GANAR".equalsIgnoreCase(objetivoPesoForm)) {
                merFactor = FACTORES_AJUSTE_DER.get("OBJETIVO_GANANCIA_PESO");
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("obesidad") || "PERDER".equalsIgnoreCase(objetivoPesoForm)) {
                merFactor = FACTORES_AJUSTE_DER.get("OBJETIVO_PERDIDA_PESO");
                condicionEspecialAplicada = true;
            } else if (condicionSaludNormalizada.contains("senior") || edadAnos >= 11) { // Gatos senior a partir de 11 años
                String claveActividadSenior = "GATO_" + (mascota.isEsterilizado() ? "CASTRADO_" : "INTACTO_") + nivelActividadForm.toUpperCase();
                merFactor = FACTORES_AJUSTE_DER.getOrDefault(claveActividadSenior, 1.2) * 0.9; // Reducir un 10% para senior
                condicionEspecialAplicada = true;
            } else if (tieneEnfermedadForm) {
                merFactor = FACTORES_AJUSTE_DER.getOrDefault("ENFERMEDAD_HIPERMETABOLICA", 1.0);
                condicionEspecialAplicada = true;
            }

            // --- Lógica para Gatos ADULTOS SALUDABLES (si no se aplicó una condición especial) ---
            if (!condicionEspecialAplicada) {
                String claveActividad = "GATO_" + (mascota.isEsterilizado() ? "CASTRADO_" : "INTACTO_") + nivelActividadForm.toUpperCase();
                merFactor = FACTORES_AJUSTE_DER.getOrDefault(claveActividad, 1.2); // Valor por defecto sensato para gatos
            }
        } else {
            System.err.println("Advertencia: Tipo de mascota no reconocido: '" + mascota.getTipo() + "'. Usando MER por defecto para un perro adulto saludable.");
            merFactor = 1.6; // Fallback general si el tipo de mascota no es "perro" ni "gato"
        }

        // Si por alguna razón el merFactor sigue siendo 0, establecer un valor por defecto seguro.
        if (merFactor == 0.0) {
            System.err.println("Advertencia: merFactor no pudo ser determinado. Estableciendo un valor predeterminado seguro de 1.6.");
            merFactor = 1.6;
        }

        return merFactor;
    }

    /**
     * Calcula la Demanda Energética Diaria (DER) de una mascota.
     * @param mer Metabolismo Energético en Reposo (kcal).
     * @param factorAjuste Factor de ajuste obtenido de calcularFactorDER().
     * @return DER en Kilocalorías (kcal).
     */
    public double calcularDER(double mer, double factorAjuste) {
        return mer * factorAjuste;
    }

    /**
     * Calcula los gramos de comida necesarios al día.
     * @param der Demanda Energética Diaria (kcal).
     * @param tipoAlimento Clave del tipo de alimento (ej. "PIENSO_SECO_MANTENIMIENTO").
     * @return Gramos de alimento necesarios al día.
     * @throws IllegalArgumentException si el tipo de alimento no es reconocido o no tiene valor calórico.
     */
    public double calcularGramosComida(double der, String tipoAlimento) {
        Double kcalPor100g = KCAL_POR_100G_ALIMENTO.get(tipoAlimento.toUpperCase());
        if (kcalPor100g == null || kcalPor100g <= 0) {
            throw new IllegalArgumentException("Tipo de alimento '" + tipoAlimento + "' no reconocido o valor calórico inválido. Consulta los tipos de alimento disponibles.");
        }
        // Gramos = (DER / kcal_por_100g) * 100
        return (der / kcalPor100g) * 100;
    }

    /**
     * Recomienda el número de comidas al día basado en la edad de la mascota.
     * @param edadMeses Edad de la mascota en meses.
     * @return Número recomendado de comidas al día.
     */
    public int recomendarNumeroComidas(int edadMeses) {
        if (edadMeses <= 4) { // Cachorros muy jóvenes
            return 3;
        } else if (edadMeses <= 12) { // Cachorros en crecimiento / Adultos jóvenes
            return 2;
        } else { // Adultos
            // Para adultos, 1 o 2 comidas son comunes. Dos suele ser más recomendado.
            return 2;
        }
    }

    /**
     * Proporciona recomendaciones adicionales específicas.
     * @param der Demanda Energética Diaria calculada.
     * @param gramosComida Gramos de comida calculados.
     * @param numComidas Número de comidas recomendado.
     * @param objetivoPeso Objetivo de peso ("MANTENER", "PERDER", "GANAR").
     * @return Una cadena con recomendaciones profesionales.
     */
    public String obtenerRecomendaciones(double der, double gramosComida, int numComidas, String objetivoPeso) {
        StringBuilder recomendaciones = new StringBuilder();
        recomendaciones.append("<h3>Recomendaciones Nutricionales:</h3>\n");
        recomendaciones.append("<ul>\n");
        recomendaciones.append("<li>La Demanda Energética Diaria (DER) estimada es de <strong>").append(String.format("%.0f", der)).append(" kcal/día</strong>.</li>\n");
        recomendaciones.append("<li>La ración diaria estimada de alimento es de <strong>").append(String.format("%.0f", gramosComida)).append(" gramos/día</strong>.</li>\n");
        recomendaciones.append("<li>Se recomienda dividir esta ración en <strong>").append(numComidas).append(" tomas</strong> al día.</li>\n");
        recomendaciones.append("<li>Siempre asegúrate de que tu mascota tenga acceso constante a <strong>agua fresca y limpia</strong>.</li>\n");

        if ("PERDER".equalsIgnoreCase(objetivoPeso)) {
            recomendaciones.append("<li>Si el objetivo es la <strong>pérdida de peso</strong>, es crucial controlar las raciones y evitar premios excesivos. Considera piensos específicos 'light'.</li>\n");
        } else if ("GANAR".equalsIgnoreCase(objetivoPeso)) {
            recomendaciones.append("<li>Si el objetivo es la <strong>ganancia de peso</strong>, elige alimentos con alta densidad energética y controla el progreso regularmente.</li>\n");
        }

        recomendaciones.append("<li>La cantidad de alimento es una guía y debe ajustarse individualmente según la condición corporal de tu mascota, su metabolismo único y su nivel real de actividad. Observa su peso y estado físico regularmente.</li>\n");
        recomendaciones.append("<li>Para obtener un plan dietético personalizado y abordar cualquier condición de salud específica, es **imperativo consultar a un médico veterinario o a un nutricionista veterinario certificado**.</li>\n");
        recomendaciones.append("<li>Al cambiar de alimento, hazlo gradualmente durante 7-10 días para evitar problemas digestivos.</li>\n");
        recomendaciones.append("</ul>");
        return recomendaciones.toString();
    }

    /**
     * Devuelve un mapa de los tipos de alimento disponibles con su contenido calórico.
     * @return Mapa inmutable de tipos de alimento y sus kcal/100g.
     */
    public static Map<String, Double> getKcalPor100gAlimento() {
        return new HashMap<>(KCAL_POR_100G_ALIMENTO); // Devuelve una copia para evitar modificaciones externas
    }
}
