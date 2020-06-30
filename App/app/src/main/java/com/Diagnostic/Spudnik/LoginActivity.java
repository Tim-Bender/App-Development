package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
 * @author timothy.bender
 * @version dev1.0.0
 *
 * Welcome to the login activity...
 */

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private vehicle myvehicle;
    private boolean fromSettings;


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
        auth = FirebaseAuth.getInstance();  //setup firebase user and auth
        user = auth.getCurrentUser();
        if(user != null){   //If they are already logged in, send them to the home activity
            finish();
        }
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        setTitle("Sign In");
        //Objects.requireNonNull(getSupportActionBar()).setIcon(R.mipmap.ic_launcher);
        toolbar.setTitleTextColor(Color.WHITE);
        emailEditText = findViewById(R.id.loginemailedittext);
        passwordEditText = findViewById(R.id.loginpasswordedittext);
        fromSettings = getIntent().getBooleanExtra("fromsettings",false);//a boolean value is used to determine where the user came from
    }


    /**
     * Pass the contents of the two edittext fields into a firebase authentication request.
     * If the task is successful, then we send them on their way.
     * @param view view
     */
    public void login(View view){
        auth.signInWithEmailAndPassword(emailEditText.getText().toString().trim(),passwordEditText.getText().toString().trim()) //pass the information into an authentication request
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){ //if the task is successful, we welcome the user, assign the user variable, then update the database
                            user = FirebaseAuth.getInstance().getCurrentUser();
                            if(fromSettings){
                                finish(); //if they logged in from the settings page we will just close this page and send them back there
                            }
                            //otherwise they came from the loading screen.
                            myvehicle = new vehicle(); //create a new vehicle, since it couldn't have been done on loading
                            new UpdateDatabase(getApplicationContext()); //update the database
                            myvehicle.preBuildVehicleObject(getApplicationContext()); //prebuild the vehicle
                            goToHome(); //go to the home activity

                        }
                        else{
                            //otherwise we inform them that authentication has failed.
                            Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(R.id.loginconstraintlayout), "Sign In Failed", Snackbar.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * This function will be used to send the user to the home activity.
     */
    private void goToHome(){
        if(user != null) { //redundancy check
            Intent i = new Intent(getBaseContext(), home.class);
            i.putExtra("myvehicle",myvehicle); //put the vehicle as a parcelable extra
            startActivity(i); //go to home
        }
    }

}
