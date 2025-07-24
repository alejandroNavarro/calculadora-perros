package com.calculadoraperros.web.modelo;

import java.util.Date;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

/**
 * Clase de utilidad para calcular las necesidades calóricas y la cantidad de alimento
 * para una mascota, basándose en sus características.
 */
public class CalculoComida {

    // --- Constantes para los factores de MER (Maintenance Energy Requirement) ---
    // Estos factores se multiplican por el RER (Resting Energy Requirement)

    // Cachorros
    private static final double MER_FACTOR_PUPPY_0_4_MONTHS = 3.0;
    private static final double MER_FACTOR_PUPPY_4_12_MONTHS = 2.0;

    // Adultos Esterilizados/Castrados
    private static final double MER_FACTOR_ADULT_SPAYED_NEUTERED_LOW_ACTIVITY = 1.4;
    private static final double MER_FACTOR_ADULT_SPAYED_NEUTERED_MODERATE_ACTIVITY = 1.6;
    private static final double MER_FACTOR_ADULT_SPAYED_NEUTERED_HIGH_ACTIVITY = 1.8;

    // Adultos Enteros (no castrados)
    private static final double MER_FACTOR_ADULT_INTACT_LOW_ACTIVITY = 1.6;
    private static final double MER_FACTOR_ADULT_INTACT_MODERATE_ACTIVITY = 1.8;
    private static final double MER_FACTOR_ADULT_INTACT_HIGH_ACTIVITY = 2.0;

    // Estados especiales
    private static final double MER_FACTOR_GESTATION_LAST_THIRD = 1.95; // Último tercio de gestación
    private static final double MER_FACTOR_WEIGHT_LOSS = 1.0; // Para pérdida de peso
    private static final double MER_FACTOR_WEIGHT_GAIN = 1.3; // Para ganancia de peso

    /**
     * Calcula el Requerimiento Energético en Reposo (RER) de una mascota.
     * Fórmula: RER = 70 * (peso_kg)^0.75
     *
     * @param pesoKg El peso de la mascota en kilogramos.
     * @return El RER en Kcal/día.
     */
    public static double calcularRER(double pesoKg) {
        if (pesoKg <= 0) {
            throw new IllegalArgumentException("El peso de la mascota debe ser mayor que 0.");
        }
        return 70 * Math.pow(pesoKg, 0.75);
    }

    /**
     * Calcula las necesidades calóricas diarias (MER) de una mascota.
     *
     * @param mascota El objeto Mascota con todos sus atributos.
     * @return Las calorías diarias recomendadas en Kcal/día.
     */
    public static double calcularNecesidadesCaloricas(Mascota mascota) {
        double rer = calcularRER(mascota.getPeso());
        double merFactor = 1.0; // Factor base, se ajustará según las condiciones

        // Calcular edad en meses para determinar si es cachorro
        LocalDate fechaNacimiento = mascota.getFechaNacimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate fechaActual = LocalDate.now();
        Period edadPeriodo = Period.between(fechaNacimiento, fechaActual);
        int edadMeses = edadPeriodo.getYears() * 12 + edadPeriodo.getMonths();

        // 1. Prioridad: Objetivo de peso (si no es "MANTENER")
        if (mascota.getObjetivoPeso() != null) {
            switch (mascota.getObjetivoPeso().toUpperCase()) {
                case "BAJAR":
                    return rer * MER_FACTOR_WEIGHT_LOSS;
                case "SUBIR":
                    return rer * MER_FACTOR_WEIGHT_GAIN;
                // Si es "MANTENER", se continúa con la lógica de abajo
            }
        }

        // 2. Prioridad: Cachorros
        if (edadMeses <= 12) { // Consideramos cachorro hasta los 12 meses
            if (edadMeses <= 4) {
                merFactor = MER_FACTOR_PUPPY_0_4_MONTHS;
            } else {
                merFactor = MER_FACTOR_PUPPY_4_12_MONTHS;
            }
        }
        // 3. Prioridad: Estado Reproductor (para adultos)
        else if (mascota.getEstadoReproductor() != null) {
            switch (mascota.getEstadoReproductor().toUpperCase()) {
                case "GESTACION":
                    // Asumimos último tercio de gestación para el cálculo más alto
                    merFactor = MER_FACTOR_GESTATION_LAST_THIRD;
                    break;
                case "LACTANCIA":
                    // El factor de lactancia depende del número de cachorros
                    // Se establece un factor base y se añade por cada cachorro, con un límite
                    double baseLactationFactor = 3.0;
                    double factorPerPuppy = 0.5;
                    int numCachorros = (mascota.getNumCachorros() != null) ? mascota.getNumCachorros() : 1; // Mínimo 1 si no especificado
                    merFactor = baseLactationFactor + (factorPerPuppy * numCachorros);
                    // Limitar el factor de lactancia para evitar valores excesivamente altos
                    if (merFactor > 8.0) {
                        merFactor = 8.0;
                    }
                    break;
                case "CASTRADO":
                    merFactor = getAdultActivityFactor(mascota.getNivelActividad(), true);
                    break;
                case "ENTERO":
                    merFactor = getAdultActivityFactor(mascota.getNivelActividad(), false);
                    break;
                default:
                    // Si el estado reproductor no es ninguno de los anteriores, se usa la esterilización
                    merFactor = getAdultActivityFactor(mascota.getNivelActividad(), mascota.isEsterilizado());
                    break;
            }
        }
        // 4. Si no es cachorro y no hay estado reproductor específico, se usa la esterilización y actividad
        else {
            merFactor = getAdultActivityFactor(mascota.getNivelActividad(), mascota.isEsterilizado());
        }

        // Si la condición de salud no es "NORMAL", se podría considerar un ajuste,
        // pero por ahora, la dejamos sin impacto directo en el cálculo calórico.
        // if (mascota.getCondicionSalud() != null && !mascota.getCondicionSalud().equalsIgnoreCase("NORMAL")) {
        //     // Lógica para ajustar por condición de salud (ej: enfermedades que aumentan/disminuyen necesidades)
        // }

        return rer * merFactor;
    }

