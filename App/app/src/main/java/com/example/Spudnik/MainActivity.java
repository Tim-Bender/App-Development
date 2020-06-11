package com.example.Spudnik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private vehicle myVehicle;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Loading");
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            myVehicle = new vehicle();

            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = preferences.edit();

            firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseStorage = FirebaseStorage.getInstance();
            DatabaseReference addressReference = firebaseDatabase.getReference("address");
            addressReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String newAddress = dataSnapshot.getValue(String.class);
                    Log.i(TAG,"New Address: " + newAddress);
                    editor.putString("ftpaddress",newAddress);
                    editor.commit();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            DatabaseReference passwordReference = firebaseDatabase.getReference("password");
            passwordReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String newPassword = dataSnapshot.getValue(String.class);
                    Log.i(TAG,"New Password: " + newPassword);
                    editor.putString("ftppassword",newPassword);
                    editor.commit();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            DatabaseReference passwordReference3 = firebaseDatabase.getReference("username");
            passwordReference3.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String newUsername = dataSnapshot.getValue(String.class);
                    Log.i(TAG,"New Username: " + newUsername);
                    editor.putString("ftpusername",newUsername);
                    editor.commit();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            progressBar = findViewById(R.id.loadingbar);
            progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            textView = findViewById(R.id.loadingText);
            ImageView image = findViewById(R.id.gifloadingscreen);
            Glide.with(this).load(R.drawable.heartbeatgiftransparent).into(image);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (progressStatus < 100) {
                        progressStatus += 1;
                        handler.post(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {
                                progressBar.setProgress(progressStatus);
                                textView.setText("Loading " + progressStatus + "%");
                            }
                        });
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    Intent i = new Intent(getBaseContext(), home.class);
                    i.putExtra("myvehicle",myVehicle);
                    startActivity(i);
                    finish();
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(this, "Boot Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }

        super.onStart();
    }

    public void nightMode(){
        ConstraintLayout constraints = findViewById(R.id.mainactivityconstraintlayout);
        constraints.setBackgroundColor(Color.parseColor("#333333"));
        TextView textView = findViewById(R.id.textView2);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.textView3);
        textView.setTextColor(Color.WHITE);
    }
}