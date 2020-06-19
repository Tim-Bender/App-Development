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
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 *
 *
 * Welcome to the login activity...
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private FirebaseUser user;
    private EditText emailEditText;
    private EditText passwordEditText;
    private SharedPreferences.Editor editor;
    private FirebaseStorage firebaseStorage;

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
        firebaseStorage = FirebaseStorage.getInstance();
        //If they are already logged in, send them to the home activity
        if(user != null){
            goToHome();
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
    }

    /**
     * Just your typical nightmode daymode check in onstart
     */
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onStart(){
        super.onStart();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = preferences.edit();
        boolean nightmode = preferences.getBoolean("nightmode", false);
        if(nightmode){
            nightMode();
        }
        else{
            dayMode();
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
                            updateDataBase();
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
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        boolean isConnected = activeNetwork != null;
        if(isConnected) {

            StorageReference reference = firebaseStorage.getReference().getRoot().child("DataBase");
            Toast.makeText(this, "Updating...", Toast.LENGTH_SHORT).show();
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
                                                String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("_", "") + ",";
                                                toPrint = (line != null) ? line + editedItemName : editedItemName;
                                                fw = new FileWriter(toEdit);
                                                fw.append(toPrint);
                                                fw.flush();
                                                fw.close();
                                                fileNumber[0]++;
                                                if(fileNumber[0] == numberOfFiles){
                                                    Toast.makeText(LoginActivity.this, "Update Complete", Toast.LENGTH_SHORT).show();
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
                                        String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("_", "") + ",";
                                        toPrint = (line != null) ? line + editedItemName : editedItemName;
                                        fw = new FileWriter(toEdit);
                                        fw.append(toPrint);
                                        fw.flush();
                                        fw.close();
                                        fileNumber[0]++;
                                        if(fileNumber[0] == numberOfFiles){
                                            Toast.makeText(LoginActivity.this, "Update Complete", Toast.LENGTH_SHORT).show();
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
                    if (getIntent().getBooleanExtra("fromsettings", false)) {
                        finish();
                    } else {
                        goToHome();
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Firebase Update Error");
                }
            });
        }
    }

    public void updateApp(View view){

    }


    /**
     * This function will be used to send the user to the home activity.
     */
    private void goToHome(){
        Intent i = new Intent(getBaseContext(),home.class);
        startActivity(i);
    }

    /**
     * nightMode toggle
     */
    public void nightMode(){
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

    /**
     * DayMode toggle
     */

    public void dayMode(){
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

}
