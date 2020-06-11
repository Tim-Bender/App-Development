package com.example.Spudnik;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private FirebaseAuth auth;
    private FirebaseUser user;
    private EditText emailEditText;
    private EditText passwordEditText;
    private vehicle myVehicle;
    private Toolbar toolbar;
    private SharedPreferences preferences;
    private boolean nightmode;
    private SharedPreferences.Editor editor;
    private FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        firebaseStorage = FirebaseStorage.getInstance();
        editor = sharedPreferences.edit();
        if(user != null){
            goToHome();
            finish();
        }
        myVehicle = getIntent().getParcelableExtra("myvehicle");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Login");
        toolbar.setTitleTextColor(Color.WHITE);
        emailEditText = findViewById(R.id.loginemailedittext);
        passwordEditText = findViewById(R.id.loginpasswordedittext);
    }

    @Override
    protected void onStart(){
        super.onStart();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        nightmode = preferences.getBoolean("nightmode",false);
        if(nightmode){
            nightMode();
        }
        else{
            dayMode();
        }
    }

    public void login(View view){
        auth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(),passwordEditText.getText().toString().trim())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
                            user = auth.getCurrentUser();
                            updateDataBase(new View(LoginActivity.this));
                            goToHome();
                        }
                        else{
                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }
    public void updateDataBase(View view){
        StorageReference reference = firebaseStorage.getReference().getRoot();

        final File rootpath = new File(getFilesDir(),"database");
        if(!rootpath.exists()){
            rootpath.mkdirs();
        }
        reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                for(StorageReference item : listResult.getItems()){
                    final File localFile = new File(rootpath,item.getName());
                    localFile.delete();
                    Log.i(TAG,"Item Name: " + item.getName());
                    item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(LoginActivity.this, "Downloaded: " + localFile, Toast.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this, "FileDownloaded", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });


                }
                editor.putBoolean("databaseupdated",true);
                editor.commit();
                Toast.makeText(LoginActivity.this, "Update Complete", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG,"Firebase Update Error");
            }
        });
    }


    private void goToHome(){
        Intent i = new Intent(getBaseContext(),home.class);
        i.putExtra("myvehicle",myVehicle);
        startActivity(i);
    }

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
