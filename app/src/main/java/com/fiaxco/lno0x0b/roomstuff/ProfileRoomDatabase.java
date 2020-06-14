package com.fiaxco.lno0x0b.roomstuff;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;

@Database(entities = {Profile.class}, version = 1, exportSchema = false)
public abstract class ProfileRoomDatabase extends RoomDatabase {

    public abstract ProfileDao profileDao();

    private static final int NUMBER_OF_THREADS = 4;
    private static volatile ProfileRoomDatabase INSTANCE;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static ProfileRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ProfileRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ProfileRoomDatabase.class, ProfileEntry.DATABASE_NAME)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            databaseWriteExecutor.execute(() -> {
                ProfileDao dao = INSTANCE.profileDao();
                //dao.deleteAllProfiles();

                Profile profile = new Profile("Han Solo", 37, 1, 186, 82);
                dao.insert(profile);

                profile = new Profile("Chewbacca", 40, 1, 192, 90);
                dao.insert(profile);
            });
        }
    };

}
