package hu.run4yourlife.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * Dani
 */
@Dao
public interface RunHistoryDao {
    @Insert
    public void insertRun(RunhistoryDB run);

    @Query("select * from FUTASOK")
    public List<RunhistoryDB> getUsers();

    @Delete
    public void deleteRun(RunhistoryDB user);
}