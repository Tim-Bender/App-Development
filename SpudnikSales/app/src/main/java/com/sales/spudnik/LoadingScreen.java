package com.sales.spudnik;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LoadingScreen extends AppCompatActivity {
    private ProgressBar progressBar;
    private TextView textView;
    private int progressStatus = 0;
    private Handler handler = new Handler();
    private vehicle myVehicle;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        progressBar = findViewById(R.id.loadingbar);
        progressBar.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        textView = findViewById(R.id.loadingText);
        ImageView image = findViewById(R.id.gifloadingscreen);
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
                i.putExtra("myvehicle",myVehicle);
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
}
