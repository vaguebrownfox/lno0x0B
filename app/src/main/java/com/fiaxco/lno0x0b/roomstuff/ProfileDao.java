package com.fiaxco.lno0x0b.roomstuff;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;

import java.util.List;

@Dao
public interface ProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Profile profile);

    @Query("SELECT * FROM " + ProfileEntry.TABLE_NAME)
    LiveData<List<Profile>> getAllProfiles();

    @Query("DELETE FROM " + ProfileEntry.TABLE_NAME)
    void deleteAllProfiles();

}
