package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private FirebaseUser user;
    private vehicle myvehicle;

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

        progressBar = findViewById(R.id.loadingbar);
        progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

        textView = findViewById(R.id.textView3); //set the version name dynamically. This will be the version_name that is packaged during apk building.
        textView.setText(BuildConfig.VERSION_NAME);
        textView = findViewById(R.id.loadingText);
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
        super.onStart();
        if(user != null){ //if user is null, they are not logged in
           new UpdateDatabase(this); //attempt the database update
        }
        try{
            AsyncTask.execute(new Runnable() { //onto another thread we go
                @Override
                public void run() {
                    while (progressStatus < 100) { //lets loop until the progress bar is completely filled
                        progressStatus += 1;
                        handler.post(new Runnable() {//a handler is required to modify ui elements.
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() { //post the progress to the progress bar
                                progressBar.setProgress(progressStatus); //1 - 100
                                textView.setText("Loading " + progressStatus + "%");
                                switch (progressStatus){
                                    case 20:
                                    case 50:
                                    case 80:
                                        setTitle("Loading.");
                                        break;
                                    case 30:
                                    case 60:
                                    case 90:
                                        setTitle("Loading..");
                                        break;
                                    case 40:
                                    case 70:
                                    case 100:
                                        setTitle("Loading...");
                                        break;
                                }
                            }
                        });
                        try {
                            Thread.sleep(30); //Adjust this to give more time during loading. 30 = 3 seconds.. 40 = 4 seconds ect. Milliseconds divided by 100 above...
                        } catch (InterruptedException ignored) {}
                    }
                    Intent i;
                    if(user != null){ //if the user is already logged in, then we send them to the home screen
                        i = new Intent(getBaseContext(), home.class);
                        i.putExtra("myvehicle",myvehicle);
                    }
                    else{ //otherwise they get passed over to login
                        i = new Intent(getBaseContext(),LoginActivity.class);
                    }
                    startActivity(i);
                    finish();
                }
            });
        } catch (Exception ignored) {}
    }
}