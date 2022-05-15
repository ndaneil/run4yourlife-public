package hu.run4yourlife;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import hu.run4yourlife.database.RunhistoryDB;
import hu.run4yourlife.database.RunningDatabase;
import hu.run4yourlife.interfaces.Speedtrap;
import hu.run4yourlife.interfaces.StaticStuff;

public class SingleRunStatisticsActivity extends AppCompatActivity {
    Context ctx;
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
        Thread myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                selectedRunId = getIntent().getIntExtra("runId", -1);
                db = Room.databaseBuilder(ctx.getApplicationContext(), RunningDatabase.class, StaticStuff.RUNDB_NAME).build();
                runs = (ArrayList<RunhistoryDB>) db.myDataBase().getUsers();
                db.close();
                currentRun = runs.get(selectedRunId);
                notifyAll();
            }
        });
        myThread.start();
        map = findViewById(R.id.mapView);
        if(map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);
            try {
                myThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (RunningService.GPSCoordinate coord : currentRun.getGpsdata()) {
                IMapController mapController = map.getController();
                mapController.setZoom(9.5);

                GeoPoint point = new GeoPoint(coord.lat, coord.lon);
                mapController.setCenter(point);
                Marker m = new Marker(map);
                m.setPosition(point);
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                m.setTitle("This is ");
                map.getOverlays().add(m);
            }
        }else{
            Log.e("Custom Error","Map was null");
        }

    }
}