package hu.run4yourlife.interfaces;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Calendar;

public class UserData {

    private String name;
    private class birthdate{
        private int year;
        private int month;
        private int day;
        public birthdate(int _year,int _month,int _day){
            this.day=day;
            this.month=month;
            this.year=year;
        }
    }
    private birthdate birth_date;
    private String sex;
    private Integer bodyweight;
    //TODO make the advised_activity dynamic
    private final static int advised_activity=25;

    /*public UserData(){            //Ez a konstruktor csak a teszt miatt van benne
        this.setName("asdf");
        this.setBirth_date(2000,1,1);
        this.setBodyweight(66);
        this.setSex("F");
    }*/

    public UserData(Context context){
        SharedPreferences sh = context.getSharedPreferences("UserData", MODE_PRIVATE);
        this.name=sh.getString("name","");
        this.birth_date=new birthdate(sh.getInt("birthyear",0),sh.getInt("birthmonth",0),sh.getInt("birthday",0));
        this.sex=sh.getString("sex","");
        this.bodyweight=sh.getInt("bodyweight",0);

    }

    public void changeData(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        this.name=prefs.getString("name","");
        this.birth_date=new birthdate(Integer.valueOf(prefs.getString("year","")),Integer.valueOf(prefs.getString("month","")),Integer.valueOf(prefs.getString("day","")));
        this.sex=prefs.getString("sex","");
        this.bodyweight=Integer.valueOf(prefs.getString("weight",""));
    }

    public void saveData(Context context){
        SharedPreferences sh = context.getSharedPreferences("UserData", MODE_PRIVATE);
        SharedPreferences.Editor editor= sh.edit();
        editor.putString("name",this.name);
        editor.putInt("birthyear",this.birth_date.year);
        editor.putInt("birthmonth",this.birth_date.month);
        editor.putInt("birthday",this.birth_date.day);
        editor.putString("sex",this.sex);
        editor.putInt("bodyweight",this.bodyweight);
        editor.putInt("advised_activity",this.advised_activity);
        editor.commit();
    }
    public static int getAdvisedActivity(){return advised_activity;}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirth_date(int year,int month,int day){
        this.birth_date=new birthdate(year,month,day);
    }

    private Integer getAge(){
        Calendar rightnow=Calendar.getInstance();
        birthdate rightnow2= new birthdate(rightnow.get(Calendar.YEAR),rightnow.get(Calendar.MONTH),rightnow.get(Calendar.DAY_OF_MONTH));
        Integer age=(rightnow2.year-this.birth_date.year);
        if (rightnow2.month<=birth_date.month){
            if (rightnow2.day<birth_date.day){
                age--;
            }
        }
        return age;
    }

    public String getSex(){
        return this.sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * @param bodyweight in kg
     */
    public void setBodyweight(Integer bodyweight) {
        this.bodyweight = bodyweight;
    }

    public Integer getRecommendedExercisePerDay(){
        if (this.getAge()<18){
            return 60;
        }else if (this.getAge()<65){
            return 22;
        }else{
            return 22;
        }
    }



}
