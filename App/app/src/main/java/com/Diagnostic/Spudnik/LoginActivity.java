package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
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
 * Welcome to the login activity. This activity is responsible for logging the user in, validating that they have accepted the terms of service
 * performing a database update to ensure everything is up to date, and finally prebuilding the vehicle object.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 * @see UpdateDatabase
 * @see FirebaseAuth
 * @see vehicle
 */

public class LoginActivity extends AppCompatActivity {

    /**Firebase authentication object. Used to sign the user in*/
    private FirebaseAuth auth;
    /**Firebase user object.*/
    private FirebaseUser user;
    /**Textfield object that will store the input'ed email*/
    private TextInputEditText emailEditText;
    /**Textfield object that will store the input'ed password*/
    private TextInputEditText passwordEditText;
    /**Vehicle object. Will be prebuild and passed to home later. */
    private vehicle myvehicle;
    /**Control flow boolean, if we are logging in from the Settings page this becomes true and changes behavior upon exit.*/
    private boolean fromSettings;
    /**Used to ensure that they cannot double press the button and split the threads. Terms agreed is used to ensure that they have accepted the terms.*/
    private boolean pressed = false, termsAgreed = false;
    /**Constants used for control flow. */
    private final static int LOGGING_IN_BEGUN = 0, LOGGING_IN_COMPLETE = 1, LOGGING_IN_FAILURE = 2;
    /**Broadcast receiver used to receive updates from UpdateDatabase.class. Defined below as a subclass*/
    private UpdateDatabaseBroadcastReceiver broadcastReceiver;

    /**
     * Nothing special in the onCreate, just assigning instance fields and setting up the toolbar.
     * There is a redundancy check to see if the user is already logged in, if they are we send them to the home activity.
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();  //setup firebase user and auth
        user = auth.getCurrentUser();
        if(user != null)   //If they are already logged in, send them to the home activity. redundancy check
            finish();
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        setTitle("Sign In");
        toolbar.setTitleTextColor(Color.WHITE);
        emailEditText = findViewById(R.id.loginemailedittext);
        passwordEditText = findViewById(R.id.loginpasswordedittext);
        fromSettings = getIntent().getBooleanExtra("fromsettings",false);//a boolean value is used to determine where the user came from
        findViewById(R.id.loginprogressbar).setVisibility(View.GONE);

        TextView textView = findViewById(R.id.logintermsoftersivetextview); //now we will make the "Terms of Service" text blue
        Spannable spannable = new SpannableString(textView.getText().toString()); //new spannable
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE),22,textView.getText().length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set that section to blue
        textView.setText(spannable); //set the text

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION); //intent filter used to filter broadcasts
        filter.addAction(UpdateDatabase.action); //add the action we want to filter for
        broadcastReceiver = new LoginActivity.UpdateDatabaseBroadcastReceiver(); //make a new receiver
        registerReceiver(broadcastReceiver,filter); //register the receiver with our filter
    }

    /**
     * Mostly setting up listeners here.
     * @since dev 1.0.0
     */
    @Override
    protected void onStart(){
        super.onStart();
        final CheckBox acceptTermsCheckbox = findViewById(R.id.logincheckbox); //set the oncheckedchange listener for the accepttermscheckbox
        acceptTermsCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!pressed) //toggle the boolean
                    termsAgreed = isChecked;
                else
                    acceptTermsCheckbox.setChecked(true); //once they have accepted, and pressed login. they cannot un-accept the terms.
            }
        });
        TextView termsOfServiceTextView = findViewById(R.id.logintermsoftersivetextview); //set an onclick listener onto the text field. This will be used to redirect them
        termsOfServiceTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toTermsOfServiceIntent = new Intent(getBaseContext(),termsofservice.class);
                startActivity(toTermsOfServiceIntent); //send them to the terms of service activity
            }
        });
    }

    /**
     * Just unregister's the broadcast receiver on destroy so we avoid a memory leak.
     * @since dev 1.0.0
     */
    @Override
    protected void onDestroy(){
        unregisterReceiver(broadcastReceiver); //unregister it
        super.onDestroy();
    }

    /**
     * Pass the contents of the two edittext fields into a firebase authentication request.
     * If the task is successful, then we perform a database update, which will then trigger the next activity upon its completion
     * @param view view
     * @since dev 1.0.0
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
                                if (task.isSuccessful())  //if the task is successful, we begin a database update/
                                    new UpdateDatabase(LoginActivity.this);
                                else {
                                    //otherwise we inform them that authentication has failed.
                                    updateLoadingView(LOGGING_IN_FAILURE);
                                    Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) { //if we fail sign in then we inform then the same as above
                        pressed = false;
                        Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
                        updateLoadingView(LOGGING_IN_FAILURE);
                    }
                });
            }
            else //if they have no accepted the terms and conditions we ask them to
                Snackbar.make(findViewById(R.id.loginconstraintlayout),"Please Accept the Terms and Conditions",Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * This function will be used to send the user to the home activity.
     * @since dev 1.0.0
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

    /**
     * This method
     * @param code Constant code, defined above. Used for control flow
     * @since dev 1.0.0
     */
    public void updateLoadingView(@NonNull int code){
        switch(code) {
            case LOGGING_IN_COMPLETE: //if the login is complete then we hide the loading bar
                findViewById(R.id.loginprogressbar).setVisibility(View.GONE);
                findViewById(R.id.loginspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .55f));
                break;
            case LOGGING_IN_FAILURE: //if the login is a failure, we allow them to press the button again, and make the loading bar invisible
                pressed = false;
                findViewById(R.id.loginprogressbar).setVisibility(View.GONE);
                findViewById(R.id.loginspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .55f));
                break;
            case LOGGING_IN_BEGUN: //if the login process has begun then we make the loading bar visible
                findViewById(R.id.loginprogressbar).setVisibility(View.VISIBLE);
                findViewById(R.id.loginspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .45f));
        }
    }

    /**
     * Broadcast receiver to receive updates from UpdateDatabase.class. Upon the completion of the update, we will pre-build the vehicle object and send the users to the home activity.
     * @since dev 1.0.0
     */
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
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_FAILED) { //update the ui accordingly
                    updateLoadingView(LOGGING_IN_FAILURE);
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_BEGUN) { //update the ui accordingly
                    updateLoadingView(LOGGING_IN_BEGUN);
                }
            }
        }

    }



}
