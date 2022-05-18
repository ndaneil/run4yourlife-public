package hu.run4yourlife.interfaces;

import android.content.Context;
import android.service.controls.Control;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;

/**
 * Dani , getBestTime: Kinga+Matyi
 */
public class RecommendedTime {

    private Context ctx;
    ArrayList<QualityData> qualities = new ArrayList<>();
    QualityData current = null;


    public RecommendedTime(Context ctx){
        this.ctx = ctx;

    }

    public QualityData getCurrent() {
        return current;
    }

    public boolean fillDataFromHTTPRequest(float lat, float lon){
        String result = makeHttpRequest(lat,lon);
        String result2 = makeHttpRequestPoll(lat,lon);
        if (result == null || result2 == null) return false;
        try {
            JSONObject resp1 = new JSONObject(result);
            JSONObject poll = new JSONObject(result2);
            qualities.clear();
            JSONObject curr = resp1.getJSONObject("current");
            JSONObject weather = curr.getJSONArray("weather").getJSONObject(0);
            current = new QualityData(curr.getDouble("temp"),
                    curr.getDouble("humidity"),
                    curr.getDouble("clouds"),
                    curr.getDouble("uvi"),
                    curr.getDouble("wind_speed"),
                    curr.has("rain")?curr.getJSONObject("rain").getDouble("1h"):0,
                    curr.getLong("dt"),-1,
                    weather.getString("description"),
                    weather.getString("icon"),
                    weather.getString("main"),
                    weather.getInt("id"));
            JSONArray airqualities = poll.getJSONArray("list");
            current.airQualityIndex = airqualities.getJSONObject(0).getJSONObject("main").getDouble("aqi");
            JSONObject aqc = airqualities.getJSONObject(0).getJSONObject("components");
            current.aqComponents = new QualityData.AQComponents(aqc.getDouble("co"),aqc.getDouble("no"),
                    aqc.getDouble("no2"),aqc.getDouble("o3"),aqc.getDouble("so2"),aqc.getDouble("pm2_5"),
                    aqc.getDouble("pm10"),aqc.getDouble("nh3"));

            JSONArray hourly = resp1.getJSONArray("hourly");
            for (int i = 0; i < Math.min(hourly.length(), airqualities.length());i++){
                curr = hourly.getJSONObject(i);
                QualityData qd = new QualityData(curr.getDouble("temp"),
                        curr.getDouble("humidity"),
                        curr.getDouble("clouds"),
                        curr.getDouble("uvi"),
                        curr.getDouble("wind_speed"),
                        curr.has("rain")?curr.getJSONObject("rain").getDouble("1h"):0,
                        curr.getLong("dt"),-1,
                        weather.getString("description"),
                        weather.getString("icon"),
                        weather.getString("main"),
                        weather.getInt("id"));
                qd.airQualityIndex = airqualities.getJSONObject(i).getJSONObject("main").getDouble("aqi");
                aqc = airqualities.getJSONObject(i).getJSONObject("components");
                qd.aqComponents = new QualityData.AQComponents(aqc.getDouble("co"),aqc.getDouble("no"),
                        aqc.getDouble("no2"),aqc.getDouble("o3"),aqc.getDouble("so2"),aqc.getDouble("pm2_5"),
                        aqc.getDouble("pm10"),aqc.getDouble("nh3"));
                qualities.add(qd);
            }

            //Log.i("API-CURRENT",current.toString());
            /*for (QualityData d : qualities){
                Log.i("API-FORECASR",d.toString());
            }*/


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String makeHttpRequestPoll(float lat, float lon) {
        URL url = null;
        try {
            url = new URL("https://api.openweathermap.org/data/2.5/air_pollution/forecast?lat="+lat+"&lon="+lon+"&appid="+StaticStuff.WeatherApiKey);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();
                String server_response;
                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = readStream(urlConnection.getInputStream());
                    Log.v("Weather-RESTAPI", server_response);
                    return server_response;
                }
            } catch(IOException e){
                e.printStackTrace();
            }finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<QualityData> getForecast(){
        return qualities;
    }

    private static final int hourMin = 5;
    private static final int hourMax = 22;

    public QualityData getBestTime(){
        double min = 10;
        QualityData min_qd = qualities.get(0);
        int i = 0;
        for ( QualityData qd : qualities) {
            int ourtime = new java.util.Date((long)qd.timeIndex*1000).getHours();
            //Log.i("OUR","OT:" + ourtime);
            if (ourtime >= hourMin && ourtime <= hourMax && i <24) {
                min = qd.airQualityIndex + qd.uvi/2f+ourtime/48f;
                min_qd = qd;
            }
            i++;
        }
        i = 0;
        for ( QualityData qd : qualities) {
            int ourtime = new java.util.Date((long)qd.timeIndex*1000).getHours();
            if (qd.airQualityIndex + qd.uvi/2f+ourtime/48f < min && i <24 && ourtime >= hourMin && ourtime <= hourMax) {
                min = qd.airQualityIndex + qd.uvi/2f+ourtime/48f;
                min_qd = qd;
            }
            i++;
        }
        return min_qd;
    }

    public ArrayList<TimeData> getDailyStat() {
        ArrayList<TimeData> result = new ArrayList<>();
        for(int i = 0; i<24 && i < qualities.size(); i++) {
            result.add(new TimeData(qualities.get(i).timeIndex, qualities.get(i).airQualityIndex + qualities.get(i).uvi/2f));
        }
        return result;
    }

    private String makeHttpRequest(float lat,float lon){
        URL url = null;
        try {
            url = new URL("https://api.openweathermap.org/data/2.5/onecall?lat="+lat+"&lon="+lon+"&appid="+StaticStuff.WeatherApiKey+"&units=metric&exclude=alerts,daily,minutely");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();
                String server_response;
                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = readStream(urlConnection.getInputStream());
                    Log.v("Weather-RESTAPI", server_response);
                    return server_response;
                }
            } catch(IOException e){
                e.printStackTrace();
            }finally {
                urlConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

// Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    public class TimeData {
        public long time;
        public float quality;
        public int hourtime;
        public TimeData(long _time, double q) {
            this.time = _time;
            this.quality  = (float) q;
            this.hourtime = new java.util.Date((long)this.time*1000).getHours();
        }

        public String getHourString(){
            return hourtime+"-"+(hourtime+1)%24;
        }
    }

}
