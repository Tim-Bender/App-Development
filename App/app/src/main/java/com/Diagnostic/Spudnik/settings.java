package com.Diagnostic.Spudnik;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Display various settings and options for the user.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 */
public class settings extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private UpdateDatabaseBroadcastReceiver broadcastReceiver;
    private String reportEmail;

    /**
     * Only thing out of the ordinary here in onCreate would be the switch's OnCheckedChangeListener.
     * This will toggle night and day mode for the entire app by pushing a boolean value into permanent storage via
     * shared preferences
     *
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolBar = findViewById(R.id.topAppBar);
        setSupportActionBar(myToolBar);

        setTitle("Settings");
        myToolBar.setTitleTextColor(Color.WHITE);
        firebaseDatabase = FirebaseDatabase.getInstance();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(UpdateDatabase.action);
        broadcastReceiver = new UpdateDatabaseBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);
        findViewById(R.id.settingsprogressbar).setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * Retrieve the latest up to date reportemail from firebase realtime database.
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onStart() {
        super.onStart();
        updateEmail(false);
    }

    /**
     *
     *
     * @param sendEmail Whether or not after retrieving the latest email it will auto send the user to the email client.
     */
    private void updateEmail(@NonNull final boolean sendEmail) {
        DatabaseReference reference = firebaseDatabase.getReference("settings").child("reportemail");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reportEmail = dataSnapshot.getValue(String.class);
                if (sendEmail)
                    sendEmail();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Report a bug button redirect, create an email with auto-filled fields
     *
     * @param view view
     * @since dev 1.0.0
     */
    public void reportBug(View view) {
        if (reportEmail != null)
            sendEmail();
        else
            updateEmail(true);
    }

    /**
     * Send bug report via email client.
     *
     * @since dev 1.0.0
     */
    private void sendEmail() {
        Display display = getWindowManager().getDefaultDisplay();  //get screen size
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        Intent emailIntent = new Intent(Intent.ACTION_SEND);  //create an email intent and fill in necessary information
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{reportEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Bug report Diagnostic Tool");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + " \nScreenSize:" + height + " x " + width + "\nAndroid Version: " +
                Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nPlease describe the bug in detail:\n");
        //start the intent and start an email
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    /**
     * Button redirect to the terms and conditons page
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void terms(View view) {
        Intent toTermsAndConditons = new Intent(getBaseContext(), termsofservice.class);
        startActivity(toTermsAndConditons);
    }

    /**
     * Update DataBase button redirect, see UpdateDatabase.java
     *
     * @param view view
     * @since dev 1.0.0
     */
    public void updateDataBase(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            new UpdateDatabase(this);
        else {
            ConstraintLayout layout = findViewById(R.id.settingsconstraintlayout);
            Snackbar.make(layout, "Please Sign In", Snackbar.LENGTH_SHORT).show();
        }

    }

    /**
     * Login button redirect
     *
     * @param view view
     * @since dev 1.0.0
     */
    public void login(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //if the user hasn't been authenticated, then we pass them to the login activity
        if (user == null) {
            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            i.putExtra("fromsettings", true);
            startActivity(i);
            finish();
        }
        //otherwise we assume it was a mistake
        else {
            ConstraintLayout layout = findViewById(R.id.settingsconstraintlayout);
            Snackbar.make(layout, "Already Signed In", Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Log out button redirect
     *
     * @param view view
     * @since dev 1.0.0
     */
    public void logout(View view) {
        //log the user out.
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ConstraintLayout layout = findViewById(R.id.settingsconstraintlayout);
        //if logged in, log out
        if (user != null) {
            auth.signOut();
            Snackbar.make(layout, "Signed Out", Snackbar.LENGTH_SHORT).show();
        }
        //else we assume it was a mistake.
        else
            Snackbar.make(layout, "Already Signed Out", Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Receives broadcasts from Updatedatabase and updates the ui accordingly
     *
     * @since dev 1.0.0
     */
    private class UpdateDatabaseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UpdateDatabase.action)) {
                if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_COMPLETE) {
                    findViewById(R.id.settingsspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .5f));
                    findViewById(R.id.settingsprogressbar).setVisibility(View.GONE);
                    Snackbar.make(findViewById(R.id.settingsconstraintlayout), "Update Complete: "
                            + intent.getIntExtra("updatedfiles", 0) + " Files Updated", Snackbar.LENGTH_SHORT).show();
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_FAILED) {
                    findViewById(R.id.settingsprogressbar).setVisibility(View.GONE);
                    findViewById(R.id.settingsspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .5f));
                    Snackbar.make(findViewById(R.id.settingsconstraintlayout), "Update Failed", Snackbar.LENGTH_SHORT).show();
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_BEGUN) {
                    findViewById(R.id.settingsprogressbar).setVisibility(View.VISIBLE);
                    findViewById(R.id.settingsspace).setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, .4f));
                } else if (intent.getIntExtra("data", 2) == UpdateDatabase.TERMS_OF_SERVICE_UPDATED)
                    Snackbar.make(findViewById(R.id.settingsconstraintlayout), "Updated Terms Of Service Available For Viewing", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * DEV MODE FEATURE, WILL BE REMOVED LATER
     *
     * @param view view
     * @since dev 1.0.0
     */
    public void testBluetooth(View view) {
        Intent i = new Intent(getBaseContext(), BluetoothTestActivity.class);
        startActivity(i);
    }


}
