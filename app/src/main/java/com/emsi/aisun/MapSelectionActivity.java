package com.emsi.aisun;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapSelectionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng selectedLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_selection);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.btnConfirmLocation).setOnClickListener(v -> {
            if (selectedLocation != null) {
                Intent result = new Intent();
                result.putExtra("latitude", selectedLocation.latitude);
                result.putExtra("longitude", selectedLocation.longitude);
                result.putExtra("address", "Position sélectionnée");
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Position initiale (Paris)
        LatLng paris = new LatLng(48.8566, 2.3522);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(paris, 10));

        mMap.setOnMapClickListener(latLng -> {
            selectedLocation = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
        });
    }
}