package com.example.main;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.Constraints;

public class settings extends AppCompatActivity {
    private Toolbar myToolBar;
    private Switch aSwitch;
    boolean nightmode = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        this.myToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolBar);
        setTitle("Settings");
        myToolBar.setTitleTextColor(Color.WHITE);
        aSwitch = findViewById(R.id.settingsToggle);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked && nightmode == false){
                    nightmode = true;
                    nightMode();
                    return;
                }
                if(!isChecked && nightmode == true){
                    nightmode = false;
                    dayMode();
                    return;
                }
            }
        });

    }

    public void nightMode(){
        LinearLayout layout = findViewById(R.id.settingsbackground);
        layout.setBackgroundColor(Color.parseColor("#333333"));
        TextView textView = findViewById(R.id.welcometosettingstextview);
        textView.setTextColor(Color.WHITE);
        Button button = findViewById(R.id.updatedatabasebutton);
        button.setBackgroundResource(R.drawable.toolbargradient);
        button.setTextColor(Color.WHITE);
        button = findViewById(R.id.reportbugbutton);
        button.setBackgroundResource(R.drawable.toolbargradient);
        button.setTextColor(Color.WHITE);
        button = findViewById(R.id.reportfeedback);
        button.setBackgroundResource(R.drawable.toolbargradient);
        button.setTextColor(Color.WHITE);
        aSwitch.setTextColor(Color.WHITE);

    }

    public void dayMode(){
        LinearLayout layout = findViewById(R.id.settingsbackground);
        layout.setBackgroundColor(Color.WHITE);
        TextView textView = findViewById(R.id.welcometosettingstextview);
        textView.setTextColor(Color.BLACK);
        Button button = findViewById(R.id.updatedatabasebutton);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setTextColor(Color.BLACK);
        button = findViewById(R.id.reportbugbutton);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setTextColor(Color.BLACK);
        button = findViewById(R.id.reportfeedback);
        button.setBackgroundResource(android.R.drawable.btn_default);
        button.setTextColor(Color.BLACK);
        aSwitch.setTextColor(Color.BLACK);

    }
}
