package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


/**
 * @author timothy.bender
 * @version dev1.0.0
 * Welcome to the starting activity of the app. This activity will serve as a loading screen, an authentication check, and an automate database update.
 */
public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private FirebaseUser user;
    private vehicle myvehicle;
    private UpdateDatabaseBroadcastReceiver broadcastReceiver;

    /**
     * Setup the toolbar, this will be the same across all activities, and will thus only be mentioned here.
     * Assign values to instance fields, including SharedPreferences and firebath authentication/user.
     * @param savedInstanceState savedInstanceState
     */
    @SuppressLint({"CommitPrefEdits", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Loading");
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser(); //get the current user. if this is null, they aren't logged in

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(UpdateDatabase.action);
        broadcastReceiver = new UpdateDatabaseBroadcastReceiver();
        this.registerReceiver(broadcastReceiver,filter);

        TextView textView = findViewById(R.id.textView3); //set the version name dynamically. This will be the version_name that is packaged during apk building.
        textView.setText(BuildConfig.VERSION_NAME);
        myvehicle = new vehicle(); //create the first vehicle object.
    }

    /**
     * This method is primarily used to toggle between night and day mode. It also begins the gif animation.
     */
    @Override
    protected void onResume(){
        super.onResume();
        handler.post(new Runnable() {
            @Override
            public void run() {
                myvehicle.preBuildVehicleObject(getApplicationContext()); //try and pre-build the list of acceptable ids and dealer names.
                ImageView image = findViewById(R.id.gifloadingscreen); //image reference
                Glide.with(getApplicationContext()).load(R.drawable.heartbeatgiftransparent).into(image); //begin the gif animation
            }
        });
    }

    /**
     * This method will attempt to do database update, then begin updating the progress bar and also
     * Nested threads are used to achieve this. Since UpdateDataBase is an Asynchronous object it doesn't need to be inside of the Asnyc execution.
     */

    @Override
    protected void onStart() {
        super.onStart();//if user is null, they are not logged in//attempt the database update
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new UpdateDatabase(getApplicationContext());
            }
        },2000); //we will delay it so the loading screen isn't skipped entirely
    }

    /**
     * Unregister the broadcast receiver on destroy to avoid memory leak
     */
    @Override
    protected void onDestroy(){
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * Move to the next activity
     */
    private void go(){
        Intent i;
        if(user != null){ //if the user is already logged in, then we send them to the home screen
            i = new Intent(getBaseContext(), home.class);
            i.putExtra("myvehicle",myvehicle);
        }
        else //otherwise they get passed over to login
            i = new Intent(getBaseContext(),LoginActivity.class);
        startActivity(i);
        finish();
    }

    /**
     * Here's our broadcast receiver to receive updates from Updatedatabase.java
     */
    private class UpdateDatabaseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(UpdateDatabase.action))
               go();
        }
    }

}