package com.fiaxco.lno0x0b.roomstuff;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ProfileViewModel extends AndroidViewModel {

    private ProfileRepository mRepository;

    private LiveData<List<Profile>> mAllProfiles;

    public ProfileViewModel (Application application) {
        super(application);

        mRepository = new ProfileRepository(application);
        mAllProfiles = mRepository.getAllProfiles();
    }

    public LiveData<List<Profile>> getAllProfiles() {
        return mAllProfiles;
    }

    public void insert(Profile profile) {
        mRepository.insert(profile);
    }

    public void delete(Profile profile) {
        mRepository.delete(profile);
    }

    public void deleteAll() {
        mRepository.deleteAll();
    }
}
