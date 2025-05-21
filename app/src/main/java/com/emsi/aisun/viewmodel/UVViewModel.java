package com.emsi.aisun.viewmodel;

import android.app.Application;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emsi.aisun.model.UVData;
import com.emsi.aisun.domain.UVCalculator;

import java.util.Calendar;

public class UVViewModel extends AndroidViewModel {
    private MutableLiveData<UVData> uvData = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    private int currentSkinType = 2; // Default: Type II
    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private float currentLightLevel = 0;

    public UVViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<UVData> getUVData() {
        return uvData;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void startMonitoring() {
        // Implémentez votre logique de monitoring ici
        calculateAndPostUVData();
    }

    public void stopMonitoring() {
        // Implémentez votre logique d'arrêt ici
    }

    public void setSkinType(int skinType) {
        this.currentSkinType = skinType;
        calculateAndPostUVData();
    }

    public void updateLocation(Location location) {
        if (location != null) {
            this.currentLatitude = location.getLatitude();
            this.currentLongitude = location.getLongitude();
            calculateAndPostUVData();
        } else {
            error.postValue("Invalid location");
        }
    }

    public void updateLightLevel(float lux) {
        this.currentLightLevel = lux;
        calculateAndPostUVData();
    }

    private void calculateAndPostUVData() {
        try {
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            // Calcul de l'UV index
            double uvIndex = UVCalculator.calculateUV(
                    currentLightLevel,
                    currentHour,
                    currentLatitude
            );

            // Création de l'objet UVData
            UVData data = new UVData(
                    currentLatitude,
                    currentLongitude,
                    uvIndex,
                    currentLightLevel,
                    new java.util.Date(),
                    getRiskDescription(uvIndex, currentSkinType),
                    calculateRiskPercentage(uvIndex, currentSkinType)
            );

            uvData.postValue(data);

        } catch (Exception e) {
            Log.e("UVViewModel", "Error calculating UV data", e);
            error.postValue("Calculation error: " + e.getMessage());
        }
    }

    private int calculateRiskPercentage(double uvIndex, int skinType) {
        double baseRisk = uvIndex * 2.5;
        double skinFactor = getSkinTypeFactor(skinType);
        return (int) Math.min(100, Math.round(baseRisk * skinFactor));
    }


    private double getSkinTypeFactor(int skinType) {
        switch (skinType) {
            case 1:
                return 2.0;
            case 2:
                return 1.5;
            case 3:
                return 1.2;
            case 4:
                return 1.0;
            case 5:
                return 0.8;
            case 6:
                return 0.6;
            default:
                return 1.0;
        }
    }

    private String getRiskDescription(double uvIndex, int skinType) {
        double adjustedUV = uvIndex * (1.0 + (6 - skinType) * 0.2);

        if (adjustedUV < 3) return "Low risk";
        if (adjustedUV < 6) return "Moderate risk";
        if (adjustedUV < 8) return "High risk";
        if (adjustedUV < 11) return "Very high risk";
        return "Extreme risk";
    }
}