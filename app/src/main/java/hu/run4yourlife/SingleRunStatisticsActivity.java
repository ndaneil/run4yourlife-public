package hu.run4yourlife;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;
import org.osmdroid.views.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.IOException;
import java.util.ArrayList;

import hu.run4yourlife.database.RunhistoryDB;
import hu.run4yourlife.database.RunningDatabase;
import hu.run4yourlife.interfaces.Challenge;
import hu.run4yourlife.interfaces.Challenges;
import hu.run4yourlife.interfaces.Speedtrap;
import hu.run4yourlife.interfaces.StaticStuff;

/**
 * Levi
 */
public class SingleRunStatisticsActivity extends AppCompatActivity {
    RunningDatabase db;
    ArrayList<RunhistoryDB> runs = new ArrayList<>();
    RunhistoryDB currentRun;
    int selectedRunId;
    Speedtrap sp = new Speedtrap();
    MapView map;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_run_statistics);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                selectedRunId = getIntent().getIntExtra("runId", -1);
                db = Room.databaseBuilder(getApplicationContext(), RunningDatabase.class, StaticStuff.RUNDB_NAME).build();
                runs = (ArrayList<RunhistoryDB>) db.myDataBase().getUsers();
                db.close();
                for(RunhistoryDB i : runs){
                    if(i.getId()==selectedRunId){
                        currentRun = i;
                        break;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(map != null) {
                            map.setMultiTouchControls(true);
                            IMapController mapController = map.getController();
                            mapController.setZoom(20);
                            RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(SingleRunStatisticsActivity.this, map);
                            mRotationGestureOverlay.setEnabled(true);
                            map.setMultiTouchControls(true);
                            map.getOverlays().add(mRotationGestureOverlay);
                            Polyline line = new Polyline();
                            line.setWidth(20f);
                            ArrayList<GeoPoint> pts = new ArrayList<>();

                            try {
                                Challenge ch = new Challenges(SingleRunStatisticsActivity.this).getChallengeDetailsForID(currentRun.getChallengeID());
                                int currIdx = 0;
                                double distRequired = ch.getDistances().get(0);
                                RunningService.GPSCoordinate last = null;
                                for (RunningService.GPSCoordinate coord : currentRun.getGpsdata()) {
                                    GeoPoint point = new GeoPoint(coord.lat, coord.lon);
                                    pts.add(point);
                                }
                                line.setPoints(pts);
                                line.setGeodesic(true);
                                map.getOverlayManager().add(line);
                                mapController.setCenter(pts.get(0));
                                for (RunningService.GPSCoordinate coord : currentRun.getGpsdata()) {

                                    GeoPoint point = new GeoPoint(coord.lat, coord.lon);

                                    if(currIdx == 0 && last == null){
                                        Marker m = new Marker(map);
                                        m.setPosition(point);
                                        m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                        m.setTitle(ch.getStops().get(currIdx));
                                        map.getOverlays().add(m);
                                    }else{
                                        distRequired -= sp.calcDist(last,coord)*1000.0;
                                        Log.i("SingleRunStatistics","DistRequired:" + distRequired + " currentIdx:" + currIdx);
                                        if (distRequired < 0 && currIdx + 1 < ch.getStops().size()){
                                            currIdx++;
                                            distRequired += ch.getDistances().get(currIdx);
                                            Marker m = new Marker(map);
                                            m.setPosition(point);
                                            m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                            m.setTitle(ch.getStops().get(currIdx));
                                            map.getOverlays().add(m);
                                        }


                                    }
                                    last = coord;

                                    //pts.add(point);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        }else{
                            Log.e("Custom Error","Map was null");
                        }
                    }
                });
            }
        });
        myThread.start();





    }
}