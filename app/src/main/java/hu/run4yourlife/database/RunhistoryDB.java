package hu.run4yourlife.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.lang.reflect.Array;
import java.util.ArrayList;

import hu.run4yourlife.RunningService;

/**
 * Dani
 */
@Entity(tableName="FUTASOK")
public class RunhistoryDB {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "challengeID")
    private int challengeID;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "endtimestamp")
    private long endtimestamp;

    public long getEndtimestamp() {
        return endtimestamp;
    }

    public void setEndtimestamp(long endtimestamp) {
        this.endtimestamp = endtimestamp;
    }

    public ArrayList<RunningService.GPSCoordinate> getGpsdata() {
        return gpsdata;
    }

    public void setGpsdata(ArrayList<RunningService.GPSCoordinate> gpsdata) {
        this.gpsdata = gpsdata;
    }

    @TypeConverters(ArrayConverter.class) // add here
    @ColumnInfo(name = "gpsdata")
    private ArrayList<RunningService.GPSCoordinate> gpsdata;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public RunhistoryDB(int challengeID, long timestamp, long endtimestamp, ArrayList<RunningService.GPSCoordinate> gpsdata){
        this.challengeID=challengeID;
        this.timestamp = timestamp;
        this.endtimestamp = endtimestamp;
        this.gpsdata = gpsdata;
        this.id = 0;
    }


    public int getChallengeID() {
        return challengeID;
    }

    public void setChallengeID(int challengeID) {
        this.challengeID = challengeID;
    }
}

