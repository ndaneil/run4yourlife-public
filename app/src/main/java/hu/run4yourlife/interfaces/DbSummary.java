package hu.run4yourlife.interfaces;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.room.Room;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import hu.run4yourlife.database.RunhistoryDB;
import hu.run4yourlife.database.RunningDatabase;

/**
 * Dani+ Ferkó
 */
public class DbSummary{

    public ArrayList<Float> getSummaryFromDB(Context ctx){
        RunningDatabase db = Room.databaseBuilder(ctx.getApplicationContext(), RunningDatabase.class, StaticStuff.RUNDB_NAME).build();
        List<RunhistoryDB> rawdata = db.myDataBase().getUsers();
        ///time tester
        /*
        try {
            long difff = (rawdata.get(0).getEndtimestamp() - rawdata.get(0).getTimestamp());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String c = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()).withZone(ZoneId.of("UTC"))
                        .format(Instant.ofEpochSecond(difff));
                Log.i("onerun length", c.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        */

        /*rawdata.sort(new Comparator<RunhistoryDB>() {
            @Override
            public int compare(RunhistoryDB runhistoryDB, RunhistoryDB t1) {
                return (int) (-runhistoryDB.getTimestamp() + t1.getTimestamp());
            }
        });*/

        // today
        Calendar date = new GregorianCalendar();
        // reset hour, minutes, seconds and millis
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        long millis = date.getTimeInMillis()/1000;
        ArrayList<Float> wkdat = new ArrayList<>();
        long start = millis-6*24*3600;
        for (int i = 0; i < 7; i++){
            long finalStart = start;
            Integer val = rawdata.stream().filter(v -> v.getTimestamp() >= finalStart && v.getTimestamp() < finalStart +24*3600).map(v -> Math.round(v.getEndtimestamp()-v.getTimestamp())).reduce(Integer::sum).orElse(0);
            wkdat.add(val/60f);
            start += 24*3600;
        }
        return wkdat;
    }
}
