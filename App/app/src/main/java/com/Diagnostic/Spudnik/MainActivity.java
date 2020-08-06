/*
 *
 *  Copyright (c) 2020, Spudnik LLc <https://www.spudnik.com/>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are not permitted in any form.
 *
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION, DEATH, or SERIOUS INJURY or DAMAGE)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.Diagnostic.Spudnik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.Diagnostic.Spudnik.CustomObjects.UpdateDatabase;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Loading screen, performs a database update in the background. Directs to login screen if not logged in, otherwise directs to home.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see Vehicle
 * @see UpdateDatabase
 * @see Glide
 * @since dev 1.0.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Handler, used for delayed UI updates and other multithreaded control
     */
    private Handler handler = new Handler();
    /**
     * Instance of our firebase user, retrieved from FireBaseAuth. If null: User not logged in
     */
    private FirebaseUser user;
    /**
     * Broadcast Receiver object. Defined below. Captures broadcasts from UpdateDatabase.class
     */
    private UpdateDatabaseBroadcastReceiver broadcastReceiver;
    /**
     * Boolean value used to make this activity thread safe, prevents an issue during handler delay causing activity split.
     */
    private boolean updateBegun = false;


    /**
     * Setup the toolbar, this will be the same across all activities, and will thus only be mentioned here.
     * Assign values to instance fields, including SharedPreferences and firebase authentication/user.
     *
     * @param savedInstanceState savedInstanceState
     * @since dev 1.0.0
     */
    @SuppressLint({"CommitPrefEdits", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Loading");
        Toolbar toolbar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        user = FirebaseAuth.getInstance().getCurrentUser(); //get the current user. if this is null, they aren't logged in

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION); //create a new intent filter for our broadcast listener
        filter.addAction(UpdateDatabase.action); //add the action inside of UpdateDatabase, we will listen for these broadcasts
        broadcastReceiver = new UpdateDatabaseBroadcastReceiver(); //create the broadcast receiver
        registerReceiver(broadcastReceiver, filter); //register the receiver with our broadcast filter.

        TextView textView = findViewById(R.id.textView3); //set the version name dynamically. This will be the version_name that is packaged during apk building.
        textView.setText(BuildConfig.VERSION_NAME); //set the text for version name
    }

    /**
     * This method is primarily used to toggle between night and day mode. It also begins the gif animation.
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onResume() {
        super.onResume();
        ImageView image = findViewById(R.id.gifloadingscreen); //image reference
        Glide.with(getApplicationContext()).load(R.drawable.heartbeatgiftransparent).into(image); //begin the gif animation
    }

    /**
     * This method will attempt to do database update, then begin updating the progress bar and also
     * Nested threads are used to achieve this. Since UpdateDataBase is an Asynchronous object it doesn't need to be inside of the Asnyc execution.
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onStart() {
        super.onStart();//if user is null, they are not logged in
        Intent startUpdateIntent = new Intent(this, UpdateDatabase.class);
        startService(startUpdateIntent);
    }


    /**
     * Unregister the broadcast receiver on destroy to avoid memory leak
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver); //unregister the receiver
        super.onDestroy();
    }

    /**
     * Move to the next activity. If the user is not logged in, aka !null then we direct them to home. Else they go to login
     *
     * @since dev 1.0.0
     */
    private void go() {
        if (!(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        Intent i = (user != null) ? new Intent(getApplicationContext(),Home.class) : new Intent(getApplicationContext(),LoginActivity.class);
        startActivity(i); //start the selected activity
        finish(); //if the user tries to go back to this activity, close and exit the app
    }


    /**
     * Here's our broadcast receiver to receive updates from Updatedatabase.java
     *
     * @since dev 1.0.0
     */
    private class UpdateDatabaseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UpdateDatabase.action)) {
                if (intent.getIntExtra("data", 0) == UpdateDatabase.UPDATE_COMPLETE)
                    updateBegun = true; //protection against multiple threads branching off, resulting in multiple home screens
                else if (!updateBegun) { //else, if there isnt already a thread running here then we delay moving to the home screen for now.
                    handler.postDelayed(MainActivity.this::go, 2000);
                }
            }
        }
    }

}