package com.emsi.aisun;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.emsi.aisun.model.UVData;
import com.emsi.aisun.viewmodel.UVViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private UVViewModel viewModel;
    private LineChart uvChart;
    private FloatingActionButton fab;
    private RadioGroup skinTypeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(UVViewModel.class);
        // Initialize UI components
        uvChart = findViewById(R.id.uvChart);
        fab = findViewById(R.id.fab);
        skinTypeGroup = findViewById(R.id.skinTypeGroup);

        // Setup chart
        setupUVChart();

        // Observe UV data changes
        viewModel.getUVData().observe(this, this::updateUI);

        // Observe errors
        viewModel.getError().observe(this, error -> {
            // Handle errors (e.g., show a toast)
        });

        // Set default skin type (Type II)
        skinTypeGroup.check(R.id.skinType2);

        // Skin type selection listener
        skinTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int skinType = 2; // default
            if (checkedId == R.id.skinType1) skinType = 1;
            else if (checkedId == R.id.skinType2) skinType = 2;
            else if (checkedId == R.id.skinType3) skinType = 3;
            else if (checkedId == R.id.skinType4) skinType = 4;
            else if (checkedId == R.id.skinType5) skinType = 5;
            else if (checkedId == R.id.skinType6) skinType = 6;

            viewModel.setSkinType(skinType);
        });

        // FAB click listener
        fab.setOnClickListener(v -> {
            viewModel.startMonitoring();
            generateForecastData();
        });
    }

    private void setupUVChart() {
        uvChart.getDescription().setEnabled(false);
        uvChart.setTouchEnabled(true);
        uvChart.setDragEnabled(true);
        uvChart.setScaleEnabled(true);
        uvChart.setPinchZoom(true);
        uvChart.setDrawGridBackground(false);
        uvChart.setBackgroundColor(Color.WHITE);

        // X-axis setup
        XAxis xAxis = uvChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getHoursLabels()));

        // Y-axis setup
        YAxis leftAxis = uvChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(12f);
        leftAxis.setGranularity(1f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = uvChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Disable legend
        uvChart.getLegend().setEnabled(false);
    }

    private String[] getHoursLabels() {
        String[] labels = new String[24];
        for (int i = 0; i < 24; i++) {
            labels[i] = String.format(Locale.getDefault(), "%02d:00", i);
        }
        return labels;
    }

    private void generateForecastData() {
        // This would be replaced with actual forecast data from your model
        // For now, we'll generate some sample data
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            // Simulate UV index that peaks at noon
            float value = (float) (5 * Math.sin(Math.PI * (i - 6) / 12) + 2);
            entries.add(new Entry(i, value));
        }

        LineDataSet dataSet = new LineDataSet(entries, "UV Index");
        dataSet.setColor(Color.RED);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        uvChart.setData(lineData);
        uvChart.invalidate();
    }

    private void updateUI(UVData uvData) {
        if (uvData == null) return;

        // Update UV index display
        TextView uvIndexValue = findViewById(R.id.uvIndexValue);
        uvIndexValue.setText(String.format(Locale.getDefault(), "%.1f", uvData.getUvIndex()));

        // Update risk level
        TextView uvRiskLevel = findViewById(R.id.uvRiskLevel);
        uvRiskLevel.setText(String.format("Risk: %s", uvData.getRiskLevel()));

        // Update recommendation
        TextView uvRecommendation = findViewById(R.id.uvRecommendation);
        uvRecommendation.setText(getRecommendation(uvData.getUvIndex(), uvData.getSkinType()));

        // Update location info
        TextView latitudeValue = findViewById(R.id.latitudeValue);
        latitudeValue.setText(String.format(Locale.getDefault(), "%.4f", uvData.getLatitude()));

        TextView longitudeValue = findViewById(R.id.longitudeValue);
        longitudeValue.setText(String.format(Locale.getDefault(), "%.4f", uvData.getLongitude()));

        TextView lightValue = findViewById(R.id.lightValue);
        lightValue.setText(String.format(Locale.getDefault(), "%.1f lux", uvData.getLightIntensity()));

        // Update chart with real-time data
        updateChartWithRealTimeData(uvData);
    }

    private String getRecommendation(double uvIndex, int skinType) {
        double adjustedUV = uvIndex * (1.0 + (6 - skinType) * 0.2);

        if (adjustedUV < 3) {
            return "Recommendation: Sun protection not needed";
        } else if (adjustedUV < 6) {
            return "Recommendation: Wear sunscreen SPF 15+";
        } else if (adjustedUV < 8) {
            return "Recommendation: Wear sunscreen SPF 30+, hat and sunglasses";
        } else if (adjustedUV < 11) {
            return "Recommendation: Extra protection needed - avoid sun 10am-4pm";
        } else {
            return "Recommendation: Avoid sun exposure - very high risk";
        }
    }

    private void updateChartWithRealTimeData(UVData uvData) {
        // Get current hour
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

        // Get existing data or create new
        LineData lineData = uvChart.getData();
        if (lineData == null) return;

        LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0);
        if (dataSet == null) return;

        // Update the current hour's value
        dataSet.getEntryForIndex(currentHour).setY((float) uvData.getUvIndex());
        lineData.notifyDataChanged();
        uvChart.notifyDataSetChanged();
        uvChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.startMonitoring();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.stopMonitoring();
    }
}