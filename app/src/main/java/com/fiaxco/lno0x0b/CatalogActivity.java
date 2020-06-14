package com.fiaxco.lno0x0b;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fiaxco.lno0x0b.roomstuff.Profile;
import com.fiaxco.lno0x0b.roomstuff.ProfileListAdapter;
import com.fiaxco.lno0x0b.roomstuff.ProfileViewModel;
import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CatalogActivity extends AppCompatActivity implements ProfileListAdapter.OnProfileListener {

    private static final int EDITOR_ACTIVITY_REQUEST_CODE = 1;
    private static final int EDITOR_ACTIVITY_UPDATE_REQUEST_CODE = 2;

    private static final String TAG = CatalogActivity.class.getSimpleName();

    public static final String EDITOR_ACTIVITY_VALUE_EXTRA = "com.fiaxco.lno0x0b.CatalogActivity.VALUE";

    private ProfileViewModel mProfileViewModel;

    private List<Profile> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Recycler view and its Adapter
        RecyclerView recyclerView = findViewById(R.id.main_profile_list);
        final ProfileListAdapter adapter = new ProfileListAdapter(this, this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        // View model
        mProfileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Add observer for live data returned by view model to observer
        mProfileViewModel.getAllProfiles().observe(this, new Observer<List<Profile>>() {
            @Override
            public void onChanged(List<Profile> profiles) {
                mList = profiles;
                adapter.setProfiles(profiles);

            }
        });

        // Floating action button
        FloatingActionButton fab = findViewById(R.id.floatingActionButton_catalog_add_profile);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
            startActivityForResult(intent, EDITOR_ACTIVITY_REQUEST_CODE);
        });
    }

    private Profile handleResult(ContentValues values) {
        assert values != null;
        String name = values.getAsString(ProfileEntry.NAME);
        Integer age = values.getAsInteger(ProfileEntry.AGE);
        Integer gender = values.getAsInteger(ProfileEntry.GENDER);
        Integer height = values.getAsInteger(ProfileEntry.HEIGHT);
        Integer weight = values.getAsInteger(ProfileEntry.WEIGHT);

        Log.d("CatalogActivityLol", name+age+gender+height+weight);

        return new Profile(name, age, gender, height, weight);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDITOR_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            ContentValues values;
            assert data != null;
            values = data.getParcelableExtra(EditorActivity.EXTRA_REPLY);

            assert values != null;
            Profile profile = handleResult(values);
            mProfileViewModel.insert(profile);

        } else if(requestCode == EDITOR_ACTIVITY_UPDATE_REQUEST_CODE && resultCode == RESULT_OK) {

            ContentValues values;
            assert data != null;
            values = data.getParcelableExtra(EditorActivity.EXTRA_REPLY);

            assert values != null;
            Profile profile = handleResult(values);
            profile.mId = values.getAsInteger(ProfileEntry._ID);
            mProfileViewModel.insert(profile);

        } else {
            Toast.makeText(getApplicationContext(), "Not saved", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all_entries) {
            // delete all function
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onProfileClick(int position) {
        // go to new activity here
        Log.d(TAG, "onProfileClick: " + position);
        Profile profile = mList.get(position);

        Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
        intent.putExtra(EDITOR_ACTIVITY_VALUE_EXTRA, profile.getProfileValues());
        startActivityForResult(intent, EDITOR_ACTIVITY_UPDATE_REQUEST_CODE);
    }
}
