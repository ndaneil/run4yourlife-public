package hu.run4yourlife.rvadapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;

import hu.run4yourlife.MainActivity;
import hu.run4yourlife.R;
import hu.run4yourlife.RunTrackerActivity;
import hu.run4yourlife.interfaces.Challenge;
import hu.run4yourlife.interfaces.Challenges;
import hu.run4yourlife.interfaces.StaticStuff;

public class MainRVAdapter extends RecyclerView.Adapter<MainRVAdapter.ViewHolder> {

    /*public interface OnItemClickCallback{
        void onItemClick(int id);
    }*/

    Context ctx;

    private int currSelected = 0;

    public int getCurrSelected() {
        return currSelected;
    }

    ArrayList<Challenge> challenges = new ArrayList<>();
    Challenges ch;
    public MainRVAdapter(Context c){
        this.ctx = c;
        try{
            ch = new Challenges(c);
            challenges = ch.getChallenges();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_challenge, parent,false);
        ViewHolder vh = new ViewHolder(v);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //int position = vh.getAdapterPosition();
                currSelected = vh.getAdapterPosition();
                MainRVAdapter.super.notifyDataSetChanged();
                //cb.onItemClick(vh.getAdapterPosition());
                //Toast.makeText(ctx, "Clicked: " + vh.getAdapterPosition(), Toast.LENGTH_SHORT).show();
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Challenge d = challenges.get(position);
        holder.tvname.setText(d.getChallengeName());
        holder.tvdist.setText(Math.round(d.getMaxDist()) + " m");
        if (position == currSelected){
            holder.view.setBackgroundColor(ContextCompat.getColor(ctx, R.color.secondaryColor));
        }else {
            holder.view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public int getItemCount() {
        return challenges.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvname, tvdist;
        private View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvname = itemView.findViewById(R.id.item_challenge_name);
            tvdist = itemView.findViewById(R.id.item_challenge_distance);
            view = itemView.findViewById(R.id.item_challenge_view);

        }
    }
}
