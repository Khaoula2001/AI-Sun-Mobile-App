package com.emsi.aisun.model;

import java.util.Date;

public class UVData {
    private double uvIndex;
    private double latitude;
    private double longitude;
    private double lightIntensity;
    private Date timestamp;
    private String riskLevel;
    private int skinType;

    public UVData(double uvIndex, double latitude, double longitude,
                  double lightIntensity, Date timestamp, String riskLevel, int skinType) {
        this.uvIndex = uvIndex;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lightIntensity = lightIntensity;
        this.timestamp = timestamp;
        this.riskLevel = riskLevel;
        this.skinType = skinType;
    }

    // Getters and Setters
    public double getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(double uvIndex) {
        this.uvIndex = uvIndex;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLightIntensity() {
        return lightIntensity;
    }

    public void setLightIntensity(double lightIntensity) {
        this.lightIntensity = lightIntensity;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getSkinType() {
        return skinType;
    }

    public void setSkinType(int skinType) {
        this.skinType = skinType;
    }
}
