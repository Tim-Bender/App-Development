package com.Diagnostic.Spudnik;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;

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
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 *
 *
 * Welcome to the Settings activity.
 */
public class settings extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;

    /**
     * Only thing out of the ordinary here in onCreate would be the switch's OnCheckedChangeListener.
     * This will toggle night and day mode for the entire app by pushing a boolean value into permanent storage via
     * shared preferences
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar myToolBar = findViewById(R.id.topAppBar);
        setSupportActionBar(myToolBar);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        setTitle("Settings");
        myToolBar.setTitleTextColor(Color.WHITE);
        firebaseDatabase = FirebaseDatabase.getInstance();


    }
    /**
     * Report a bug button redirect, create an email with auto-filled fields
     * @param view view
     */

    public void reportBug(View view){
        try{
            DatabaseReference reference = firebaseDatabase.getReference("settings").child("reportemail");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String email = dataSnapshot.getValue(String.class);
                    //get screen size
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    int height = size.y;
                    //create an email intent and fill in necessary information
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Bug report Diagnostic Tool");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + " \nScreenSize:" + height +" x "+ width + "\nAndroid Version: " +
                            Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nPlease describe the bug in detail:\n");
                    //start the intent and start an email
                    startActivity(Intent.createChooser(emailIntent,"Send mail..."));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Its literally the same function as above. With a different email subject...
     * @param view view
     */

    public void submitFeedback(View view){
        try{
            DatabaseReference reference = firebaseDatabase.getReference("settings").child("reportemail");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String email = dataSnapshot.getValue(String.class);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");

                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Feedback Diagnostic Tool");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + "\nAndroid Version: " +
                            Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nComments: \n");
                    startActivity(Intent.createChooser(emailIntent,"Send mail..."));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } catch (Exception ignored) {
        }
    }

    /**
     * Update DataBase button redirect, for a description see MainActivity's comments, its the exact same function.
      * @param view view
     */

    public void updateDataBase(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        new UpdateDatabase(this);
        else {
            ConstraintLayout layout = findViewById(R.id.settingsconstraintlayout);
            Snackbar.make(layout, "Please Sign In", Snackbar.LENGTH_SHORT).show();
        }
    }


    /**
     * Login button redirect
     * @param view view
     */
    public void login(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //if the user hasn't been authenticated, then we pass them to the login activity
        if(user == null) {
            Intent i = new Intent(getBaseContext(), LoginActivity.class);
            i.putExtra("fromsettings", true);
            startActivity(i);
        }
        //otherwise we assume it was a mistake
        else{
            ConstraintLayout layout = findViewById(R.id.settingsconstraintlayout);
            Snackbar.make(layout, "Already Signed In", Snackbar.LENGTH_SHORT).show();
        }

    }

    /**
     * Log out button redirect
     * @param view view
     */
    public void logout(View view){
        //log the user out.
       FirebaseAuth auth = FirebaseAuth.getInstance();
       FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ConstraintLayout layout = findViewById(R.id.settingsconstraintlayout);
       //if logged in, log out
       if(user != null) {
           auth.signOut();
           Snackbar.make(layout, "Signed Out", Snackbar.LENGTH_SHORT).show();
       }
       //else we assume it was a mistake.
       else{
           Snackbar.make(layout, "Already Signed In", Snackbar.LENGTH_SHORT).show();
       }
    }

    /**
     * DEV MODE FEATURE, WILL BE REMOVED LATER
     * @param view view
     */
    public void testBluetooth(View view){
        try{
            Intent i = new Intent(getBaseContext(), BluetoothTestActivity.class);
            startActivity(i);
        }catch(Exception ignored){}
    }


}
