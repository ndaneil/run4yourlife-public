package hu.run4yourlife;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DecimalFormat;
import java.util.ArrayList;

import hu.run4yourlife.interfaces.QualityData;
import hu.run4yourlife.interfaces.RecommendedTime;
import hu.run4yourlife.interfaces.StaticStuff;

/***
 * 100% L
 */
public class ForecastActivity extends AppCompatActivity implements OnChartValueSelectedListener {

    ImageView weatherImage;
    TextView currentTemp;
    BarChart forecastBarchart;
    ScrollView detailedForecast;
    ArrayList<RecommendedTime.TimeData> forecastData;
    //RecommendedTime RT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        forecastBarchart = findViewById(R.id.forecastBarchart);
        currentTemp=findViewById(R.id.currentTempTV);
        weatherImage = findViewById(R.id.weatherIm);


        //RT = new RecommendedTime(this);

        if (StaticStuff.recommendedTime == null || StaticStuff.recommendedTime.getForecast().size() == 0 || StaticStuff.cachedCoord == null || System.currentTimeMillis() - StaticStuff.cachedCoord.timestamp > 300000){
            SingleLocation sl = new SingleLocation(this, new SingleLocation.LocationReceivedCallback() {
                @Override
                public void onLocationReceived(double lat, double lon, double hei, double accu) {
                    StaticStuff.cachedCoord = new RunningService.GPSCoordinate(lat,lon,hei,accu,System.currentTimeMillis());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if(StaticStuff.recommendedTime.fillDataFromHTTPRequest((float)StaticStuff.cachedCoord.lat,(float)StaticStuff.cachedCoord.lon)){
                                runsetStep2();
                            }else Log.e("Error", "Dani Béna");
                        }
                    }).start();

                }
            });
            sl.initLocationRequest();
        }else{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runsetStep2();
                }
            }).start();
        }



    }
    private void runsetStep2(){
                    Log.i("Yeey","Dani menő");
                    forecastData=StaticStuff.recommendedTime.getDailyStat();
                    if (forecastData.size() == 0){
                        if(StaticStuff.recommendedTime.fillDataFromHTTPRequest((float)StaticStuff.cachedCoord.lat,(float)StaticStuff.cachedCoord.lon)){
                            Log.i("Yeey","Dani menő");
                        }else Log.e("Error", "Dani Béna");
                    }
                    QualityData nowData = StaticStuff.recommendedTime.getCurrent();
                    setData(forecastData);


                    int picturename= R.drawable.sunny;
                    int id = nowData.weatherID;
                    if (id>=200 && id<300){
                        picturename=R.drawable.thunder;
                    }else if(id>=300 && id<400){
                        picturename=R.drawable.rain;
                    }else if(id>=500 && id<505){
                        picturename=R.drawable.rain;
                    }else if(id== 511){
                        picturename=R.drawable.snow;
                    }else if(id>=520 && id<600){
                        picturename=R.drawable.rain;
                    }else if(id>=600 && id<700){
                        picturename=R.drawable.snow;
                    }else if(id>=700 && id<800){
                        picturename=R.drawable.mist;
                    }else if(id == 800){
                        picturename=R.drawable.sunny;
                    }else if(id == 801){
                        picturename=R.drawable.partlycloudy;
                    }else if(id == 802){
                        picturename=R.drawable.cloudy;
                    }else if(id == 803 || id == 804){
                        picturename=R.drawable.partlycloudy;
                    }

                    int finalPicturename = picturename;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            currentTemp.setText(nowData.currentTemp + " °C");
                            weatherImage.setImageResource(finalPicturename);
                            weatherImage.setVisibility(View.VISIBLE);
                        }
                    });
    }
    private void setData(ArrayList<RecommendedTime.TimeData> dataList) {
        int currentHour = dataList.get(0).hourtime;
        ArrayList<BarEntry> values = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        ArrayList<String> xValues = new ArrayList<>();



        int green = getColor(R.color.primaryColor);
        int yellow = getColor(R.color.orange);
        int red = getColor(R.color.red);

        for (int i = 0; i < dataList.size(); i++) {

            RecommendedTime.TimeData d = dataList.get(i);
            BarEntry entry = new BarEntry(i,d.quality);
            values.add(entry);
            xValues.add(dataList.get(i).getHourString());

            // specific colors
            if (d.quality >= 4)
                colors.add(red);
            else if(d.quality>=2.5)
                colors.add(yellow);
            else
                colors.add(green);
        }

        BarDataSet set;

        if (forecastBarchart.getData() != null &&
                forecastBarchart.getData().getDataSetCount() > 0) {
            set = (BarDataSet) forecastBarchart.getData().getDataSetByIndex(0);
            set.setValues(values);
            forecastBarchart.getData().notifyDataChanged();
            forecastBarchart.notifyDataSetChanged();
        } else {
            set = new BarDataSet(values, "Values");
            set.setColors(colors);
            set.setValueTextColors(colors);

            BarData data = new BarData(set);
            data.setDrawValues(false);


            //data.setBarWidth(0.8f);

            XAxis xAxis = forecastBarchart.getXAxis();
            xAxis.setXOffset(0.5f);
            xAxis.setGranularity(1f);
            xAxis.setLabelCount(24);
            xAxis.setAvoidFirstLastClipping(false);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(xValues) {
            });
            xAxis.setTextColor(ContextCompat.getColor(this,R.color.secondaryDarkColor));


            forecastBarchart.zoom(2,1,0,0);
            forecastBarchart.getAxisLeft().setDrawLabels(false);
            forecastBarchart.getAxisRight().setDrawLabels(false);
            forecastBarchart.setScaleEnabled(false);
            forecastBarchart.getDescription().setEnabled(false);
            forecastBarchart.getLegend().setEnabled(false);
            forecastBarchart.setOnChartValueSelectedListener(this);

            forecastBarchart.setData(data);
            forecastBarchart.invalidate();
        }
    }

    /**
     * Called when a value has been selected inside the chart.
     *
     * @param e The selected Entry
     * @param h The corresponding highlight object that contains information
     */
    @Override
    public void onValueSelected(Entry e, Highlight h) {
        TextView scroller = findViewById(R.id.scrollerText);
        QualityData q = StaticStuff.recommendedTime.getForecast().get(( (int) e.getX()));
        scroller.setText(q.toString());
    }

    /**
     * Called when nothing has been selected or an "un-select" has been made.
     */
    @Override
    public void onNothingSelected() {

    }
}