package hu.run4yourlife.interfaces;

import android.content.Context;
import android.os.Message;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import android.content.res.Resources;
import android.util.Log;

import hu.run4yourlife.R;

/**
 * Kinga+Matyi
 */
public class Challenges extends JSONObject {
    private ArrayList<Challenge> ChallengeList;

    public Challenges(Context context ) throws IOException {
        // Creates an InputStream
        InputStream input = context.getResources().openRawResource(R.raw.challenges);
        String in = readStream(input);
        Log.i("MyLog", in);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Challenge>>(){}.getType();
        ChallengeList = gson.fromJson(in, type);
        Log.i("Barmi","Barmi2");
        //fillData(in);
    }

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


    public ArrayList<String> readStringArray(JSONArray JA) throws IOException {
        ArrayList<String> strings = new ArrayList<String>();

        for (int i = 0 ; i <JA.length();++i) {
            try {
                strings.add(JA.get(i).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return strings;
    }

    public ArrayList<Double> readDoublesArray(JSONArray JA) throws IOException {
        ArrayList<Double> doubles = new ArrayList<Double>();
        for (int i = 0 ; i <JA.length();++i) {
            try {
                String s = JA.get(i).toString();
                doubles.add(Double.parseDouble(s));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return doubles;
    }

    public ArrayList<Integer> readIntArray(JSONArray JA) throws IOException {
        ArrayList<Integer> ints = new ArrayList<Integer>();

        for (int i = 0 ; i <JA.length();++i) {
            try {
                String s  = JA.get(i).toString();
                ints.add(Integer.parseInt(s));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ints;
    }

    public ArrayList<Challenge> getChallenges(){
        return ChallengeList;
    }
    public Challenge getChallengeDetailsForID(int id){
        return ChallengeList.get(id);
    }

    @Override
    public String toString() {
        return "Challenges{" +
                "ChallengeList=" + ChallengeList +
                '}';
    }
}