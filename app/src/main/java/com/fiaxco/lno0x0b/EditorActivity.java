package com.fiaxco.lno0x0b;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;

public class EditorActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "com.fiaxco.lno0x0b.EditorActivity.REPLY";
    private static final String TAG = EditorActivity.class.getSimpleName();
    public static final String EDITOR_ACTIVITY_PROFILE_VALUE_EXTRA = "com.fiaxco.lno0x0b.EditorActivity.EXTRA_VALUE";

    private EditText mEditTextName;
    private EditText mEditTextAge;
    private EditText mEditTextHeight;
    private EditText mEditTextWeight;
    private Spinner mGenderSpinner;

    private int mGender = 0;

    private ContentValues mValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        mEditTextName = findViewById(R.id.edit_profile_name);
        mEditTextAge = findViewById(R.id.edit_profile_age);
        mEditTextHeight = findViewById(R.id.edit_profile_height);
        mEditTextWeight = findViewById(R.id.edit_profile_weight);
        mGenderSpinner = findViewById(R.id.spinner_gender);
        setupSpinner();


        Intent intent = getIntent();
        mValues = intent.getParcelableExtra(CatalogActivity.EDITOR_ACTIVITY_VALUE_EXTRA);
        if (mValues != null) {
            setTitle("Edit Profile");

            mEditTextName.setText(mValues.getAsString(ProfileEntry.NAME));
            // mEditTextName.setFilters(new InputFilter[] {filter});
            mEditTextAge.setText(mValues.getAsString(ProfileEntry.AGE));
            mEditTextHeight.setText(mValues.getAsString(ProfileEntry.HEIGHT));
            mEditTextWeight.setText(mValues.getAsString(ProfileEntry.WEIGHT));
            switch (mValues.getAsInteger(ProfileEntry.GENDER)) {
                case ProfileEntry.GENDER_MALE :
                    mGenderSpinner.setSelection(1);
                    break;
                case ProfileEntry.GENDER_FEMALE :
                    mGenderSpinner.setSelection(2);
                    break;
                default:
                    mGenderSpinner.setSelection(0);
                    break;
            }
            Button button = findViewById(R.id.button_editor_start_process);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(v -> {
                Log.d(TAG, "onClick: Start recording process");
                if (!checkForEmptyEditText()) {
                    Intent recordIntent = new Intent(EditorActivity.this, RecordActivity.class);
                    recordIntent.putExtra(EDITOR_ACTIVITY_PROFILE_VALUE_EXTRA, mValues);
                    startActivity(recordIntent);
                } else {
                    Toast.makeText(EditorActivity.this, "Please fill all the fields",
                            Toast.LENGTH_SHORT).show();
                }

            });
        }

    }


    private void setupSpinner() {
        ArrayAdapter<CharSequence> genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = ProfileEntry.GENDER_MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = ProfileEntry.GENDER_FEMALE;
                    } else {
                        mGender = ProfileEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0;
            }
        });
    }

      // filter for name edit text to avoid special characters
//    private InputFilter filter = (source, start, end, dest, dstart, dend) -> {
//        String blockCharacterSet = "~#^|$%&*!";
//        for (int i = start; i < end; i++) {
//            if (!Character.isLetterOrDigit(source.charAt(i))) {
//                return "";
//            }
//        }
//        return null;
//    };



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private boolean checkForEmptyEditText() {

        return TextUtils.isEmpty(mEditTextName.getText()) ||
                TextUtils.isEmpty(mEditTextAge.getText()) ||
                TextUtils.isEmpty(mEditTextHeight.getText()) ||
                TextUtils.isEmpty(mEditTextWeight.getText());

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save :
                Intent replyIntent = new Intent();
                if (checkForEmptyEditText()) {
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    ContentValues values = new ContentValues();
                    values.put(ProfileEntry.NAME, mEditTextName.getText().toString().trim());
                    values.put(ProfileEntry.AGE, Integer.parseInt(mEditTextAge.getText().toString()));
                    values.put(ProfileEntry.GENDER, mGender);
                    values.put(ProfileEntry.HEIGHT, Integer.parseInt(mEditTextHeight.getText().toString()));
                    values.put(ProfileEntry.WEIGHT, Integer.parseInt(mEditTextWeight.getText().toString()));

                    if (mValues != null) {
                        // Add id to profile for utilizing replace strategy of insert to update
                        values.put(ProfileEntry._ID, mValues.getAsInteger(ProfileEntry._ID));
                    }

                    replyIntent.putExtra(EXTRA_REPLY, values);
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
                return true;
            case R.id.action_delete :
                Toast.makeText(EditorActivity.this, "Go back and swipe left to delete",
                        Toast.LENGTH_LONG).show();
                return true;
            case android.R.id.home :
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}