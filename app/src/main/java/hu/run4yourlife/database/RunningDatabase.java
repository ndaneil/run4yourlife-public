package hu.run4yourlife.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities={RunhistoryDB.class},version = 1, exportSchema = false)
public abstract class RunningDatabase extends RoomDatabase {
    public abstract RunHistoryDao myDataBase();
}
