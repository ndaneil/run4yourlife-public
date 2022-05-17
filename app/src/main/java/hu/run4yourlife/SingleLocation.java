package hu.run4yourlife;

import static android.location.LocationManager.GPS_PROVIDER;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;

import hu.run4yourlife.interfaces.Speedtrap;

/**
 * Dani
 */
public class SingleLocation {
    public interface LocationReceivedCallback{
        void onLocationReceived(double lat, double lon, double hei, double accu);
    }

    Context ctx;
    LocationManager locationManager;
    LocationListener listener;
    ArrayList<LocationReceivedCallback> callback;
    public SingleLocation(Context c, LocationReceivedCallback cb){
        this.ctx = c;
        this.callback = new ArrayList<>();
        this.callback.add(cb);
    }
    public void registerCallbacK(LocationReceivedCallback cb){
        callback.add(cb);
    }

    public void initLocationRequest(){
        locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
        locationManager.getProvider(GPS_PROVIDER);


        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsStatus) {
            Toast.makeText(ctx.getApplicationContext(), "GPS Not enabled :(", Toast.LENGTH_SHORT).show();
        }

        Speedtrap speedtrap = new Speedtrap();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double alt = location.getAltitude();
                float accu = location.getAccuracy();
                long nou = System.currentTimeMillis();
                locationManager.removeUpdates(this);
                if(callback!=null){
                    for (LocationReceivedCallback cb : callback)
                    cb.onLocationReceived(lat,lon,alt,accu);
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("gps", 500, 0, listener);

    }
}
