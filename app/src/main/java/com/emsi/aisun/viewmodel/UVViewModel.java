package com.emsi.aisun.viewmodel;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.emsi.aisun.domain.LocationHelper;
import com.emsi.aisun.domain.UVCalculator;
import com.emsi.aisun.model.UVData;


public class UVViewModel extends AndroidViewModel {
    private UVCalculator uvCalculator;
    private LocationHelper locationHelper;
    private MutableLiveData<UVData> uvData = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    public UVViewModel(@NonNull Application application) {
        super(application);
        uvCalculator = new UVCalculator(application);
        locationHelper = new LocationHelper(application, this::onLocationUpdated);
    }

    public LiveData<UVData> getUVData() {
        return uvData;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void startMonitoring() {
        uvCalculator.startListening();
        locationHelper.startLocationUpdates();
    }

    public void stopMonitoring() {
        uvCalculator.stopListening();
        locationHelper.stopLocationUpdates();
    }

    public void setSkinType(int skinType) {
        uvCalculator.setSkinType(skinType);
    }

    private void onLocationUpdated(Location location) {
        uvCalculator.setCurrentLocation(location);
        UVData data = uvCalculator.calculateUVData();
        if (data != null) {
            uvData.postValue(data);
        } else {
            error.postValue("Unable to calculate UV data");
        }
    }
}