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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.Diagnostic.Spudnik.CustomObjects.UpdateDatabase;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Login the user, ensure that the terms of service are accepted and perform a database update.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see UpdateDatabase
 * @see FirebaseAuth
 * @see Vehicle
 * @since dev 1.0.0
 */

public class LoginActivity extends AppCompatActivity {

    /**
     * Used to sign the user in
     */
    private FirebaseAuth auth;
    /**
     * Firebase user
     */
    private FirebaseUser user;
    /**
     * Store the input'ed email
     */
    private TextInputEditText emailEditText;
    /**
     * Store the input'ed password
     */
    private TextInputEditText passwordEditText;
    /**
     * Control flow boolean, if we are logging in from the Settings page this becomes true and changes behavior upon exit.
     */
    private boolean fromSettings;
    /**
     * Used to ensure that they cannot double press the button and split the threads. Terms agreed is used to ensure that they have accepted the terms.
     */
    private boolean pressed = false, termsAgreed = false;
    /**
     * Constants used for control flow.
     */
    private final static int LOGGING_IN_BEGUN = 0, LOGGING_IN_COMPLETE = 1, LOGGING_IN_FAILURE = 2;
    /**
     * Broadcast receiver used to receive updates from UpdateDatabase.class. Defined below as a subclass
     */
    private UpdateDatabaseBroadcastReceiver broadcastReceiver;

    /**
     * Assigning instance fields and setting up the toolbar.
     *
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
        if (user != null)   //If they are already logged in, send them to the home activity. redundancy check
            finish();
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        setTitle("Sign In");
        toolbar.setTitleTextColor(Color.WHITE);
        emailEditText = findViewById(R.id.loginemailedittext);
        passwordEditText = findViewById(R.id.loginpasswordedittext);
        fromSettings = getIntent().getBooleanExtra("fromsettings", false);//a boolean value is used to determine where the user came from
        findViewById(R.id.loginprogressbar).setVisibility(View.GONE);

        TextView textView = findViewById(R.id.logintermsoftersivetextview); //now we will make the "Terms of Service" text blue
        Spannable spannable = new SpannableString(textView.getText().toString()); //new spannable
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 22, textView.getText().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); //set that section to blue
        textView.setText(spannable); //set the text

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION); //intent filter used to filter broadcasts
        filter.addAction(UpdateDatabase.action); //add the action we want to filter for
        broadcastReceiver = new LoginActivity.UpdateDatabaseBroadcastReceiver(); //make a new receiver
        registerReceiver(broadcastReceiver, filter); //register the receiver with our filter
    }

    /**
     * Set up action listeners
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onStart() {
        super.onStart();
        final CheckBox acceptTermsCheckbox = findViewById(R.id.logincheckbox); //set the oncheckedchange listener for the accepttermscheckbox
        acceptTermsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!pressed) //toggle the boolean
                termsAgreed = isChecked;
            else
                acceptTermsCheckbox.setChecked(true); //once they have accepted, and pressed login. they cannot un-accept the terms.
        });
        TextView termsOfServiceTextView = findViewById(R.id.logintermsoftersivetextview); //set an onclick listener onto the text field. This will be used to redirect them
        termsOfServiceTextView.setOnClickListener(v -> {
            Intent toTermsOfServiceIntent = new Intent(getBaseContext(), TermsOfService.class);
            startActivity(toTermsOfServiceIntent); //send them to the terms of service activity
        });
    }

    /**
     * Just unregister's the broadcast receiver on destroy so we avoid a memory leak.
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver); //unregister it
        super.onDestroy();
    }

    /**
     * Pass the contents of the two edittext fields into a firebase authentication request.
     * If the task is successful, then we perform a database update, which will then trigger the next activity upon its completion
     *
     * @param view view
     * @since dev 1.0.0
     */
    public void login(View view) {
        if (!pressed) {
            if (termsAgreed) {
                pressed = true;
                updateLoadingView(LOGGING_IN_BEGUN);
                auth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(), passwordEditText.getText().toString().trim()) //pass the information into an authentication request
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {  //if the task is successful, we begin a database update/
                                Intent startUpdateIntent = new Intent(this, UpdateDatabase.class);
                                startService(startUpdateIntent);
                            }
                            else {
                                //otherwise we inform them that authentication has failed.
                                updateLoadingView(LOGGING_IN_FAILURE);
                                Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> { //if we fail sign in then we inform then the same as above
                            pressed = false;
                            Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
                            updateLoadingView(LOGGING_IN_FAILURE);
                        });
            } else //if they have no accepted the terms and conditions we ask them to
                Snackbar.make(findViewById(R.id.loginconstraintlayout), "Please Accept the Terms and Conditions", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Send the user to the home activity.
     *
     * @since dev 1.0.0
     */
    private void goToHome() {
        Intent i = new Intent(getBaseContext(), Home.class);
        pressed = false;
        startActivity(i); //go to home
        finish();
    }

    /**
     * Update the UI depending on broadcasts received from updatedatabase.
     *
     * @param code Constant code, defined above. Used for control flow
     * @since dev 1.0.0
     */
    public void updateLoadingView(@NonNull int code) {
        switch (code) {
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
     * Receive updates from UpdateDatabase.class. Upon the completion of the update, we will pre-build the vehicle object and send the users to the home activity.
     *
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
                    updateLoadingView(LOGGING_IN_COMPLETE);
                    goToHome(); //go to the home activity
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_FAILED)  //update the ui accordingly
                    updateLoadingView(LOGGING_IN_FAILURE);
                else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_BEGUN)  //update the ui accordingly
                    updateLoadingView(LOGGING_IN_BEGUN);

            }
        }

    }

}
