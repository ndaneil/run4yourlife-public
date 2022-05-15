package hu.run4yourlife;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import hu.run4yourlife.rvadapters.MainRVAdapter;
import hu.run4yourlife.rvadapters.RunStatisticsRVAdapter;

public class RunStatisticsActivity extends AppCompatActivity {
    RecyclerView rv;
    RunStatisticsRVAdapter adapter;
    float selectedDay;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run_statistics);
        selectedDay = getIntent().getFloatExtra("day",-1f);
        Log.i("intent extra" , String.valueOf(selectedDay));
        rv = findViewById(R.id.runsRecycler);
        adapter = new RunStatisticsRVAdapter(this, selectedDay);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        rv.setAdapter(adapter);
        rv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }
}