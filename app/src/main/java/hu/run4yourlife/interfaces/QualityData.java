package hu.run4yourlife.interfaces;

import androidx.annotation.NonNull;

/**
 * Dani
 */
public class QualityData {
    public double currentTemp;
    public double currentHumidity;
    public double clouds;
    public double uvi;
    public double windspeed;
    public double rain;
    public long timeIndex;
    public double airQualityIndex;
    public String weatherDescription;
    public String weatherIcon;
    public String weatherMain;
    public int weatherID;

    AQComponents aqComponents;

    public static class AQComponents{
        public double co,no,no2,o3,so2,pm2_5,pm10,nh;

        public AQComponents(double co, double no, double no2, double o3, double so2, double pm2_5, double pm10, double nh) {
            this.co = co;
            this.no = no;
            this.no2 = no2;
            this.o3 = o3;
            this.so2 = so2;
            this.pm2_5 = pm2_5;
            this.pm10 = pm10;
            this.nh = nh;
        }

        @Override
        public String toString() {
            return "Detailed Air Quality:" +
                    "\n  - CO: " + co +
                    "\n  - NO: " + no +
                    "\n  - NO2: " + no2 +
                    "\n  - O3: " + o3 +
                    "\n  - SO2: " + so2 +
                    "\n  - PM2,5: " + pm2_5 +
                    "\n  - PM10: " + pm10 +
                    "\n  - NH: " + nh + "\n";
        }
    }

    public QualityData(double currentTemp, double currentHumidity, double clouds, double uvi, double windspeed, double rain, long timeIndex, double airQualityIndex, String weatherDescription, String weatherIcon, String weatherMain, int weatherID) {
        this.currentTemp = currentTemp;
        this.currentHumidity = currentHumidity;
        this.clouds = clouds;
        this.uvi = uvi;
        this.windspeed = windspeed;
        this.rain = rain;
        this.timeIndex = timeIndex;
        this.airQualityIndex = airQualityIndex;
        this.weatherDescription = weatherDescription;
        this.weatherIcon = weatherIcon;
        this.weatherMain = weatherMain;
        this.weatherID = weatherID;
    }

    @NonNull
    @Override
    public String toString() {
        return "UVIndex: "+uvi+"\nAirQualityIndex: "+ airQualityIndex+ "\nTemperature: " +  currentTemp +"\n"+ "Humidity: " + currentHumidity + "\n"+"Clouds: "+clouds +
                "\nwindspeed: "+windspeed + "\nrain: "+rain+
                "\nWeather Desctiption: " + weatherDescription+
                "\n"+aqComponents.toString();


    }
}