    /**
     * Obtiene el factor MER para un perro adulto basado en su nivel de actividad y estado de esterilización.
     *
     * @param nivelActividad El nivel de actividad de la mascota ("BAJO", "MODERADO", "ALTO").
     * @param esterilizado   True si la mascota está esterilizada/castrada, false en caso contrario.
     * @return El factor MER correspondiente.
     */
    private static double getAdultActivityFactor(String nivelActividad, boolean esterilizado) {
        if (nivelActividad == null) {
            nivelActividad = "MODERADO"; // Valor por defecto si no se especifica
        }

        if (esterilizado) {
            switch (nivelActividad.toUpperCase()) {
                case "BAJO":
                    return MER_FACTOR_ADULT_SPAYED_NEUTERED_LOW_ACTIVITY;
                case "MODERADO":
                    return MER_FACTOR_ADULT_SPAYED_NEUTERED_MODERATE_ACTIVITY;
                case "ALTO":
                    return MER_FACTOR_ADULT_SPAYED_NEUTERED_HIGH_ACTIVITY;
                default:
                    return MER_FACTOR_ADULT_SPAYED_NEUTERED_MODERATE_ACTIVITY; // Default
            }
        } else { // No esterilizado (entero)
            switch (nivelActividad.toUpperCase()) {
                case "BAJO":
                    return MER_FACTOR_ADULT_INTACT_LOW_ACTIVITY;
                case "MODERADO":
                    return MER_FACTOR_ADULT_INTACT_MODERATE_ACTIVITY;
                case "ALTO":
                    return MER_FACTOR_ADULT_INTACT_HIGH_ACTIVITY;
                default:
                    return MER_FACTOR_ADULT_INTACT_MODERATE_ACTIVITY; // Default
            }
        }
    }

    /**
     * Calcula la cantidad de alimento en gramos por día.
     *
     * @param caloriasDiarias Las calorías diarias recomendadas para la mascota.
     * @param kcalPor100g     Las kilocalorías por cada 100 gramos del alimento.
     * @return La cantidad de alimento en gramos por día.
     */
    public static double calcularGramosAlimento(double caloriasDiarias, Double kcalPor100g) {
        if (kcalPor100g == null || kcalPor100g <= 0) {
            // Manejar el caso donde no hay valor calórico o es inválido
            // Podríamos lanzar una excepción, devolver 0, o un valor por defecto.
            // Por ahora, devolveremos 0 para evitar divisiones por cero.
            System.err.println("Advertencia: Kcal por 100g de alimento no especificado o inválido. No se puede calcular la cantidad de alimento.");
            return 0.0;
        }
        // (Calorías diarias / Kcal por 100g) * 100g
        return (caloriasDiarias / kcalPor100g) * 100;
    }
}
