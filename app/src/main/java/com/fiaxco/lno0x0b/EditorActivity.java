package com.fiaxco.lno0x0b;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;

public class EditorActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "com.fiaxco.lno0x0b.EditorActivity.REPLY";

    private EditText mEditTextName;
    private EditText mEditTextAge;
    private EditText mEditTextHeight;
    private EditText mEditTextWeight;
    private Spinner mGenderSpinner;

    private int mGender = 0;

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
                    values.put(ProfileEntry.NAME, mEditTextName.getText().toString());
                    values.put(ProfileEntry.AGE, Integer.parseInt(mEditTextAge.getText().toString()));
                    values.put(ProfileEntry.GENDER, mGender);
                    values.put(ProfileEntry.HEIGHT, Integer.parseInt(mEditTextHeight.getText().toString()));
                    values.put(ProfileEntry.WEIGHT, Integer.parseInt(mEditTextWeight.getText().toString()));

                    replyIntent.putExtra(EXTRA_REPLY, values);
                    setResult(RESULT_OK, replyIntent);
                }
                finish();
                return true;
            case R.id.action_delete :
                //nothing
                return true;
            case android.R.id.home :
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}