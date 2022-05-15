package hu.run4yourlife.interfaces;

import java.util.ArrayList;

import hu.run4yourlife.RunningService;

public class Speedtrap {
    private static final double r = 6378100;
    private static final double pi = 3.14159265359;

    public double calcDist(RunningService.GPSCoordinate a, RunningService.GPSCoordinate b) {
        double lat1 = a.lat * pi / 180.0;
        double lon1 = a.lon * pi / 180.0;
        double alt1 = a.alt * pi / 180.0;
        double lat2 = b.lat * pi / 180.0;
        double lon2 = b.lon * pi / 180.0;
        double alt2 = b.alt * pi / 180.0;

        double rho1 = r * Math.cos(lat1);
        double z1 = r * Math.sin(lat1);
        double x1 = rho1 * Math.cos(lon1);
        double y1 = rho1 * Math.sin(lon1);

        double rho2 = r * Math.cos(lat2);
        double z2 = r * Math.sin(lat2);
        double x2 = rho2 * Math.cos(lon2);
        double y2 = rho2 * Math.sin(lon2);

        double dot = (x1 * x2 + y1 * y2 + z1 * z2);
        double cos_theta = dot / (r * r);
        double theta = Math.acos(cos_theta);
        double xy = theta * r;
        double ret = Math.sqrt(xy*xy+(alt2-alt1)*(alt2-alt1))/1000.0;
        return Double.isNaN(ret)?0.0:ret;
    }

    /**
     * @param coords input coordinate array
     * @return array of size input with distance data plus distance sum as last element
     */
    public ArrayList<Double> CalcAllDistance(ArrayList<RunningService.GPSCoordinate> coords){
        ArrayList<Double> allDist = new ArrayList<>();
        double sum=0;
        for( int i=0;i<coords.size()-1;i++){
            double temp=calcDist(coords.get(1),coords.get(i+1));
            allDist.add(temp);
            sum+=temp;
        }
        allDist.add(sum);
        return allDist;
    }

    private double calcTimeDiff(RunningService.GPSCoordinate a, RunningService.GPSCoordinate b) {
        return Math.abs(b.timestamp-a.timestamp)/Math.pow(10,7);
    }

    public ArrayList<Double> SpeedCalc(ArrayList<RunningService.GPSCoordinate> in) {
        ArrayList<Double> speeds = new ArrayList<Double>();
        for ( int i = 0; i < in.size()-1; ++i) {
            speeds.add(calcDist(in.get(i), in.get(i+1))/calcTimeDiff(in.get(i), in.get(i+1)));
        }
        return speeds;
    }
}
