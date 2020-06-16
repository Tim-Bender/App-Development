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
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

/*
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
*/
public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private FirebaseUser user;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Loading");
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);

            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = preferences.edit();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();
            if(user != null){
                updateDataBase();
            }
            progressBar = findViewById(R.id.loadingbar);
            progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
            textView = findViewById(R.id.loadingText);
            ImageView image = findViewById(R.id.gifloadingscreen);
            Glide.with(this).load(R.drawable.heartbeatgiftransparent).into(image);
            new Thread(new Runnable() {
                @Override
                public void run() {
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
                    if(user != null) {
                        i = new Intent(getBaseContext(), home.class);
                    }
                    else{
                        i = new Intent(getBaseContext(),LoginActivity.class);
                    }
                    startActivity(i);
                    finish();
                }
               /* StorageReference reference = firebaseStorage.getReference().getRoot();

                final File rootpath = new File(getFilesDir(),"database");
        if(!rootpath.exists()){
                    rootpath.mkdirs();
                }*/
            }).start();
        } catch (Exception e) {
            Toast.makeText(this, "Boot Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }

        super.onStart();
    }

    public void nightMode(){
        ConstraintLayout constraints = findViewById(R.id.mainactivityconstraintlayout);
        constraints.setBackgroundColor(Color.parseColor("#333333"));
        TextView textView = findViewById(R.id.textView2);
        textView.setTextColor(Color.WHITE);
        textView = findViewById(R.id.textView3);
        textView.setTextColor(Color.WHITE);
    }

    public void updateDataBase() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        boolean isConnected = activeNetwork != null;
        if(isConnected) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    StorageReference reference = FirebaseStorage.getInstance().getReference().getRoot();

                    final File rootpath = new File(getFilesDir(), "database");
                    File temp1 = new File(getFilesDir(), "");
                    File temp2 = new File(temp1, "machineids");
                    boolean temp3 = temp2.delete();

                    if (!rootpath.exists()) {
                        Log.i(TAG, "Folder Created: " + rootpath.mkdirs());
                    }
                    reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (final StorageReference item : listResult.getItems()) {
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