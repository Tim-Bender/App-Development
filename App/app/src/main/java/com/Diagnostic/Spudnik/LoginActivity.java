package com.Diagnostic.Spudnik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 *
 *
 * Welcome to the login activity...
 */

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private EditText emailEditText;
    private EditText passwordEditText;
    private SharedPreferences preferences;
    private int currentMode = 0;
    private Handler handler = new Handler();
    private vehicle myvehicle;
    private boolean fromSettings;


    /**
     * Nothing special in the onCreate, just assigning instance fields and setting up the toolbar.
     * There is a redundancy check to see if the user is already logged in, if they are we send them to the home activity.
     * @param savedInstanceState Bundle
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //setup firebase user and auth
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        //If they are already logged in, send them to the home activity
        if(user != null){
            finish();
        }
        //setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Login");
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);
        toolbar.setTitleTextColor(Color.WHITE);
        //grab the two edit text views
        emailEditText = findViewById(R.id.loginemailedittext);
        passwordEditText = findViewById(R.id.loginpasswordedittext);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        fromSettings = getIntent().getBooleanExtra("fromsettings",false);
        System.out.println("FROM SETTINGS " + fromSettings);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean nightmode = preferences.getBoolean("nightmode",false);
        int NIGHTMODE = 1, DAYMODE = 2;
        if(nightmode && currentMode != NIGHTMODE){
            nightMode();
            currentMode = NIGHTMODE;
        }
        else if(!nightmode && currentMode != DAYMODE){
            dayMode();
            currentMode = DAYMODE;
        }
    }

    /**
     * Pass the contents of the two edittext fields into a firebase authentication request.
     * If the task is successful, then we
     * @param view view
     */
    public void login(View view){
        auth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(),passwordEditText.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){ //if the task is successful, we welcome the user, assign the user variable, then update the database
                            Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                            user = auth.getCurrentUser();
                            if(fromSettings){
                                finish();
                            }
                            else{
                                myvehicle = new vehicle();
                                updateDataBase();
                                myvehicle.preBuildVehicleObject(getApplicationContext());
                                goToHome();
                            }
                        }
                        else{
                            //otherwise we inform them that authentication has failed.
                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    /**
     * Update database function, for comments see MainActivity's version. It's the exact same function.
     */
    public void updateDataBase(){
        if(user != null)
        new UpdateDatabase(this);
    }


    /**
     * This function will be used to send the user to the home activity.
     */
    private void goToHome(){
        if(user != null) {
            Intent i = new Intent(getBaseContext(), home.class);
            i.putExtra("myvehicle",myvehicle);
            startActivity(i);
        }
    }

    /**
     * nightMode toggle
     */
    public void nightMode(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.logintextview1);
                textView.setTextColor(Color.WHITE);
                textView = findViewById(R.id.logintextview2);
                textView.setTextColor(Color.WHITE);
                textView = findViewById(R.id.logintextviewtop);
                textView.setTextColor(Color.WHITE);
                EditText editText = findViewById(R.id.loginpasswordedittext);
                editText.setTextColor(Color.WHITE);
                editText = findViewById(R.id.loginemailedittext);
                editText.setTextColor(Color.WHITE);
                ConstraintLayout layout = findViewById(R.id.loginconstraintlayout);
                layout.setBackgroundColor(Color.parseColor("#333333"));
                Button button = findViewById(R.id.loginbutton);
                button.setBackgroundResource(R.drawable.nightmodebuttonselector);
                button.setTextColor(Color.WHITE);
            }
        });
    }

    /**
     * DayMode toggle
     */

    public void dayMode(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.logintextview1);
                textView.setTextColor(Color.BLACK);
                textView = findViewById(R.id.logintextview2);
                textView.setTextColor(Color.BLACK);
                textView = findViewById(R.id.logintextviewtop);
                textView.setTextColor(Color.BLACK);
                EditText editText = findViewById(R.id.loginpasswordedittext);
                editText.setTextColor(Color.BLACK);
                editText = findViewById(R.id.loginemailedittext);
                editText.setTextColor(Color.BLACK);
                ConstraintLayout layout = findViewById(R.id.loginconstraintlayout);
                layout.setBackgroundColor(Color.WHITE);
                Button button = findViewById(R.id.loginbutton);
                button.setBackgroundResource(R.drawable.daymodebuttonselector);
                button.setTextColor(Color.BLACK);
            }
        });
    }


}
