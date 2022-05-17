package hu.run4yourlife;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;

import hu.run4yourlife.interfaces.DbSummary;
import hu.run4yourlife.interfaces.QualityData;
import hu.run4yourlife.interfaces.RecommendedTime;
import hu.run4yourlife.interfaces.StaticStuff;
import hu.run4yourlife.interfaces.UserData;
import hu.run4yourlife.rvadapters.MainRVAdapter;

/**
 * Dani+ Levi
 */
public class MainActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    private static final int ALL_PERM_REQUEST = 1;
    private BarChart chart;
    private static final String[] _permission = {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION/*,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION,*/,Manifest.permission.FOREGROUND_SERVICE,Manifest.permission.WRITE_EXTERNAL_STORAGE};


    MainRVAdapter adapter;
    RecyclerView rv;
    LinearLayout forecastBanner;
    ImageView profileP;


    private class Data {

        final String xAxisValue;
        final float yValue;
        final float xValue;

        Data(float xValue, float yValue, String xAxisValue) {
            this.xAxisValue = xAxisValue;
            this.yValue = yValue;
            this.xValue = xValue;
        }
    }
    /*
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>*/
    long clicktimeoutMS = 0;

    @Override
    protected void onResume() {
        super.onResume();
        updateGraphData();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!checkPermissions(_permission)){
            ActivityCompat.requestPermissions(
                    this,
                    _permission,
                    ALL_PERM_REQUEST
            );
        }
        StaticStuff.recommendedTime = new RecommendedTime(MainActivity.this);
        createNotificationChannel();
        rv = findViewById(R.id.challengeRecycler);
        adapter = new MainRVAdapter(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(adapter);
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


        profileP= findViewById(R.id.profilePicture);
        profileP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(it);
            }
        });
        forecastBanner = findViewById(R.id.forecastBanner);
        forecastBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this,ForecastActivity.class);
                startActivity(it);
            }
        });

        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it = new Intent(MainActivity.this, RunTrackerActivity.class);
                it.putExtra(StaticStuff.RUN_EXTRA_ID_NAME,adapter.getCurrSelected());
                startActivity(it);
            }
        });
        launchGraph();
        checkApiKey();

    }

    void checkApiKey(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String apiKey = prefs.getString("KEY",null);
        if (apiKey == null){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("OpenWeatherMap API key");
            alertDialog.setMessage("Please enter the API key");

            final EditText input = new EditText(MainActivity.this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            input.setLayoutParams(lp);
            alertDialog.setView(input);
            alertDialog.setIcon(R.drawable.ic_launcher_foreground);

            alertDialog.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String key = input.getText().toString();
                            SharedPreferences.Editor e = prefs.edit();
                            e.putString("KEY",key);
                            e.commit();
                            StaticStuff.WeatherApiKey = key;
                            launchLocation();
                        }
                    });

            alertDialog.setNegativeButton("NO",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog.show();

        }else{
            StaticStuff.WeatherApiKey = apiKey;
            launchLocation();
        }
    }

    private void launchLocation(){
        SingleLocation sl = new SingleLocation(this, new SingleLocation.LocationReceivedCallback() {
            @Override
            public void onLocationReceived(double lat, double lon, double hei, double accu) {
                StaticStuff.cachedCoord = new RunningService.GPSCoordinate(lat,lon,hei,accu,System.currentTimeMillis());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        StaticStuff.recommendedTime.fillDataFromHTTPRequest((float) StaticStuff.cachedCoord.lat, (float) StaticStuff.cachedCoord.lon);
                        if (StaticStuff.recommendedTime.getDailyStat().size() == 0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"BAD API KEY",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    TextView uvindex = findViewById(R.id.uvIndex);
                                    uvindex.setText("UV index: " + StaticStuff.recommendedTime.getCurrent().uvi);
                                    TextView airw = findViewById(R.id.airQuality);
                                    airw.setText("Air Quality: " + StaticStuff.recommendedTime.getCurrent().airQualityIndex);
                                    TextView best = findViewById(R.id.bestTime);
                                    QualityData bs = StaticStuff.recommendedTime.getBestTime();
                                    int ourtime = new java.util.Date((long) bs.timeIndex * 1000).getHours();

                                    best.setText(" " + ourtime + ":00-" + (ourtime + 1) % 24 + ":00");
                                }
                            });
                        }
                    }
                }).start();
            }
        });
        sl.initLocationRequest();

    }

    private void launchGraph() {
        chart = findViewById(R.id.weeklyChart);
        chart.setExtraTopOffset(-30f);
        chart.setExtraBottomOffset(10f);
        chart.setExtraLeftOffset(70f);
        chart.setExtraRightOffset(70f);

        chart.setDrawBarShadow(false);
        chart.setDrawValueAboveBar(false);

        chart.getDescription().setEnabled(false);

        // scaling can now only be done on x- and y-axis separately
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(Color.TRANSPARENT);

        chart.setOnChartValueSelectedListener(this);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.LTGRAY);
        xAxis.setTextSize(13f);
        xAxis.setLabelCount(5);
        xAxis.setEnabled(false);
        xAxis.setCenterAxisLabels(false);
        xAxis.setGranularity(1f);
        YAxis left = chart.getAxisLeft();
        left.setAxisMaximum(UserData.getAdvisedActivity() * 2);
        left.setDrawLabels(true);
        LimitLine limitLine = new LimitLine(UserData.getAdvisedActivity());
        limitLine.enableDashedLine(20f, 20f, 1);
        left.addLimitLine(limitLine);

        left.setSpaceTop(25f);
        left.setSpaceBottom(25f);

        left.setDrawZeroLine(true); // draw a zero line
        left.setZeroLineColor(Color.GRAY);
        left.setZeroLineWidth(0.7f);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(false);

        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.i("ONTOUCH", "LEFUT");
                // This allows clicking twice on same selection to show the popup again
                chart.getOnTouchListener().setLastHighlighted(null);
                chart.highlightValues(null);
                return false;
            }
        });
        updateGraphData();
    }
    private void updateGraphData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DbSummary sum = new DbSummary();
                ArrayList<Float> s = sum.getSummaryFromDB(MainActivity.this);
                for (Float i : s){
                    //Log.i("Summary","--" + i);
                }
                //Log.i("Summary",s.toString());
                setGraphData(s);
            }
        }).start();
    }

    private void setGraphData(ArrayList<Float> s){
        final ArrayList<Data> data = new ArrayList<>();
        for (int i = 0; i < s.size(); i++){
            data.add(new Data(i,Math.round(s.get(i)),"")); //TODO divide by 60 to get minutes
        }
        ArrayList<BarEntry> values = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        int green = getColor(R.color.primaryColor);
        int red =getColor(R.color.red);

        for (int i = 0; i < data.size(); i++) {

            Data d = data.get(i);
            BarEntry entry = new BarEntry(d.xValue, d.yValue);
            values.add(entry);

            // specific colors
            if (d.yValue < UserData.getAdvisedActivity())
                colors.add(red);
            else
                colors.add(green);
        }

        BarDataSet set;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set = new BarDataSet(values, "Values");
            set.setColors(colors);
            set.setValueTextColors(colors);

            BarData bdata = new BarData(set);
            bdata.setDrawValues(false);

            bdata.setBarWidth(0.8f);

            chart.setData(bdata);
            chart.invalidate();
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("ONVALUESELECTED","LEFUT");
        if(e.getY() != 0 && System.currentTimeMillis()-clicktimeoutMS > 100) {
            clicktimeoutMS = System.currentTimeMillis();
            Intent it = new Intent(MainActivity.this, RunStatisticsActivity.class);
            it.putExtra("day", (e.getX()));
            startActivity(it);
        }
    }

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    @Override
    public void onNothingSelected() {
        Log.i("chart", "nothing selected");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Running notification";
            String description = "This notification is shown when a ";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(StaticStuff.NOTIF_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }

    protected boolean checkPermissions(String[] perm){
        for(String p:perm){
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERM_REQUEST && !checkPermissions(permissions)) {
            ActivityCompat.requestPermissions(
                    this,
                    _permission,
                    ALL_PERM_REQUEST
            );
        }
    }

}