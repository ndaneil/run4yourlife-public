package hu.run4yourlife;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.BubbleEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import hu.run4yourlife.interfaces.Challenge;
import hu.run4yourlife.interfaces.Challenges;
import hu.run4yourlife.interfaces.StaticStuff;

public class RunTrackerActivity extends AppCompatActivity implements RunningService.DataChangeCallback {
    TextView challengeName;
    BarChart chart;
    TextView nextStopName;
    ImageView foodImage;
    TextView timerTV;
    FloatingActionButton floatingStartButton;

    static int challengedID = -1;

    boolean stopped = false;

    double distanceCurrent = 0;
    Challenge challenge;

    Thread t;

    static long startMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_tracker);

        startMS = System.currentTimeMillis()/1000L;
        challengeName = findViewById(R.id.trackerChallengeName);
        chart = findViewById(R.id.trackerChart);
        nextStopName = findViewById(R.id.trackerNextStopName);
        foodImage = findViewById(R.id.foodImage);
        timerTV = findViewById(R.id.timerTextView);
        floatingStartButton = findViewById(R.id.floatingStartTimer);
        floatingStartButton.setImageResource(android.R.drawable.ic_media_pause);
        challengedID = getIntent().getIntExtra(StaticStuff.RUN_EXTRA_ID_NAME,0);



        try {
            Challenges c = new Challenges(this);
            challenge = c.getChallengeDetailsForID(challengedID);
            challengeName.setText(challenge.getChallengeName());
            challengeName.setVisibility(View.GONE);
            setTitle(challenge.getChallengeName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        RunningService.callback = this;
        startTracking();

        floatingStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (stopped == false){
                    stopped = true;
                    floatingStartButton.setImageResource(android.R.drawable.ic_delete);
                    t.interrupt();
                    stopTracking();
                }else{
                    finish();
                }
            }
        });

        createGraph();

    }
    private void createThread(){
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (t.isInterrupted() == false) {
                    try {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int time = Math.round(System.currentTimeMillis()/1000L-startMS);
                                int sec = time%60;
                                int min = time/60;
                                timerTV.setText("" +(min<10?"0":"")+min  + ":" +(sec<10?"0":"")+ sec);
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        });
        t.start();
    }

    private void createGraph(){
        try {
            Challenge ch = new Challenges(this).getChallengeDetailsForID(challengedID);
            ArrayList<BarEntry> values = new ArrayList<>();
            ArrayList<Integer> colors = new ArrayList<>();
            ArrayList<String> labels = new ArrayList<>();

            BarDataSet set;

            if (chart.getData() != null &&
                    chart.getData().getDataSetCount() > 0) {
                set = (BarDataSet) chart.getData().getDataSetByIndex(0);
                //TODO UPDATE data
                values.clear();
                double runningtotal = distanceCurrent;
                String nexstoplabel = runningtotal >0 ? null: ch.getStops().get(0);
                for (int i = 0; i < ch.getStops().size();i++){
                    double dis = ch.getDistances().get(i);
                    double val = 0f;
                    if (runningtotal > dis){
                        runningtotal -= dis;
                        val = 1;
                    }else{
                        if(runningtotal>0){
                            if(i+1 >= ch.getStops().size()){
                                nexstoplabel="Végállomás";
                            }else {
                                nexstoplabel = ch.getStops().get(i + 1);
                            }
                        }
                        if(dis==0){
                            val=0;
                        }else{
                            val = runningtotal/dis;
                        }
                        runningtotal = 0;
                    }
                    values.add(new BarEntry(i,(float)val));
                }
                if(nexstoplabel==null){
                    nextStopName.setText("Végállomás");
                }else {
                    nextStopName.setText(nexstoplabel);
                }

                set.setValues(values);

                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();

            } else {
                int color = Color.rgb(255, 127, 0);
                for (int i = 0; i < ch.getStops().size(); i++) {
                    BarEntry entry = new BarEntry(i, 0);
                    values.add(entry);
                    colors.add(color);
                    labels.add(ch.getStops().get(i));
                }
                set = new BarDataSet(values, "Values");
                set.setColors(colors);
                set.setValueTextColors(colors);

                BarData data = new BarData(set);
                data.setDrawValues(false);


                //data.setBarWidth(0.8f);

                XAxis xAxis = chart.getXAxis();
                xAxis.setGranularity(1f);
                xAxis.setLabelCount(24);
                xAxis.setAvoidFirstLastClipping(false);
                xAxis.setValueFormatter(new IndexAxisValueFormatter(labels) {
                });
                xAxis.setLabelRotationAngle(90);
                xAxis.setDrawGridLines(false);
                xAxis.setDrawAxisLine(false);

                xAxis.setDrawLabels(true);
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                YAxis leftAxis = chart.getAxisLeft();
                leftAxis.setAxisMaximum(1.0f);
                leftAxis.setAxisMinimum(0.0f);
                leftAxis.setDrawAxisLine(false);
                leftAxis.setDrawGridLines(false);
                xAxis.setTextColor(ContextCompat.getColor(this,R.color.secondaryDarkColor));


                chart.getAxisRight().setEnabled(false);
                chart.setDrawGridBackground(false);

                chart.zoom(2,1,0,0);
                chart.setDrawBarShadow(false);
                chart.setDrawValueAboveBar(false);
                chart.getDescription().setEnabled(false);
                chart.setScaleEnabled(false);
                chart.setPinchZoom(false);
                chart.setDrawGridBackground(false);
                chart.getAxisLeft().setDrawLabels(false);
                chart.getAxisRight().setDrawLabels(false);
                chart.setScaleEnabled(false);
                chart.getDescription().setEnabled(false);
                chart.getLegend().setEnabled(false);
                chart.setData(data);
                chart.invalidate();

            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }



    @Override
    protected void onPause() {
        RunningService.callback = null;
        super.onPause();
        if (t != null){
            t.interrupt();
        }
    }

    @Override
    protected void onResume() {
        RunningService.callback = this;
        super.onResume();
        createThread();
    }

    void startTracking(){
        Intent serviceIntent = new Intent(getApplicationContext(),RunningService.class);
        serviceIntent.setAction(RunningService.ACTION_START);
        serviceIntent.putExtra(StaticStuff.RUN_EXTRA_ID_NAME,challengedID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(serviceIntent);
        }else{
            getApplicationContext().startService(serviceIntent);
        }
    }
    void stopTracking(){
        Intent serviceIntent = new Intent(getApplicationContext(),RunningService.class);
        serviceIntent.setAction(RunningService.ACTION_STOP);
        serviceIntent.putExtra(StaticStuff.RUN_EXTRA_ID_NAME,challengedID);
        getApplicationContext().startService(serviceIntent);
    }

    @Override
    public void newData(double totalDistance, long timeElapsed, double lat, double lon, double accu) {
        //Toast.makeText(getApplicationContext(),"GPS Data received...",Toast.LENGTH_SHORT).show();
        this.distanceCurrent = totalDistance*1000;
        startMS = timeElapsed;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //timerTV.setText("" + distanceCurrent);
                createGraph();
                TextView tv = findViewById(R.id.kmDone);
                tv.setText("" + (Math.round(distanceCurrent*100)/100000.0) + " km");
                //chart.notifyDataSetChanged();
                //nextStopDistance.setText(" " + lat + ";" + lon);
            }
        });
        //TODO...
    }
}