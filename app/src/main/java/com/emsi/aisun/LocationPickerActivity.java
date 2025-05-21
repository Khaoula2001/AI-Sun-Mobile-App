package com.emsi.aisun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationPickerActivity extends AppCompatActivity {

    private EditText etAddress, etLatitude, etLongitude;
    private Button btnSearch, btnSave, btnOpenMap;
    private List<Map<String, String>> savedLocations = new ArrayList<>();
    public static final String KEY_LOCATION = "current_location";
    public static final String KEY_SAVED_LOCATIONS = "saved_locations";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        etAddress = findViewById(R.id.etAddress);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnSearch = findViewById(R.id.btnSearch);
        btnSave = findViewById(R.id.btnSave);
        btnOpenMap = findViewById(R.id.btnOpenMap);

        loadSavedLocations();

        btnSearch.setOnClickListener(v -> {
            String address = etAddress.getText().toString();
            if (!address.isEmpty()) {
                Geocoder geocoder = new Geocoder(this);
                try {
                    List<Address> results = geocoder.getFromLocationName(address, 1);
                    if (results != null && !results.isEmpty()) {
                        Address location = results.get(0);
                        etLatitude.setText(String.valueOf(location.getLatitude()));
                        etLongitude.setText(String.valueOf(location.getLongitude()));
                        Toast.makeText(this, "Adresse trouvée!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Adresse introuvable.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(this, "Erreur de géocodage.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSave.setOnClickListener(v -> {
            String name = etAddress.getText().toString();
            String lat = etLatitude.getText().toString();
            String lng = etLongitude.getText().toString();

            if (!name.isEmpty() && !lat.isEmpty() && !lng.isEmpty()) {
                saveLocation(name, lat, lng);
                Toast.makeText(this, "Lieu sauvegardé!", Toast.LENGTH_SHORT).show();
            }
        });

        btnOpenMap.setOnClickListener(v -> {
            // Ouvrir l'activité de sélection sur carte
            Intent intent = new Intent(this, MapSelectionActivity.class);
            startActivityForResult(intent, 1);
        });
    }

    private void saveLocation(String name, String lat, String lng) {
        Map<String, String> location = new HashMap<>();
        location.put("name", name);
        location.put("lat", lat);
        location.put("lng", lng);

        savedLocations.add(location);

        SharedPreferences prefs = getSharedPreferences("AI_Sun_Prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Sauvegarder la localisation actuelle
        editor.putString(KEY_LOCATION, new Gson().toJson(location));

        // Sauvegarder la liste des lieux
        editor.putString(KEY_SAVED_LOCATIONS, new Gson().toJson(savedLocations));
        editor.apply();
    }

    private void loadSavedLocations() {
        SharedPreferences prefs = getSharedPreferences("AI_Sun_Prefs", MODE_PRIVATE);
        String json = prefs.getString(KEY_SAVED_LOCATIONS, null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<Map<String, String>>>() {
            }.getType();
            savedLocations = new Gson().fromJson(json, type);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            double lat = data.getDoubleExtra("latitude", 0);
            double lng = data.getDoubleExtra("longitude", 0);
            String address = data.getStringExtra("address");

            etAddress.setText(address);
            etLatitude.setText(String.valueOf(lat));
            etLongitude.setText(String.valueOf(lng));
        }
    }

    public void onSelectSavedLocation(View view) {
        if (!savedLocations.isEmpty()) {
            // Extraire les noms des lieux
            String[] names = new String[savedLocations.size()];
            for (int i = 0; i < savedLocations.size(); i++) {
                names[i] = savedLocations.get(i).get("name");
            }

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Choisir un lieu sauvegardé")
                    .setItems(names, (dialog, which) -> {
                        Map<String, String> selected = savedLocations.get(which);
                        etAddress.setText(selected.get("name"));
                        etLatitude.setText(selected.get("lat"));
                        etLongitude.setText(selected.get("lng"));
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        } else {
            Toast.makeText(this, "Aucun lieu sauvegardé.", Toast.LENGTH_SHORT).show();
        }

    }

    public void onConfirmLocation(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("latitude", Double.parseDouble(etLatitude.getText().toString()));
        resultIntent.putExtra("longitude", Double.parseDouble(etLongitude.getText().toString()));
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}