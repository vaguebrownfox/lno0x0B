package com.fiaxco.lno0x0b.roomstuff;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

class ProfileRepository {

    private ProfileDao mProfileDao;
    private LiveData<List<Profile>> mAllProfiles;

    ProfileRepository(Application application) {
        ProfileRoomDatabase db = ProfileRoomDatabase.getDatabase(application);

        mProfileDao = db.profileDao();
        mAllProfiles = mProfileDao.getAllProfiles();
    }

    LiveData<List<Profile>> getAllProfiles() {
        return mAllProfiles;
    }

    void insert(final Profile profile) {
        ProfileRoomDatabase.databaseWriteExecutor.execute(() -> {
            mProfileDao.insert(profile);
        });
    }

}
