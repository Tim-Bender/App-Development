package com.example.Spudnik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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
import java.util.Objects;


/**
 * Welcome to the starting activity of the app. This activity will serve as a loading screen, an authentication check, and an automate database update.
 */
public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private FirebaseUser user;
    private SharedPreferences.Editor editor;

    /**
     * Setup the toolbar, this will be the same across all activities, and will thus only be mentioned here.
     * Assign values to instance fields, including SharedPreferences and firebath authentication/user.
     * @param savedInstanceState savedInstanceState
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Loading");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        progressBar = findViewById(R.id.loadingbar);
        progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        textView = findViewById(R.id.loadingText);

    }

    /**
     * This method will toggle night and day mode, initiate gif glide,
     * then begin asynchronous background tasks involving database construction, and loading bar updates
     */

    @Override
    protected void onStart() {
        //Night/Day Mode Toggle
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        //If the firebase user != null, then they are authenticated and we can attempt a database update.
        if(user != null){
            updateDataBase();
        }
        //GIF Glide animation begin
        ImageView image = findViewById(R.id.gifloadingscreen);
        Glide.with(this).load(R.drawable.heartbeatgiftransparent).into(image);
        //Begin Asynchronous threading
        try{
        new Thread(new Runnable() {
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
                //If user is authenticated, we redirect them to home
                if(user != null) {
                    i = new Intent(getBaseContext(), home.class);
                }
                //Otherwise they are directed towards the login activity.
                else{
                    i = new Intent(getBaseContext(),LoginActivity.class);
                }
                startActivity(i);
                //Closes the App if someone attempts to "back" into the loading activity.
                finish();
            }
        }).start();
    } catch (Exception ignored) {
    }
        super.onStart();
    }
    /**
     * NightMode Toggle
     * Most activities will have a nightmode and a daymode toggle, so that they might be switched in the middle
     * of runtime, however since we are only showing the loading screen once, it does not need a daymode toggle.
     */
    public void nightMode(){
        ConstraintLayout constraints = findViewById(R.id.mainactivityconstraintlayout);
        constraints.setBackgroundColor(Color.parseColor("#333333"));
        TextView textView = findViewById(R.id.textView2);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.textView3);
        textView.setTextColor(Color.WHITE);
    }

    /**
     * Asynchronous database construction and updatability. Connects to firebase's server,
     * iterates through all items in the bucket, and downloads needed files, replacing the old ones.
     * It will also update the list of acceptable vehicle id numbers stored in com.example.Spudnik/files/machineids
     */

    public void updateDataBase() {
        //Check if there is an internet connection, if not then we exit.
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        boolean isConnected = activeNetwork != null;
        //if there is a connection, we go ahead and update
        if(isConnected) {
            //the entire database updating will be done Asynchronously
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    //get firebase storage reference
                    StorageReference reference = FirebaseStorage.getInstance().getReference().getRoot();
                    final File rootpath = new File(getFilesDir(), "database");

                    //Here we delete the current machineids file.
                    File temp1 = new File(getFilesDir(), "");
                    File temp2 = new File(temp1, "machineids");
                    temp2.delete();

                    //if the database folder has not been created yet, usually the first time someone installs the app,
                    // then it is created.
                    if (!rootpath.exists()) {
                        Log.i(TAG, "Folder Created: " + rootpath.mkdirs());
                    }

                    //Make a listall request to firebase to retrieve a list of all items within the storage bucket
                    reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            //loop through all items in the bucket
                            for (final StorageReference item : listResult.getItems()) {
                                final File localFile = new File(rootpath, item.getName());
                                //retrieve the metadata of the object
                                item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        //compare the Last updated time of the local file, and the file stored on firebase.
                                        //If the new one needs to be downloaded, download it
                                        if (localFile.lastModified() < storageMetadata.getUpdatedTimeMillis()) {
                                            Log.i(TAG, "File deleted " + localFile.delete());
                                            item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Log.i(TAG, "ItemName " + item.getName());

                                                    //If the download is a success, add that machine id to the machineids's list file
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
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                            //If we don't need to download the file, add the machine id to the list anyway.
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
                            Log.e(TAG, "Firebase Update Error");
                        }
                    });
                }
            });
        }
    }
}