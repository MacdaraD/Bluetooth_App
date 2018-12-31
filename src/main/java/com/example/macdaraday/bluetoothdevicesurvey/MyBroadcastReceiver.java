package com.example.macdaraday.bluetoothdevicesurvey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


class MyBroadcastReceiver extends BroadcastReceiver {
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private long timestamp;

    @Override
    public void onReceive(Context context, Intent intent) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        timestamp = System.currentTimeMillis();
        String ts = String.valueOf(timestamp);
        DatabaseReference myRef = database.getReference(ts);
        myRef.child(ts).setValue("Yes");
    }
}
