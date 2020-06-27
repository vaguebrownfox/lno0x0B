package com.fiaxco.lno0x0b;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fiaxco.lno0x0b.roomstuff.Profile;
import com.fiaxco.lno0x0b.roomstuff.ProfileListAdapter;
import com.fiaxco.lno0x0b.roomstuff.ProfileViewModel;
import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class CatalogActivity extends AppCompatActivity implements ProfileListAdapter.OnProfileListener {

    // Where are my comments?
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

        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                     ItemTouchHelper.LEFT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Profile profile = adapter.getProfileAtPosition(position);
//                        if (direction == ItemTouchHelper.RIGHT) {
//                            //onProfileClick(position);
//                        } else if (direction == ItemTouchHelper.LEFT) {
//                            mProfileViewModel.delete(profile);
//                            Toast.makeText(CatalogActivity.this, "Deleting " +
//                                    profile.mName, Toast.LENGTH_LONG).show();
//                        }
                        mProfileViewModel.delete(profile);
                        Toast.makeText(CatalogActivity.this,
                                profile.mName + "'s profile deleted", Toast.LENGTH_LONG)
                                .show();

                    }
                }
        );

        helper.attachToRecyclerView(recyclerView);

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
                if (mList != null) {
                    TextView emptyListTV = findViewById(R.id.empty_list_text_view);
                    if (mList.size() < 1) {

                        emptyListTV.setVisibility(View.VISIBLE);
                    } else {
                        emptyListTV.setVisibility(View.INVISIBLE);
                    }
                }
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
            mProfileViewModel.deleteAll();
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
        //overridePendingTransition( R.anim.left_to_right,R.anim.right_to_left);
    }
}
