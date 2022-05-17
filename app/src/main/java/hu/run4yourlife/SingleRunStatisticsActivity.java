package hu.run4yourlife;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;
import org.osmdroid.views.*;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import java.util.List;
import java.util.stream.Collectors;

import hu.run4yourlife.database.RunhistoryDB;
import hu.run4yourlife.database.RunningDatabase;
import hu.run4yourlife.interfaces.Challenge;
import hu.run4yourlife.interfaces.Challenges;
import hu.run4yourlife.interfaces.Speedtrap;
import hu.run4yourlife.interfaces.StaticStuff;

/**
 * Levi + /Dani
 */
public class SingleRunStatisticsActivity extends AppCompatActivity {
    RunningDatabase db;
    ArrayList<RunhistoryDB> runs = new ArrayList<>();
    RunhistoryDB currentRun;
    int selectedRunId;
    Speedtrap sp = new Speedtrap();
    MapView map;
    FloatingActionButton fab;
    LineChart topchart;
    LineChart bottomchart;
    LinearLayout linearLayoutCompat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_run_statistics);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        map = (MapView) findViewById(R.id.mapView);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        fab=findViewById(R.id.mapFAB);
        linearLayoutCompat = findViewById(R.id.linearLayoutOverlay);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(linearLayoutCompat.getVisibility()==View.GONE){
                    linearLayoutCompat.setVisibility(View.VISIBLE);
                }else{
                    linearLayoutCompat.setVisibility(View.GONE);
                }
            }
        });
        topchart=findViewById(R.id.topChart);
        bottomchart=findViewById(R.id.bottomChart);



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
                topchart = findViewById(R.id.topChart);
                bottomchart = findViewById(R.id.bottomChart);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initCharts(topchart);
                        initCharts(bottomchart);

                        ArrayList<Double> topValues =sp.SpeedCalc(currentRun.getGpsdata());
                        ArrayList<Double> bottomValues = (ArrayList<Double>) currentRun.getGpsdata().stream()
                                .map(RunningService.GPSCoordinate::getAltitude)
                                .collect(Collectors.toList());

                        ArrayList<Float> distVals=new ArrayList<>();
                        ArrayList<Entry> topData=new ArrayList<Entry>();
                        ArrayList<Entry> bottomData=new ArrayList<Entry>();
                        float sum=0;
                        for( Double i :(sp.CalcAllDistance(currentRun.getGpsdata()))){
                            sum+=i.floatValue();
                            distVals.add(sum);
                        }


                        for (int i = 0; i < topValues.size(); i++) {
                            double val = topValues.get(i);
                            topData.add(new Entry(distVals.get(i), ((float) val)));
                        }
                        for (int i = 0; i < bottomValues.size(); i++) {
                            double val = bottomValues.get(i);
                            bottomData.add(new Entry(distVals.get(i), ((float) val)));
                        }
                        Log.i("chartdataSize",topData.size() + "    -    "+ bottomData.size());


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setData(topchart, topData);
                                setData(bottomchart,bottomData);
                            }
                        });


                    }
                }).start();


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
                            map.setBuiltInZoomControls(false);
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
                                        //Log.i("SingleRunStatistics","DistRequired:" + distRequired + " currentIdx:" + currIdx);
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
    private void initCharts(LineChart lineChart){
        lineChart.setViewPortOffsets(0, 0, 0, 0);
        lineChart.setBackgroundColor(getColor(R.color.light2BackgroundColor));

        // no description text
        lineChart.getDescription().setEnabled(false);

        // disable touch gestures
        lineChart.setTouchEnabled(false);

        // disable scaling and dragging
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);

        // if disabled, scaling can be done on x- and y-axis separately
        lineChart.setPinchZoom(false);

        lineChart.setDrawGridBackground(false);
        lineChart.setMaxHighlightDistance(300);

        XAxis x = lineChart.getXAxis();
        x.setEnabled(true);

        YAxis y = lineChart.getAxisLeft();
        y.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        y.setLabelCount(6, false);
        y.setTextColor(R.color.primaryTextColor);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(R.color.primaryColor);

        lineChart.getAxisRight().setEnabled(false);

        lineChart.getLegend().setEnabled(false);

        //lineChart.animateXY(2000, 2000);

        // don't forget to refresh the drawing
        lineChart.invalidate();
    }



    private void setData(@NonNull LineChart lineChart, ArrayList<Entry> inputData) {


        LineDataSet set1;


        if (lineChart.getData() != null &&
                lineChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            set1.setValues(inputData);
            lineChart.getData().notifyDataChanged();
            lineChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(inputData, "DataSet 1");

            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.02f);
            set1.setDrawFilled(true);
            set1.setDrawCircles(false);
            set1.setLineWidth(1.8f);
            set1.setCircleRadius(4f);
            /*set1.setCircleColor(Color.WHITE);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.WHITE);
            set1.setFillColor(Color.WHITE);*/
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);
            /*set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return lineChart.getAxisLeft().getAxisMinimum();
                }
            });*/

            // create a data object with the data sets
            LineData data = new LineData(set1);
            data.setValueTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            data.setValueTextSize(9f);
            data.setDrawValues(false);

            // set data
            lineChart.setData(data);
            lineChart.invalidate();
        }
    }
}