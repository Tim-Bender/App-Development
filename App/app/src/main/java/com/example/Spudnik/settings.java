package com.example.Spudnik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
    private static final String TAG = settings.class.getSimpleName();
    private FirebaseStorage firebaseStorage;
    private Switch aSwitch;
    boolean nightmode = false;
    SharedPreferences preferences;
    private SharedPreferences.Editor editor;

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

        Toolbar myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        setTitle("Settings");
        myToolBar.setTitleTextColor(Color.WHITE);

        aSwitch = findViewById(R.id.settingsToggle);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        firebaseStorage = FirebaseStorage.getInstance();

        //set a listener to the nightmode switch button.
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && !nightmode){
                    nightmode = true;
                    nightMode();
                    editor.putBoolean("nightmode",true);
                    editor.apply();
                }
                if(!isChecked && nightmode){
                    nightmode = false;
                    dayMode();
                    editor.putBoolean("nightmode",false);
                    editor.commit();
                }
            }
        });

    }

    /**
     * Check if its in day or night mode.
     */

    @Override
    protected void onStart(){
        super.onStart();
        nightmode = preferences.getBoolean("nightmode",false);
        if(nightmode){
            nightMode();
            aSwitch.setChecked(true);
        }
        else{
            dayMode();
        }
    }

    /**
     * Report a bug button redirect, create an email with auto-filled fields
     * @param view view
     */

    public void reportBug(View view){
        try{
            //get screen size
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            //create an email intent and fill in necessary information
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            //THIS IS WHERE YOU CHANGE THE DESTINATION EMAIL ADDRESS!!!!!!!!
            //Dev note: Might switch this to database grab?
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"timothy.bender@spudnik.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Bug report Diagnostic Tool");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + " \nScreenSize:" + height +" x "+ width + "\nAndroid Version: " +
                                                    Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nPlease describe the bug in detail:\n");
            //start the intent and start an email
            startActivity(Intent.createChooser(emailIntent,"Send mail..."));
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
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");

            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"timothy.bender@spudnik.com"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Feedback Diagnostic Tool");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Device: " + Build.DEVICE + "\nAndroid Version: " +
                    Build.VERSION.CODENAME + " " + Build.VERSION.RELEASE + "\n\nComments: \n");
            startActivity(Intent.createChooser(emailIntent,"Send mail..."));
        } catch (Exception ignored) {
        }
    }

    /**
     * Update DataBase button redirect, for a description see MainActivity's comments, its the exact same function.
      * @param view view
     */

    public void updateDataBase(View view){
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        boolean isConnected = activeNetwork != null;
       if(isConnected) {
           StorageReference reference = firebaseStorage.getReference().getRoot();

           final File rootpath = new File(getFilesDir(), "database");
           File temp1 = new File(getFilesDir(), "");
           File temp2 = new File(temp1, "machineids");
           temp2.delete();

           if (!rootpath.exists()) {
               Log.i(TAG, "Folder Created: " + rootpath.mkdirs());
           }
           reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
               @Override
               public void onSuccess(ListResult listResult) {
                   for (final StorageReference item : listResult.getItems()) {
                       final int numberOfFiles = listResult.getItems().size();
                       final int[] fileNumber = { 1 };
                       final File localFile = new File(rootpath, item.getName());
                       item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                           @Override
                           public void onSuccess(StorageMetadata storageMetadata) {
                               if (localFile.lastModified() < storageMetadata.getUpdatedTimeMillis()) {
                                   Log.i(TAG, "File deleted " + localFile.delete());
                                   item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                       @Override
                                       public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                           Log.i(TAG, "ItemName " + item.getName());

                                           File root = new File(getFilesDir(), "");
                                           FileWriter fw;
                                           File toEdit = new File(root, "machineids");
                                           try {
                                               String line = "", toPrint;
                                               if (toEdit.exists()) {
                                                   BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(toEdit)));
                                                   line = reader.readLine();
                                               }
                                               String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("machine", "") + ",";
                                               toPrint = (line != null) ? line + editedItemName : editedItemName;
                                               fw = new FileWriter(toEdit);
                                               fw.append(toPrint);
                                               fw.flush();
                                               fw.close();
                                               fileNumber[0]++;
                                               if(fileNumber[0] == numberOfFiles){
                                                   Toast.makeText(settings.this, "Update Complete", Toast.LENGTH_SHORT).show();
                                               }
                                           } catch (IOException e) {
                                               e.printStackTrace();
                                           }
                                       }
                                   });
                               } else if (localFile.lastModified() > storageMetadata.getUpdatedTimeMillis()) {
                                   File root = new File(getFilesDir(), "");
                                   FileWriter fw;
                                   File toEdit = new File(root, "machineids");
                                   try {
                                       String line = "", toPrint;
                                       if (toEdit.exists()) {
                                           BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(toEdit)));
                                           line = reader.readLine();
                                       }
                                       String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("machine", "") + ",";
                                       toPrint = (line != null) ? line + editedItemName : editedItemName;
                                       fw = new FileWriter(toEdit);
                                       fw.append(toPrint);
                                       fw.flush();
                                       fw.close();
                                       fileNumber[0]++;
                                       if(fileNumber[0] == numberOfFiles){
                                           Toast.makeText(settings.this, "Update Complete", Toast.LENGTH_SHORT).show();
                                       }
                                   } catch (IOException e) {
                                       e.printStackTrace();
                                   }


                               }
                           }
                       });
                   }
                   editor.putBoolean("databaseupdated", true);
                   editor.commit();
               }

           }).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
               }
           });
       }
       else{
           Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Already Logged In", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Log out button redirect
     * @param view view
     */
    public void logout(View view){
        //log the user out.
       FirebaseAuth auth = FirebaseAuth.getInstance();
       FirebaseUser user = auth.getCurrentUser();
       //if logged in, log out
       if(user != null) {
           auth.signOut();
           Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
       }
       //else we assume it was a mistake.
       else{
           Toast.makeText(this, "Already Signed In", Toast.LENGTH_SHORT).show();
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

    /**
     * NightMode Toggle
     */
    public void nightMode(){
        try {
            LinearLayout layout = findViewById(R.id.settingsbackground);
            layout.setBackgroundColor(Color.parseColor("#333333"));
            TextView textView = findViewById(R.id.welcometosettingstextview);
            textView.setTextColor(Color.WHITE);
            Button button = findViewById(R.id.updatedatabasebutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.reportbugbutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.reportfeedback);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.settingsbluetoothtestbutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.settingsloginbutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.settingslogoutbutton);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            aSwitch.setTextColor(Color.WHITE);
        } catch (Exception ignored) {}

    }

    /**
     * DayMode Toggle
     */

    public void dayMode(){
        try {
            LinearLayout layout = findViewById(R.id.settingsbackground);
            layout.setBackgroundColor(Color.WHITE);
            TextView textView = findViewById(R.id.welcometosettingstextview);
            textView.setTextColor(Color.BLACK);
            Button button = findViewById(R.id.updatedatabasebutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.reportbugbutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.reportfeedback);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.settingsbluetoothtestbutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.settingsloginbutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.settingslogoutbutton);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            aSwitch.setTextColor(Color.BLACK);
        }catch(Exception ignored){}

    }



}
