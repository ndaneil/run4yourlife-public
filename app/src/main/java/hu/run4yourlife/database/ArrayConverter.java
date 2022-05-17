package hu.run4yourlife.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

import hu.run4yourlife.RunningService;
/**
 * Dani
 * */
public class ArrayConverter implements Serializable {
    @TypeConverter // note this annotation
    public String fromOptionValuesList(ArrayList<RunningService.GPSCoordinate> optionValues) {
        if (optionValues == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<RunningService.GPSCoordinate>>() {
        }.getType();
        String json = gson.toJson(optionValues, type);
        return json;
    }

    @TypeConverter // note this annotation
    public ArrayList<RunningService.GPSCoordinate> toOptionValuesList(String optionValuesString) {
        if (optionValuesString == null) {
            return (null);
        }
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<RunningService.GPSCoordinate>>() {
        }.getType();
        ArrayList<RunningService.GPSCoordinate> productCategoriesList = gson.fromJson(optionValuesString, type);
        return productCategoriesList;
    }
}
