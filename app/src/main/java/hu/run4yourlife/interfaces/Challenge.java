package hu.run4yourlife.interfaces;

import java.util.ArrayList;

public class Challenge {
    private String ChallengeName;
    private ArrayList<String> Stops; //megallok
    private ArrayList<Double> Distances; //megallok kozti tavok
    private ArrayList<Integer> Times; //megallok kozti idok

    public String getChallengeName() {
        return ChallengeName;
    }

    public ArrayList<String> getStops() {
        return Stops;
    }

    public ArrayList<Double> getDistances() {
        return Distances;
    }

    public ArrayList<Integer> getTimes() {
        return Times;
    }

    /**
     *Konstruktor
     * abc
     */
    public Challenge(String _ChallengeName, ArrayList<String> _Stops, ArrayList<Double> _Distance, ArrayList<Integer> _Times)
    {
        this.ChallengeName = _ChallengeName;
        this.Stops = _Stops;
        this.Distances = _Distance;
        this.Times = _Times;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "ChallengeName='" + ChallengeName + '\'' +
                ", Stops=" + Stops +
                ", Distance=" + Distances +
                ", Times=" + Times +
                '}';
    }

    public double getMaxDist() {
        return Distances.stream().reduce(0.0, Double::sum);
    }
}