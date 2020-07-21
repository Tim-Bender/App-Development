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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * @author timothy.bender
 * @version dev1.0.0
 *
 * Welcome to the login activity...
 */

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private vehicle myvehicle;
    private boolean fromSettings;
    private boolean pressed = false, termsAgreed = false;
    private final static int LOGGING_IN_BEGUN = 0, LOGGING_IN_COMPLETE = 1, LOGGING_IN_FAILURE = 2;
    private UpdateDatabaseBroadcastReceiver broadcastReceiver;
    private Handler handler;

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
        auth = FirebaseAuth.getInstance();  //setup firebase user and auth
        user = auth.getCurrentUser();
        if(user != null){   //If they are already logged in, send them to the home activity. redundancy check
            finish();
        }
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        setTitle("Sign In");
        toolbar.setTitleTextColor(Color.WHITE);
        emailEditText = findViewById(R.id.loginemailedittext);
        passwordEditText = findViewById(R.id.loginpasswordedittext);
        fromSettings = getIntent().getBooleanExtra("fromsettings",false);//a boolean value is used to determine where the user came from
        findViewById(R.id.loginprogressbar).setVisibility(View.GONE);
        TextView textView = findViewById(R.id.logintermsoftersivetextview);
        Spannable spannable = new SpannableString(textView.getText().toString());
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE),22,textView.getText().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(UpdateDatabase.action);
        broadcastReceiver = new LoginActivity.UpdateDatabaseBroadcastReceiver();
        this.registerReceiver(broadcastReceiver,filter);
        handler = new Handler();
    }

    @Override
    protected void onStart(){
        super.onStart();
        final CheckBox acceptTermsCheckbox = findViewById(R.id.logincheckbox);
        acceptTermsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!pressed)
                    termsAgreed = isChecked;
                else
                    acceptTermsCheckbox.setChecked(true);
            }
        });
        TextView termsOfServiceTextView = findViewById(R.id.logintermsoftersivetextview);
        termsOfServiceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toTermsOfServiceIntent = new Intent(getBaseContext(),termsofservice.class);
                startActivity(toTermsOfServiceIntent);
            }
        });
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }


    /**
     * Pass the contents of the two edittext fields into a firebase authentication request.
     * If the task is successful, then we send them on their way.
     * @param view view
     */
    public void login(View view){
        if(!pressed) {
            if(termsAgreed) {
                pressed = true;
                updateLoadingView(LOGGING_IN_BEGUN);
                auth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()) //pass the information into an authentication request
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) { //if the task is successful, we welcome the user, assign the user variable, then update the database
                                   new UpdateDatabase(LoginActivity.this);
                                } else {
                                    //otherwise we inform them that authentication has failed.
                                    updateLoadingView(LOGGING_IN_FAILURE);
                                    Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pressed = false;
                        Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
                        updateLoadingView(LOGGING_IN_FAILURE);
                    }
                });
            }
            else{
                Snackbar.make(findViewById(R.id.loginconstraintlayout),"Please Accept the Terms and Conditions",Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * This function will be used to send the user to the home activity.
     */
    private void goToHome(){
        if(user != null) { //redundancy check
            Intent i = new Intent(getBaseContext(), home.class);
            i.putExtra("myvehicle",myvehicle); //put the vehicle as a parcelable extra
            pressed = false;
            startActivity(i); //go to home
            finish();
        }
    }
    public void updateLoadingView(int code){
        switch(code) {
            case LOGGING_IN_COMPLETE:
                findViewById(R.id.loginspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .55f));
                findViewById(R.id.loginprogressbar).setVisibility(View.GONE);
                break;
            case LOGGING_IN_FAILURE:
                pressed = false;
                findViewById(R.id.loginprogressbar).setVisibility(View.GONE);
                findViewById(R.id.loginspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .55f));
                break;
            case LOGGING_IN_BEGUN:
                findViewById(R.id.loginprogressbar).setVisibility(View.VISIBLE);
                findViewById(R.id.loginspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .45f));
        }
    }

    private class UpdateDatabaseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UpdateDatabase.action)) {
                if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_COMPLETE) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    if (fromSettings)
                        finish(); //if they logged in from the settings page we will just close this page and send them back there
                    //otherwise they came from the loading screen.
                    myvehicle = new vehicle(); //create a new vehicle, since it couldn't have been done on loading
                    myvehicle.preBuildVehicleObject(getApplicationContext()); //prebuild the vehicle
                    updateLoadingView(LOGGING_IN_COMPLETE);
                    goToHome(); //go to the home activity
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_FAILED) {
                    updateLoadingView(LOGGING_IN_FAILURE);
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_BEGUN) {
                    updateLoadingView(LOGGING_IN_BEGUN);
                }
            }
        }

    }



}
