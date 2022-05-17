package hu.run4yourlife;

import static android.location.LocationManager.GPS_PROVIDER;
import static hu.run4yourlife.interfaces.StaticStuff.NOTIF_CHANNEL_ID;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import java.io.IOException;
import java.util.ArrayList;

import hu.run4yourlife.database.RunhistoryDB;
import hu.run4yourlife.database.RunningDatabase;
import hu.run4yourlife.interfaces.Challenge;
import hu.run4yourlife.interfaces.Challenges;
import hu.run4yourlife.interfaces.Speedtrap;
import hu.run4yourlife.interfaces.StaticStuff;

/**
 * Dani
 */
public class RunningService extends Service {

    public static final String ACTION_START = "START";
    public static final String ACTION_STOP = "STOP";

    public boolean TRACKING_STARTED = false;
    public long TRACKING_ID_MS = 0;
    LocationManager locationManager;
    LocationListener listener;

    public static int challengeID = -1;

    private double totalDistance = 0;

    public interface DataChangeCallback{
        void newData(double totalDistance, long timeElapsed, double lat, double lon, double accu);
    }

    public static DataChangeCallback callback = null;

    public static class GPSCoordinate {
        public double lat, lon, alt, accu;
        public long timestamp;

        public GPSCoordinate(double lat, double lon, double hei, double accu, long timestamp) {
            this.lat = lat;
            this.lon = lon;
            this.alt = hei;
            this.accu = accu;
            this.timestamp = timestamp;
        }
    }

    ArrayList<GPSCoordinate> runHistory = new ArrayList<>();


    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_START)) {
            if (!TRACKING_STARTED) {
                TRACKING_STARTED = true;
                challengeID = intent.getIntExtra(StaticStuff.RUN_EXTRA_ID_NAME,-1);
                startService();
            }
        } else if (intent.getAction().equals(ACTION_STOP)) {
            if (TRACKING_STARTED) {
                TRACKING_STARTED = false;
                stopRun();
            }
        }

        return START_NOT_STICKY;
    }

    private void startService() {
        totalDistance = 0;
        runHistory.clear();
        TRACKING_ID_MS = System.currentTimeMillis()/1000L;
        String notificationString = "Running...";
        if (challengeID != -1){
            try {
                Challenge ch = new Challenges(this).getChallengeDetailsForID(challengeID);
                notificationString = ch.getChallengeName();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, RunTrackerActivity.class), PendingIntent.FLAG_IMMUTABLE);


        Notification.Builder b = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            b = new Notification.Builder(this, NOTIF_CHANNEL_ID)
                    .setContentTitle(notificationString)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(contentIntent)
                    .setAutoCancel(false);
            Notification notification = b.build();
            startForeground(3, notification);
        }else{
            //TODO ??
        }





        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.getProvider(GPS_PROVIDER);


        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsStatus) {
            Toast.makeText(getApplicationContext(), "GPS Not enabled :(", Toast.LENGTH_SHORT).show();
        }

        Speedtrap speedtrap = new Speedtrap();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("RunningService","OnLocationChanged....");
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double alt = location.getAltitude();
                float accu = location.getAccuracy();
                long nou = System.currentTimeMillis();
                runHistory.add(new GPSCoordinate(lat, lon, alt, accu, nou));
                if(runHistory.size() > 1){
                    totalDistance += speedtrap.calcDist(runHistory.get(runHistory.size()-1),runHistory.get(runHistory.size()-2));
                    if(callback != null) callback.newData(totalDistance,TRACKING_ID_MS,lat,lon,accu);
                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.i("RunningService","OnStatusChanged....");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i("RunningService","OnProviderEnabled....");
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("RunningService","OnProviderDisabled....");
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("gps", 2000, 0, listener);//TODO change back...

    }

    private void stopRun(){
        locationManager.removeUpdates(listener);
        saveDataToDB();
        stopSelf();
    }
    private void saveDataToDB(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                RunningDatabase db = Room.databaseBuilder(getApplicationContext(), RunningDatabase.class, StaticStuff.RUNDB_NAME).build();
                RunhistoryDB newRun = new RunhistoryDB(challengeID,TRACKING_ID_MS,System.currentTimeMillis()/1000L,runHistory);
                db.myDataBase().insertRun(newRun);
                db.close();
            }
        }).start();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
