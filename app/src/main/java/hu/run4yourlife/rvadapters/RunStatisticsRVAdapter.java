package hu.run4yourlife.rvadapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import hu.run4yourlife.MainActivity;
import hu.run4yourlife.R;
import hu.run4yourlife.RunTrackerActivity;
import hu.run4yourlife.SettingsActivity;
import hu.run4yourlife.SingleRunStatisticsActivity;
import hu.run4yourlife.database.RunhistoryDB;
import hu.run4yourlife.database.RunningDatabase;
import hu.run4yourlife.interfaces.Challenge;
import hu.run4yourlife.interfaces.Challenges;
import hu.run4yourlife.interfaces.Speedtrap;
import hu.run4yourlife.interfaces.StaticStuff;


/**
 * Levi
 */
public class RunStatisticsRVAdapter extends RecyclerView.Adapter<RunStatisticsRVAdapter.ViewHolder> {

    /*public interface OnItemClickCallback{
        void onItemClick(int id);
    }*/

    Context ctx;
    ///selected day of the past 7 days ( -6 is oldest, 0 is today)
    int selectedDay;
    private int currSelected;
    ArrayList<RunhistoryDB> runs = new ArrayList<>();
    RunningDatabase db;
    Speedtrap sp = new Speedtrap();

    public int getCurrSelected() {
        return currSelected;
    }
    private void getDataFromB(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db = Room.databaseBuilder(ctx.getApplicationContext(), RunningDatabase.class, StaticStuff.RUNDB_NAME).build();
                runs = (ArrayList<RunhistoryDB>) db.myDataBase().getUsers();
                //db.close();

                ///today zero
                Calendar date = new GregorianCalendar();
                // reset hour, minutes, seconds and millis
                date.set(Calendar.HOUR_OF_DAY, 0);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                date.set(Calendar.MILLISECOND, 0);
                ///selected day zero
                long selectedDayMillis = date.getTimeInMillis()+ selectedDay*24*3600*1000;
                Date old = new Date(selectedDayMillis);
                Log.i("DATE",old.toLocaleString());
                Calendar old_calendar = new GregorianCalendar();
                old_calendar.setTime(old);
                ///filter
                ArrayList<RunhistoryDB> valuesToRemove = new ArrayList<>();
                for (RunhistoryDB i :runs){
                    Calendar date_i = new GregorianCalendar();
                    date_i.setTime(new Date(i.getTimestamp()*1000));
                    if (old_calendar.get(Calendar.DAY_OF_YEAR) != date_i.get(Calendar.DAY_OF_YEAR)){
                        valuesToRemove.add(i);
                    }
                }
                runs.removeAll(valuesToRemove);



            }
        }).start();
    }

    public RunStatisticsRVAdapter(Context c, float selectedDay){
        this.ctx = c;
        this.selectedDay=Math.round(selectedDay)-6;
        try{
            getDataFromB();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_run, parent,false);
        ViewHolder vh = new ViewHolder(v);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int position = vh.getAdapterPosition();
                currSelected = vh.getAdapterPosition();
                RunStatisticsRVAdapter.super.notifyDataSetChanged();
                Intent it = new Intent(ctx, SingleRunStatisticsActivity.class);
                it.putExtra("runId",runs.get(currSelected).getId());
                ctx.startActivity(it);

                //cb.onItemClick(vh.getAdapterPosition());
                //Toast.makeText(ctx, "Clicked: " + vh.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        });
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RunhistoryDB d = runs.get(position);
        try {
            Challenges ch = new Challenges(ctx);
            Challenge temp = ch.getChallengeDetailsForID(d.getChallengeID());
            holder.tvname.setText(temp.getChallengeName());

            Date _date = new Date(d.getTimestamp()*1000);
            long difff = (d.getEndtimestamp() - d.getTimestamp());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String c = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.getDefault()).withZone(ZoneId.of("UTC"))
                        .format(Instant.ofEpochSecond(difff));
                holder.tvtime.setText(c.toString());
            }
            holder.tvdate.setText(_date.toLocaleString());

            holder.tvdist.setText(String.format("%d m", 111));

            if (position == currSelected){
                holder.view.setBackgroundColor(ContextCompat.getColor(ctx, R.color.secondaryColor));
            }else {
                holder.view.setBackgroundColor(Color.TRANSPARENT);
            }

            ArrayList<Double> distances = sp.CalcAllDistance(d.getGpsdata());
            holder.tvdist.setText(String.format("%.0f m", distances.get(distances.size() - 1)*1000));




        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return runs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvname, tvdate, tvdist, tvtime;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvname = itemView.findViewById(R.id.item_run_ch_name);
            tvdate = itemView.findViewById(R.id.item_run_date);
            tvdist = itemView.findViewById(R.id.item_run_distance);
            tvtime = itemView.findViewById(R.id.item_run_time);

            view = itemView.findViewById(R.id.item_run_view);

        }
    }
}
