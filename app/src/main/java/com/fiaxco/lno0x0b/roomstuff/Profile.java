package com.fiaxco.lno0x0b.roomstuff;


import android.content.ContentValues;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.fiaxco.lno0x0b.roomstuff.ProfileContract.ProfileEntry;

@Entity(tableName = ProfileEntry.TABLE_NAME)
public class Profile {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = ProfileEntry._ID)
    public Integer mId;

    @ColumnInfo(name = ProfileEntry.NAME)
    public String mName;

    @ColumnInfo(name = ProfileEntry.AGE)
    public Integer mAge;

    @ColumnInfo(name = ProfileEntry.GENDER)
    public Integer mGender;

    @ColumnInfo(name = ProfileEntry.HEIGHT)
    public Integer mHeight;

    @ColumnInfo(name = ProfileEntry.WEIGHT)
    public Integer mWeight;

    // Constructor
    public Profile(String name, Integer age, Integer gender, Integer height, Integer weight) {
        this.mName = name;
        this.mAge = age;
        this.mGender = gender;
        this.mHeight = height;
        this.mWeight = weight;
    }

    public ContentValues getProfileValues() {
        ContentValues values = new ContentValues();

        values.put(ProfileEntry._ID, this.mId);
        values.put(ProfileEntry.NAME, this.mName);
        values.put(ProfileEntry.AGE, this.mAge);
        values.put(ProfileEntry.GENDER, this.mGender);
        values.put(ProfileEntry.HEIGHT, this.mHeight);
        values.put(ProfileEntry.WEIGHT, this.mWeight);

        return values;
    }

}
