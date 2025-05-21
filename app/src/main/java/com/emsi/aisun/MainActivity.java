package com.emsi.aisun;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.emsi.aisun.domain.UVCalculator;
import com.emsi.aisun.model.UVData;
import com.emsi.aisun.viewmodel.UVViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int REQUEST_LOCATION_PICKER = 1;
    private LineChart uvChart;
    private FloatingActionButton fab;
    private RadioGroup skinTypeGroup;
    private ProgressBar riskProgressBar;
    private TextView riskPercentage, riskDescription;
    private TextView uvIndexValue, uvRiskLevel, uvExposureTime, uvRecommendation;
    private TextView locationText, lightValue;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private FusedLocationProviderClient fusedLocationClient;

    private double currentLatitude = 0;
    private double currentLongitude = 0;
    private float currentLightLevel = 0;
    private int currentSkinType = 2; // Default to type II
    private UVViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UVViewModel.class);
        viewModel.updateLightLevel(currentLightLevel); // Quand le capteur de lumière change
        viewModel.setSkinType(currentSkinType); // Quand le skin type change
        // Initialize UI components
        initViews();
        setupUVChart();

        // Initialize sensors and location
        initSensorsAndLocation();

        // Set default skin type
        skinTypeGroup.check(R.id.skinType2);

        // Setup listeners
        setupListeners();

        // Generate initial forecast
        generateUVForecast();

        // Observe ViewModel data
        observeViewModel();
    }

    private void initViews() {
        uvChart = findViewById(R.id.uvChart);
        fab = findViewById(R.id.fab);
        skinTypeGroup = findViewById(R.id.skinTypeGroup);
        riskProgressBar = findViewById(R.id.riskProgressBar);
        riskPercentage = findViewById(R.id.riskPercentage);
        riskDescription = findViewById(R.id.riskDescription);
        uvIndexValue = findViewById(R.id.uvIndexValue);
        uvRiskLevel = findViewById(R.id.uvRiskLevel);
        uvExposureTime = findViewById(R.id.uvExposureTime);
        uvRecommendation = findViewById(R.id.uvRecommendation);
        locationText = findViewById(R.id.locationText);
        lightValue = findViewById(R.id.lightValue);
    }

    private void initSensorsAndLocation() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkPermissions();
    }

    private void setupListeners() {
        skinTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.skinType1) currentSkinType = 1;
            else if (checkedId == R.id.skinType2) currentSkinType = 2;
            else if (checkedId == R.id.skinType3) currentSkinType = 3;
            else if (checkedId == R.id.skinType4) currentSkinType = 4;
            else if (checkedId == R.id.skinType5) currentSkinType = 5;
            else if (checkedId == R.id.skinType6) currentSkinType = 6;

            viewModel.setSkinType(currentSkinType);
            updateRiskAssessment();
        });

        fab.setOnClickListener(v -> refreshData());

        Button changeLocationBtn = findViewById(R.id.changeLocationBtn);
        // Modifiez le onClickListener du bouton "CHANGE LOCATION"
        changeLocationBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, LocationPickerActivity.class);
            startActivityForResult(intent, REQUEST_LOCATION_PICKER);
        });
    }

    private void observeViewModel() {
        // Observer les changements
        viewModel.getUVData().observe(this, uvData -> {
            if (uvData != null) {
                // Mettre à jour l'UI avec les nouvelles données
                uvIndexValue.setText(String.format(Locale.getDefault(), "%.1f", uvData.getUvIndex()));
                updateUI(uvData);
            }
        });


        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission required for accurate UV predictions",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        updateLocationUI(location);
                        viewModel.startMonitoring();
                    }
                });
    }

    private void updateLocationUI(Location location) {
        locationText.setText(String.format(Locale.getDefault(),
                "Location: %.4f, %.4f", location.getLatitude(), location.getLongitude()));
    }

    private void setupUVChart() {
        uvChart.getDescription().setEnabled(false);
        uvChart.setTouchEnabled(true);
        uvChart.setDragEnabled(true);
        uvChart.setScaleEnabled(true);
        uvChart.setPinchZoom(true);
        uvChart.setDrawGridBackground(false);
        uvChart.setBackgroundColor(Color.WHITE);

        XAxis xAxis = uvChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getHoursLabels()));

        YAxis leftAxis = uvChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(12f);
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);

        uvChart.getAxisRight().setEnabled(false);
        uvChart.getLegend().setEnabled(false);
    }

    private String[] getHoursLabels() {
        String[] labels = new String[24];
        for (int i = 0; i < 24; i++) {
            labels[i] = String.format(Locale.getDefault(), "%02d:00", i);
        }
        return labels;
    }

    private float calculateUVIndex(int hour, double latitude, double longitude) {
        double uvValue = UVCalculator.calculateUV(
                currentLightLevel,
                hour,
                latitude
        );

        Log.d("UV_CALC", String.format(Locale.US,
                "Hour: %d, Lat: %.2f, Lux: %.1f -> UV: %.2f",
                hour, latitude, currentLightLevel, uvValue));

        return (float) uvValue;
    }

    private double calculateSolarDeclination() {
        // More precise solar declination calculation
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        double B = (360.0 / 365.0) * (dayOfYear - 81);
        return 23.45 * Math.sin(Math.toRadians(B));
    }

    private double calculateHourAngle(int hour, double longitude) {
        // Solar hour angle with equation of time correction
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        double B = (360.0 / 365.0) * (dayOfYear - 81);
        double equationOfTime = 9.87 * Math.sin(2 * Math.toRadians(B))
                - 7.53 * Math.cos(Math.toRadians(B))
                - 1.5 * Math.sin(Math.toRadians(B));

        // True solar time
        double solarTime = hour + (longitude / 15.0) + (equationOfTime / 60.0);
        return 15 * (solarTime - 12);
    }

    private void generateUVForecast() {
        List<Entry> entries = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        for (int i = 0; i < 24; i++) {
            float uvValue = calculateUVIndex(i, currentLatitude, currentLongitude);
            entries.add(new Entry(i, uvValue));
            Log.d("UV_FORECAST", "Hour: " + i + " - UV: " + uvValue); // Ajout pour le débogage
        }

        LineDataSet dataSet = new LineDataSet(entries, "UV Index");
        dataSet.setColor(Color.parseColor("#FF5722"));
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        uvChart.setData(new LineData(dataSet));
        uvChart.invalidate();

        if (entries.size() > currentHour) {
            float currentUV = entries.get(currentHour).getY();
            Log.d("UV_CURRENT", "Current UV: " + currentUV); // Ajout pour le débogage
            updateCurrentUVDisplay(currentUV);
        }
    }

    private double calculateSolarAltitude(double latitude, double declination, double hourAngle) {
        // Solar altitude in degrees
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);
        double haRad = Math.toRadians(hourAngle);

        return Math.toDegrees(Math.asin(
                Math.sin(latRad) * Math.sin(decRad) +
                        Math.cos(latRad) * Math.cos(decRad) * Math.cos(haRad)
        ));
    }


    private String getRiskLevel(float uvIndex) {
        double adjustedUV = uvIndex * (1.0 + (6 - currentSkinType) * 0.2);

        if (adjustedUV < 3) return "Low";
        if (adjustedUV < 6) return "Moderate";
        if (adjustedUV < 8) return "High";
        if (adjustedUV < 11) return "Very High";
        return "Extreme";
    }

    private String calculateSafeExposureTime(float uvIndex, int skinType) {
        if (uvIndex < 1) return "Unlimited";

        // Base exposure time (minutes) for skin type II at UV 5
        float baseTime = 30f;

        // Skin type adjustments
        float skinFactor;
        switch (skinType) {
            case 1:
                skinFactor = 0.5f;
                break;
            case 2:
                skinFactor = 1.0f;
                break;
            case 3:
                skinFactor = 1.5f;
                break;
            case 4:
                skinFactor = 2.0f;
                break;
            case 5:
                skinFactor = 3.0f;
                break;
            case 6:
                skinFactor = 4.0f;
                break;
            default:
                skinFactor = 1.0f;
        }

        // UV adjustment
        float uvFactor = 5.0f / uvIndex;

        float safeMinutes = baseTime * skinFactor * uvFactor;

        if (safeMinutes >= 120) return "2+ hours";
        return Math.round(safeMinutes) + " minutes";
    }

    private String getRecommendation(float uvIndex, int skinType) {
        double adjustedUV = uvIndex * (1.0 + (6 - skinType) * 0.2);

        if (adjustedUV < 3) return "Sun protection not needed";
        if (adjustedUV < 6) return "Wear sunscreen SPF 15+";
        if (adjustedUV < 8) return "Wear sunscreen SPF 30+, hat and sunglasses";
        if (adjustedUV < 11) return "Extra protection needed - avoid sun 10am-4pm";
        return "Avoid sun exposure - very high risk";
    }

    private void updateRiskAssessment() {
        try {
            float uvIndex = Float.parseFloat(uvIndexValue.getText().toString());
            float baseRisk = uvIndex * 2.5f;
            float skinFactor = 1.0f;

            switch (currentSkinType) {
                case 1:
                    skinFactor = 2.0f;
                    break;
                case 2:
                    skinFactor = 1.5f;
                    break;
                case 3:
                    skinFactor = 1.2f;
                    break;
                case 4:
                    skinFactor = 1.0f;
                    break;
                case 5:
                    skinFactor = 0.8f;
                    break;
                case 6:
                    skinFactor = 0.6f;
                    break;
            }

            int riskPercent = Math.min(100, Math.round(baseRisk * skinFactor));

            riskProgressBar.setProgress(riskPercent);
            riskPercentage.setText(riskPercent + "%");

            if (riskPercent < 20) {
                riskDescription.setText("Low risk - minimal precautions needed");
                riskProgressBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.safeGreen));
            } else if (riskPercent < 50) {
                riskDescription.setText("Moderate risk - take standard precautions");
                riskProgressBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.cautionYellow));
            } else if (riskPercent < 80) {
                riskDescription.setText("High risk - extra protection recommended");
                riskProgressBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.warningOrange));
            } else {
                riskDescription.setText("Very high risk - limit sun exposure");
                riskProgressBar.setProgressTintList(ContextCompat.getColorStateList(this, R.color.dangerRed));
            }
        } catch (NumberFormatException e) {
            Log.e("AI-Sun", "Error parsing UV index", e);
        }
    }

    private void refreshData() {
        getCurrentLocation();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        generateUVForecast();
    }

    private void updateUI(UVData uvData) {
        if (uvData == null) return;

        currentLatitude = uvData.getLatitude();
        currentLongitude = uvData.getLongitude();
        currentLightLevel = (float) uvData.getLightIntensity();

        updateLocationUI(new Location("") {{
            setLatitude(currentLatitude);
            setLongitude(currentLongitude);
        }});

        lightValue.setText(String.format(Locale.getDefault(), "%.1f lux", currentLightLevel));
        updateCurrentUVDisplay((float) uvData.getUvIndex());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        viewModel.startMonitoring();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        viewModel.stopMonitoring();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLightLevel = event.values[0];
            lightValue.setText(String.format(Locale.getDefault(), "%.1f lux", currentLightLevel));
            generateUVForecast();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }


    // Ajoutez cette méthode pour gérer le résultat
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOCATION_PICKER && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("latitude", 0);
            double lng = data.getDoubleExtra("longitude", 0);

            currentLatitude = lat;
            currentLongitude = lng;

            // Mettez à jour l'interface
            locationText.setText(String.format(Locale.getDefault(),
                    "Location: %.4f, %.4f", lat, lng));

            // Regénérez les données UV
            generateUVForecast();
        }
    }

    private void updateCurrentUVDisplay(float uvIndex) {
        Log.d("UV_DISPLAY", "Updating UI with UV: " + uvIndex);
        uvIndexValue.setText(String.format(Locale.getDefault(), "%.1f", uvIndex));
        uvRiskLevel.setText(String.format("Risk Level: %s", getRiskLevel(uvIndex)));
        uvExposureTime.setText(String.format("Safe Exposure: %s",
                calculateSafeExposureTime(uvIndex, currentSkinType)));
        uvRecommendation.setText(String.format("Recommendation: %s",
                getRecommendation(uvIndex, currentSkinType)));

        updateRiskAssessment();
    }

}