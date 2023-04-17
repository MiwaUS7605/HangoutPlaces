package com.groupb.locationsharing.Service.GetLocation;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

class GetLocation extends Service{
    String GPS_FILTER = "matos.action.GPSFIX";
    Thread serviceThread;
    LocationManager lm;
    GPSListener myLocationListener;
    @Override
    public void onStart(Intent intent, int startId) {
        serviceThread = new Thread(new Runnable() {
            public void run() {
                getGPSFix_Version2();
            }
        });
        serviceThread.start();
    }
    public void getGPSFix_Version2() {
        try {
            Looper.prepare();
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            long minTime = 2000;
            float minDistance = 0;

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, myLocationListener);
            Looper.loop();
        }
        catch (Exception e) { e.printStackTrace(); }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude(), longitude = location.getLongitude();
            Intent myFilteredResponse = new Intent(GPS_FILTER);
            myFilteredResponse.putExtra("latitude", latitude);
            myFilteredResponse.putExtra("longitude", longitude);
            myFilteredResponse.putExtra("provider", location.getProvider());

            sendBroadcast(myFilteredResponse);
        }
        public void onProviderDisabled(String provider) { }
        public void onProviderEnabled(String provider) { }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    }
}