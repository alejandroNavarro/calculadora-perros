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
        // --- Factores para PERROS ---
        // Adultos Esterilizados/Castrados
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_CASTRADO_SEDENTARIO", 1.4);
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_CASTRADO_MODERADO", 1.6);
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_CASTRADO_ACTIVO", 1.8);
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_CASTRADO_MUY_ACTIVO", 2.0);

        // Adultos Intactos (No esterilizados/castrados)
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_INTACTO_SEDENTARIO", 1.6);
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_INTACTO_MODERADO", 1.8);
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_INTACTO_ACTIVO", 2.2);
        FACTORES_AJUSTE_DER.put("PERRO_ADULTO_INTACTO_MUY_ACTIVO", 2.5);

        // Cachorros de Perro (por edad)
        FACTORES_AJUSTE_DER.put("PERRO_CACHORRO_0_4_MESES", 3.0);
        FACTORES_AJUSTE_DER.put("PERRO_CACHORRO_4_8_MESES", 2.5);
        FACTORES_AJUSTE_DER.put("PERRO_CACHORRO_8_12_MESES", 2.0);

        // --- Factores para GATOS ---
        // Adultos Esterilizados/Castrados
        FACTORES_AJUSTE_DER.put("GATO_ADULTO_CASTRADO_SEDENTARIO", 1.0); // Típicamente más bajo para gatos
        FACTORES_AJUSTE_DER.put("GATO_ADULTO_CASTRADO_MODERADO", 1.2);
        FACTORES_AJUSTE_DER.put("GATO_ADULTO_CASTRADO_ACTIVO", 1.4);

        // Adultos Intactos
        FACTORES_AJUSTE_DER.put("GATO_ADULTO_INTACTO_SEDENTARIO", 1.2);
        FACTORES_AJUSTE_DER.put("GATO_ADULTO_INTACTO_MODERADO", 1.4);
        FACTORES_AJUSTE_DER.put("GATO_ADULTO_INTACTO_ACTIVO", 1.6);

        // Cachorros de Gato (por edad)
        FACTORES_AJUSTE_DER.put("GATO_CACHORRO_0_4_MESES", 3.0);
        FACTORES_AJUSTE_DER.put("GATO_CACHORRO_4_8_MESES", 2.5);
        FACTORES_AJUSTE_DER.put("GATO_CACHORRO_8_12_MESES", 2.0);

        // --- Factores Generales (Aplicables a ambos, ajustados en la lógica) ---
        // Gestación (se puede especificar por especie si hay diferencias significativas)
        FACTORES_AJUSTE_DER.put("GESTACION_PRIMER_TERCIO", 1.8); // General
        FACTORES_AJUSTE_DER.put("GESTACION_ULTIMO_TERCIO", 2.2); // General (más alto)

        // Lactancia (base, luego se ajusta por número de crías en calcularFactorDER)
        FACTORES_AJUSTE_DER.put("LACTANCIA_BASE_PERRO", 2.5); // Factor base para perros
        FACTORES_AJUSTE_DER.put("LACTANCIA_BASE_GATO", 2.5);  // Factor base para gatos

        // Objetivo de Peso (aplicado como multiplicador final)
        FACTORES_AJUSTE_DER.put("OBJETIVO_PERDIDA_PESO", 0.8); // Reducir un 20%
        FACTORES_AJUSTE_DER.put("OBJETIVO_GANANCIA_PESO", 1.2); // Aumentar un 20%

        // Condición de Salud (ejemplos, muy simplificado)
        FACTORES_AJUSTE_DER.put("CONDICION_SENIOR_PERRO", 0.9); // Reducción para perros senior
        FACTORES_AJUSTE_DER.put("CONDICION_SENIOR_GATO", 0.85); // Reducción para gatos senior
        FACTORES_AJUSTE_DER.put("CONDICION_HIPOMETABOLICA", 0.9); // Ej: Hipotiroidismo
        FACTORES_AJUSTE_DER.put("CONDICION_HIPERMETABOLICA", 1.2); // Ej: Hipertiroidismo, recuperación de cirugía
    }

    // Calorías por 100 gramos (o por kilogramo, pero aquí usamos 100g para facilidad de cálculo)
    // Valores promedio, que pueden variar entre marcas y formulaciones.
    // EN UNA APLICACIÓN PROFESIONAL, ESTO DEBERÍA GESTIONARSE DESDE UNA BASE DE DATOS.
    private static final Map<String, Double> KCAL_POR_100G_ALIMENTO = new HashMap<>();
    static {
        KCAL_POR_100G_ALIMENTO.put("PIENSO_SECO_MANTENIMIENTO", 350.0);    // Pienso adulto estándar (300-400 kcal/100g)
        KCAL_POR_100G_ALIMENTO.put("PIENSO_SECO_ALTA_ENERGIA", 400.0);        // Pienso para cachorros, rendimiento (380-450 kcal/100g)
        KCAL_POR_100G_ALIMENTO.put("PIENSO_SECO_LIGHT", 300.0);              // Pienso bajo en calorías para control de peso (280-320 kcal/100g)
        KCAL_POR_100G_ALIMENTO.put("COMIDA_HUMEDA_LATA", 100.0);             // Comida húmeda (75-120 kcal/100g, alto contenido de agua)
        KCAL_POR_100G_ALIMENTO.put("DIETA_BARF_CRUDA", 180.0);               // Dieta cruda (muy variable, 150-250 kcal/100g dependiendo de la composición)
        KCAL_POR_100G_ALIMENTO.put("PIENSO_VETERINARIO_RENAL", 320.0);        // Ejemplo de dieta específica
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
     * @param nivelActividadForm Nivel de actividad seleccionado en el formulario ("SEDENTARIO", "MODERADO", "ACTIVO", "MUY_ACTIVO").
     * @param objetivoPesoForm Objetivo de peso seleccionado en el formulario ("MANTENER", "PERDER", "GANAR").
     * @param estadoReproductorForm Estado reproductor seleccionado en el formulario ("GESTACION", "LACTANCIA", "NINGUNO").
     * @param numCachorrosForm Si LACTANCIA, número de cachorros.
     * @param tieneEnfermedadForm True si se marcó que la mascota tiene una enfermedad que puede afectar su metabolismo.
     * @return Factor de ajuste para la DER.
     */
    public double calcularFactorDER(Mascota mascota, String nivelActividadForm, String objetivoPesoForm, String estadoReproductorForm, int numCachorrosForm, boolean tieneEnfermedadForm) {
        double baseFactor = 0.0;
        
        // Calcular edad
        LocalDate fechaNacimientoLocal = mascota.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fechaActual = LocalDate.now();
        Period periodo = Period.between(fechaNacimientoLocal, fechaActual);
        int edadTotalMeses = periodo.getYears() * 12 + periodo.getMonths();
        int edadAnos = periodo.getYears();

        String condicionSaludNormalizada = mascota.getCondicionSalud() != null ? mascota.getCondicionSalud().trim().toLowerCase() : "";
        String tipoMascota = mascota.getTipo().toUpperCase(); // "PERRO" or "GATO"

        // --- Paso 1: Determinar el factor base según Especie, Edad, Esterilización y Nivel de Actividad ---
        if ("PERRO".equals(tipoMascota)) {
            if (edadTotalMeses < 12) { // Cachorro
                if (edadTotalMeses <= 4) {
                    baseFactor = FACTORES_AJUSTE_DER.get("PERRO_CACHORRO_0_4_MESES");
                } else if (edadTotalMeses <= 8) {
                    baseFactor = FACTORES_AJUSTE_DER.get("PERRO_CACHORRO_4_8_MESES");
                } else { // 8-12 meses
                    baseFactor = FACTORES_AJUSTE_DER.get("PERRO_CACHORRO_8_12_MESES");
                }
            } else { // Perro Adulto
                String sterilizationStatus = mascota.isEsterilizado() ? "CASTRADO" : "INTACTO";
                String activityLevel = nivelActividadForm.toUpperCase();
                String key = "PERRO_ADULTO_" + sterilizationStatus + "_" + activityLevel;
                baseFactor = FACTORES_AJUSTE_DER.getOrDefault(key, 1.6); // Valor por defecto para perro adulto
            }
        } else if ("GATO".equals(tipoMascota)) {
            if (edadTotalMeses < 12) { // Gatito
                if (edadTotalMeses <= 4) {
                    baseFactor = FACTORES_AJUSTE_DER.get("GATO_CACHORRO_0_4_MESES");
                } else if (edadTotalMeses <= 8) {
                    baseFactor = FACTORES_AJUSTE_DER.get("GATO_CACHORRO_4_8_MESES");
                } else { // 8-12 meses
                    baseFactor = FACTORES_AJUSTE_DER.get("GATO_CACHORRO_8_12_MESES");
                }
            } else { // Gato Adulto
                String sterilizationStatus = mascota.isEsterilizado() ? "CASTRADO" : "INTACTO";
                String activityLevel = nivelActividadForm.toUpperCase();
                String key = "GATO_ADULTO_" + sterilizationStatus + "_" + activityLevel;
                baseFactor = FACTORES_AJUSTE_DER.getOrDefault(key, 1.2); // Valor por defecto para gato adulto
            }
        } else {
            System.err.println("Advertencia: Tipo de mascota no reconocido: '" + mascota.getTipo() + "'. Usando factor base predeterminado (1.6).");
            baseFactor = 1.6; // Fallback general si el tipo de mascota no es "perro" ni "gato"
        }

        // --- Paso 2: Aplicar modificadores/sobrescrituras para condiciones especiales (mayor prioridad) ---
        double finalFactor = baseFactor; // Se inicia con el factor base

        // Estado Reproductivo (sobrescribe factores generales de adulto/cachorro si aplica)
        if ("GESTACION".equalsIgnoreCase(estadoReproductorForm)) {
            // Se asume que el formulario o la condición de salud indicaría el tercio de gestación
            if (condicionSaludNormalizada.contains("temprana") || "GESTACION_TEMPRANA".equalsIgnoreCase(estadoReproductorForm)) {
                finalFactor = FACTORES_AJUSTE_DER.get("GESTACION_PRIMER_TERCIO");
            } else if (condicionSaludNormalizada.contains("tardia") || "GESTACION_TARDIA".equalsIgnoreCase(estadoReproductorForm)) {
                finalFactor = FACTORES_AJUSTE_DER.get("GESTACION_ULTIMO_TERCIO");
            } else { // Si solo se indica "GESTACION" sin especificar tercio
                finalFactor = FACTORES_AJUSTE_DER.get("GESTACION_PRIMER_TERCIO"); // Valor conservador
            }
        } else if ("LACTANCIA".equalsIgnoreCase(estadoReproductorForm)) {
            int actualNumCachorros = (numCachorrosForm > 0) ? numCachorrosForm : (mascota.getNumCachorros() != null ? mascota.getNumCachorros() : 1); // Por defecto 1 si no especificado

            if ("PERRO".equals(tipoMascota)) {
                // Ajuste para perros en lactancia: base + 0.5 por cachorro adicional
                finalFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_BASE_PERRO") + (actualNumCachorros - 1) * 0.5;
            } else if ("GATO".equals(tipoMascota)) {
                // Ajuste para gatos en lactancia: base + 0.7 por gatito adicional (mayor demanda)
                finalFactor = FACTORES_AJUSTE_DER.get("LACTANCIA_BASE_GATO") + (actualNumCachorros - 1) * 0.7;
            }
        } else if (("PERDER".equalsIgnoreCase(objetivoPesoForm) || "PERDER".equalsIgnoreCase(mascota.getObjetivoPeso())) && !"GANAR".equalsIgnoreCase(objetivoPesoForm)) {
            finalFactor *= FACTORES_AJUSTE_DER.get("OBJETIVO_PERDIDA_PESO");
        } else if (("GANAR".equalsIgnoreCase(objetivoPesoForm) || "GANAR".equalsIgnoreCase(mascota.getObjetivoPeso())) && !"PERDER".equalsIgnoreCase(objetivoPesoForm)) {
            finalFactor *= FACTORES_AJUSTE_DER.get("OBJETIVO_GANANCIA_PESO");
        }

        // Estado Senior (aplicado como ajuste final si no está ya cubierto por otras condiciones específicas)
        if ("PERRO".equals(tipoMascota) && edadAnos >= 7) {
            finalFactor *= FACTORES_AJUSTE_DER.get("CONDICION_SENIOR_PERRO");
        } else if ("GATO".equals(tipoMascota) && edadAnos >= 11) {
            finalFactor *= FACTORES_AJUSTE_DER.get("CONDICION_SENIOR_GATO");
        }

        // Condiciones de Salud (marcadores generales, se pueden expandir con más detalle)
        if (tieneEnfermedadForm) {
            if (condicionSaludNormalizada.contains("hipometabolica")) {
                finalFactor *= FACTORES_AJUSTE_DER.get("CONDICION_HIPOMETABOLICA");
            } else if (condicionSaludNormalizada.contains("hipermetabolica")) {
                finalFactor *= FACTORES_AJUSTE_DER.get("CONDICION_HIPERMETABOLICA");
            }
            // Aquí se podrían añadir más condiciones específicas si se tienen datos y lógica para ellas
        }

        // Asegurar un valor por defecto sensato si por alguna razón el factor final es cero o negativo
        if (finalFactor <= 0.0) {
            System.err.println("Advertencia: Factor DER calculado es cero o negativo. Estableciendo un valor predeterminado seguro de 1.6.");
            finalFactor = 1.6;
        }

        return finalFactor;
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
     * Calcula los gramos de comida necesarios al día, priorizando el valor calórico
     * predeterminado de la mascota, y si no está disponible, usando el tipo de alimento.
     *
     * @param der Demanda Energética Diaria (kcal).
     * @param mascota El objeto Mascota, que puede contener el tipo de alimento predeterminado
     * y sus kcal/100g.
     * @return Gramos de alimento necesarios al día.
     * @throws IllegalArgumentException si no se puede determinar el valor calórico del alimento.
     */
    public double calcularGramosComida(double der, Mascota mascota) {
        Double kcalPor100g = null;

        // 1. Intentar usar las kcal/100g predeterminadas de la mascota si están disponibles
        if (mascota.getKcalPor100gAlimentoPredeterminado() != null && mascota.getKcalPor100gAlimentoPredeterminado() > 0) {
            kcalPor100g = mascota.getKcalPor100gAlimentoPredeterminado();
        } else if (mascota.getTipoAlimentoPredeterminado() != null && !mascota.getTipoAlimentoPredeterminado().isEmpty()) {
            // 2. Si no, intentar usar el tipo de alimento predeterminado de la mascota para buscar en el mapa
            String tipoAlimentoNormalizado = mascota.getTipoAlimentoPredeterminado().toUpperCase();
            kcalPor100g = KCAL_POR_100G_ALIMENTO.get(tipoAlimentoNormalizado);
        }

        if (kcalPor100g == null || kcalPor100g <= 0) {
            throw new IllegalArgumentException("No se pudo determinar el valor calórico del alimento para la mascota '" + mascota.getNombre() + "'. " +
                                               "Asegúrate de que el tipo de alimento predeterminado sea válido o que las kcal/100g estén especificadas.");
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
