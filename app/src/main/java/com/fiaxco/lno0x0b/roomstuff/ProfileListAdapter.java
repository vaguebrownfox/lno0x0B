package com.fiaxco.lno0x0b.roomstuff;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fiaxco.lno0x0b.R;

import java.util.Collections;
import java.util.List;

public class ProfileListAdapter extends RecyclerView.Adapter<ProfileListAdapter.ProfileViewHolder> {

    private OnProfileListener  mOnProfileListener;


    public static class ProfileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView profileItemView;
        private final TextView profileAge;
        private final TextView profileGender;

        OnProfileListener onProfileListener;

        public ProfileViewHolder(@NonNull View itemView, OnProfileListener onProfileListener) {
            super(itemView);
            profileItemView = itemView.findViewById(R.id.text_view_name);
            profileAge = itemView.findViewById(R.id.text_view_age);
            profileGender = itemView.findViewById(R.id.text_view_gender);

            this.onProfileListener =onProfileListener;

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onProfileListener.onProfileClick(getAdapterPosition());
        }
    }

    private final LayoutInflater mInflater;
    private List<Profile> mProfiles;

    public ProfileListAdapter(Context context, OnProfileListener onProfileListener) {
        mInflater = LayoutInflater.from(context);
        this.mOnProfileListener = onProfileListener;
    }


    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.calatoglist_item, parent, false);
        return new ProfileViewHolder(itemView, mOnProfileListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {

        if (mProfiles != null) {
            Profile current = mProfiles.get(position);
            ContentValues values = current.getProfileValues();
            holder.profileItemView.setText(values.getAsString(ProfileContract.ProfileEntry.NAME));
            holder.profileAge.setText(values.getAsString(ProfileContract.ProfileEntry.AGE));
            holder.profileGender.setText(ProfileContract.ProfileEntry.genderType(values.getAsInteger(ProfileContract.ProfileEntry.GENDER)));
        } else {
            holder.profileItemView.setText(R.string.no_name);
        }

    }

    public void setProfiles(List<Profile> profiles) {
        Collections.reverse(profiles);
        mProfiles = profiles;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mProfiles != null) {
            return mProfiles.size();
        } else { return 0;}
    }

    public interface OnProfileListener {
        void onProfileClick(int position);
    }

}
