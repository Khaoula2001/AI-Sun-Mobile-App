package com.emsi.aisun.domain;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.app.ActivityCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class LocationHelper {
    private LocationManager locationManager;
    private LocationListener locationListener;
    private LocationUpdateCallback callback;
    private Context context;

    public interface LocationUpdateCallback {
        void onLocationUpdated(Location location);
    }

    public LocationHelper(Context context, LocationUpdateCallback callback) {
        this.context = context;
        this.callback = callback;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void startLocationUpdates() {
        Dexter.withContext(context)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(context,
                                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                            locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    callback.onLocationUpdated(location);
                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {}

                                @Override
                                public void onProviderEnabled(String provider) {}

                                @Override
                                public void onProviderDisabled(String provider) {}
                            };

                            locationManager.requestLocationUpdates(
                                    LocationManager.GPS_PROVIDER,
                                    5000,
                                    10,
                                    locationListener,
                                    Looper.getMainLooper());
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        ((Activity)context).finish();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    public void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}