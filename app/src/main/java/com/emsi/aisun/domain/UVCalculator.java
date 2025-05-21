package com.emsi.aisun.domain;

import java.lang.Math;

public class UVCalculator {
    // Coefficients pour le calcul UV basé sur lux + heure + localisation
    private static final double LUX_FACTOR = 0.0001; // Ajustez ce facteur selon vos besoins
    private static final double BASE_UV = 0.5;
    private static final double NOON_UV_BOOST = 5.0;

    public static double calculateUV(float lux, int hour, double latitude) {
        // 1. Composante horaire (pic à midi)
        double hourComponent = NOON_UV_BOOST * Math.exp(-0.5 * Math.pow((hour - 12) / 4.0, 2));

        // 2. Composante de luminosité
        double luxComponent = lux * LUX_FACTOR;

        // 3. Composante géographique (plus fort UV près de l'équateur)
        double latComponent = 1 + (1 - Math.abs(latitude) / 90);

        // Combinaison des composantes
        double uvValue = (BASE_UV + hourComponent * latComponent + luxComponent);

        // Ajustements finaux
        if (hour >= 22 || hour <= 5) {
            uvValue *= 0.2; // Nuit
        } else if ((hour >= 6 && hour <= 9) || (hour >= 15 && hour <= 18)) {
            uvValue *= 0.7; // Matin/Soir
        }
        // else -> garde la valeur originale

        // Limite entre 0.1 et 12.0
        return Math.min(Math.max(uvValue, 0.1), 12.0);
    }
}