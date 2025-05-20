package com.emsi.aisun.domain;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import com.emsi.aisun.model.UVData;

import java.util.Calendar;
import java.util.Date;

public class UVCalculator implements SensorEventListener {
    private static final double BASE_UV = 0.5;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private double currentLightIntensity = 0;
    private Location currentLocation;
    private int skinType = 2; // Default skin type (Fitzpatrick scale)

    public UVCalculator(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
    }

    public void startListening() {
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    public void setSkinType(int skinType) {
        this.skinType = skinType;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLightIntensity = event.values[0];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes if needed
    }

    public UVData calculateUVData() {
        if (currentLocation == null) {
            return null;
        }

        // Get current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        Date now = new Date();

        // Calculate UV index based on light, time and location
        double uvIndex = calculateUVIndex(hour, currentLocation.getLatitude(),
                currentLocation.getLongitude(), currentLightIntensity);

        // Determine risk level
        String riskLevel = determineRiskLevel(uvIndex, skinType);

        return new UVData(uvIndex, currentLocation.getLatitude(),
                currentLocation.getLongitude(), currentLightIntensity, now, riskLevel, skinType);
    }

    private double calculateUVIndex(int hour, double latitude, double longitude, double lightIntensity) {
        // Normalize light intensity (assuming max is ~120000 lux in direct sunlight)
        double normalizedLight = Math.min(lightIntensity / 120000.0, 1.0);

        // Time factor (UV peaks around noon)
        double timeFactor = Math.cos(Math.toRadians((hour - 12) * 15));
        timeFactor = Math.max(timeFactor, 0); // No negative values

        // Latitude factor (higher UV near equator)
        double latitudeFactor = 1 - Math.abs(latitude) / 90.0;

        // Combine factors with base UV
        double uvIndex = BASE_UV + (normalizedLight * timeFactor * latitudeFactor * 10);

        // Cap at realistic maximum
        return Math.min(uvIndex, 12);
    }

    private String determineRiskLevel(double uvIndex, int skinType) {
        // Adjust threshold based on skin type (1-6 Fitzpatrick scale)
        double sensitivityFactor = 1.0 + (6 - skinType) * 0.2;

        if (uvIndex * sensitivityFactor < 3) {
            return "Low";
        } else if (uvIndex * sensitivityFactor < 6) {
            return "Moderate";
        } else if (uvIndex * sensitivityFactor < 8) {
            return "High";
        } else if (uvIndex * sensitivityFactor < 11) {
            return "Very High";
        } else {
            return "Extreme";
        }
    }
}