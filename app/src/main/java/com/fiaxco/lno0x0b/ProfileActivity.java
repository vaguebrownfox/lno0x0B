package com.fiaxco.lno0x0b;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Spinner;

import com.fiaxco.lno0x0b.R;

public class ProfileActivity extends AppCompatActivity {

    private EditText mEditTextName;
    private EditText mEditTextAge;
    private EditText mEditTextHeight;
    private EditText mEditTextWeight;
    private Spinner mGenderSpinner;

    private int mGender = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mEditTextName = findViewById(R.id.edit_view_profile_name);
        mEditTextAge = findViewById(R.id.edit_view_profile_age);
        mEditTextHeight = findViewById(R.id.edit_view_profile_height);
        mEditTextWeight = findViewById(R.id.edit_view_profile_weight);
        mGenderSpinner = findViewById(R.id.spinner_view_profile_gender);


    }
}