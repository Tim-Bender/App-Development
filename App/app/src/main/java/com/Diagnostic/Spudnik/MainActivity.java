package com.Diagnostic.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


/**
 * Welcome to the starting activity of the app. This activity will serve as a loading screen, an authentication check, and an automate database update.
 */
public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private FirebaseUser user;
    private int currentMode = 0;
    private SharedPreferences preferences;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);

        //setup shared preferences and firebase auth
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        progressBar = findViewById(R.id.loadingbar);
        progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        //set the version code
        textView = findViewById(R.id.textView3);
        textView.setText(BuildConfig.VERSION_NAME);
        textView = findViewById(R.id.loadingText);
        myvehicle = new vehicle();
        myvehicle.preBuildVehicleObject(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        handler.post(new Runnable() {
            @Override
            public void run() {
                boolean nightmode = preferences.getBoolean("nightmode",false);
                int NIGHTMODE = 1;
                if(nightmode && currentMode != NIGHTMODE){
                    nightMode();
                    currentMode = NIGHTMODE;
                }
                ImageView image = findViewById(R.id.gifloadingscreen);
                Glide.with(getApplicationContext()).load(R.drawable.heartbeatgiftransparent).into(image);
            }
        });
    }

    /**
     * This method will toggle night and day mode, initiate gif glide,
     * then begin asynchronous background tasks involving database construction, and loading bar updates
     */

    @Override
    protected void onStart() {
        super.onStart();
        //Night/Day Mode Toggle
        //If the firebase user != null, then they are authenticated and we can attempt a database update.
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if(user != null){
                    updateDataBase();
                }
            }
        });
        //GIF Glide animation begin
        //Begin Asynchronous threading
        try{
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //Continuously update the loading bar

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
                    Intent i;
                    if(user != null){
                        i = new Intent(getBaseContext(), home.class);
                        i.putExtra("myvehicle",myvehicle);
                    }
                    else{
                        i = new Intent(getBaseContext(),LoginActivity.class);
                    }
                    startActivity(i);
                    finish();
                    //Otherwise they are directed towards the login activity.

                }
            });
        } catch (Exception ignored) {
        }
    }
    /**
     * NightMode Toggle
     * Most activities will have a nightmode and a daymode toggle, so that they might be switched in the middle
     * of runtime, however since we are only showing the loading screen once, it does not need a daymode toggle.
     */
    public void nightMode(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                ConstraintLayout constraints = findViewById(R.id.mainactivityconstraintlayout);
                constraints.setBackgroundColor(Color.parseColor("#333333"));
                TextView textView = findViewById(R.id.textView2);
                textView.setTextColor(Color.WHITE);
                textView = findViewById(R.id.textView3);
                textView.setTextColor(Color.WHITE);
            }
        });

    }

    /**
     * Asynchronous database construction and updatability. Connects to firebase's server,
     * iterates through all items in the bucket, and downloads needed files, replacing the old ones.
     * It will also update the list of acceptable vehicle id numbers stored in com.example.Spudnik/files/machineids
     */

    public void updateDataBase() {
        new UpdateDatabase(this);
    }

}