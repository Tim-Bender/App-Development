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
        updateEmail(false);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(UpdateDatabase.action);
        broadcastReceiver = new UpdateDatabaseBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);
        findViewById(R.id.settingsprogressbar).setVisibility(View.GONE);
        findViewById(R.id.settingsupdatedatabasebutton).setOnClickListener((view) -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                new UpdateDatabase(this);
            else
                Snackbar.make(findViewById(R.id.settingsconstraintlayout), "Please Sign In", Snackbar.LENGTH_SHORT).show();
        });
        findViewById(R.id.settingsreportbugbutton).setOnClickListener((view) -> {
            if (reportEmail != null)
                sendEmail();
            else
                updateEmail(true);
        });
        findViewById(R.id.settingstermsbutton).setOnClickListener((view) -> startActivity(new Intent(getBaseContext(), termsofservice.class)));
        findViewById(R.id.settingsbluetoothbutton).setOnClickListener((view) -> startActivity(new Intent(getApplicationContext(), BluetoothTestActivity.class)));
        findViewById(R.id.settingssignoutbutton).setOnClickListener((view) -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                FirebaseAuth.getInstance().signOut();
                Snackbar.make(findViewById(R.id.settingsconstraintlayout), "Signed Out", Snackbar.LENGTH_SHORT).show();
            }
            //else we assume it was a mistake.
            else
                Snackbar.make(findViewById(R.id.settingsconstraintlayout), "Already Signed Out", Snackbar.LENGTH_SHORT).show();
        });
        findViewById(R.id.settingssigninbutton).setOnClickListener((view) -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class).putExtra("fromsettings", true));
                finish();
            } else {
                ConstraintLayout layout = findViewById(R.id.settingsconstraintlayout);
                Snackbar.make(layout, "Already Signed In", Snackbar.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    /**
     * @param sendEmail Whether or not after retrieving the latest email it will auto send the user to the email client.
     */
    private void updateEmail(@NonNull final boolean sendEmail) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("settings").child("reportemail");
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

}
