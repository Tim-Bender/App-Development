package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class pinlocation extends AppCompatActivity {

    private int loc;
    private connection myConnection;
    private vehicle myvehicle;
    private Map<String,Integer> orientations = new HashMap<>();
    private static final int VERTICAL = 1;
    private boolean built = false;
    private ArrayList<TextView> textViews = new ArrayList<>();
    private Handler handler = new Handler();
    private int orientation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlocation);
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Location");
        toolbar.setTitleTextColor(Color.WHITE);
        myvehicle = getIntent().getParcelableExtra("myvehicle");
        Objects.requireNonNull(myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
        myvehicle.sortConnections(this);
        myConnection = getIntent().getParcelableExtra("myConnection");
        loc = Integer.parseInt(Objects.requireNonNull(myConnection).getS4());
    }

    @Override
    protected void onStart(){
        super.onStart();
        fillHashMap();
        if(!built) {
            buildLayout();
        }
        updateValues();

    }

    @SuppressLint("SetTextI18n")
    private void updateValues(){
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    TextView textView = findViewById(R.id.pinlocationdirection);
                    String temp = myConnection.getDirection();
                    String s1 = temp.substring(0, 1).toUpperCase();
                    textView.setText(s1 + temp.substring(1));
                    textView = findViewById(R.id.pinlocationconnectorinformation);
                    textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections().get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\nConnectorVoltage\nVoltage");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void buildLayout(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        int pinnumber = myvehicle.getMap(myConnection.getDirection());
                        orientation = orientations.get(myConnection.getDirection());

                        Space topspace = findViewById(R.id.topspacepinlocation);
                        Space bottomspace = findViewById(R.id.bottomspacepinlocation);
                        Space leftspace = findViewById(R.id.leftspacepinlocation);
                        Space rightspace = findViewById(R.id.rightspacepinlocation);

                        LinearLayout outsidelayout = findViewById(R.id.outsidelayoutpinlocation);
                        LinearLayout innerlayout1 = findViewById(R.id.innerlayout1);
                        LinearLayout innerlayout2 = findViewById(R.id.innerlayout2);

                        if(orientation == VERTICAL){
                            Log.i("pinlocation","BUILDING VERTICAL");
                            topspace.setVisibility(View.GONE);
                            topspace.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                            bottomspace.setVisibility(View.GONE);
                            bottomspace.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                            rightspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,4));
                            leftspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,4));
                            outsidelayout.setOrientation(LinearLayout.HORIZONTAL);
                            innerlayout1.setOrientation(LinearLayout.VERTICAL);
                            innerlayout1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1));

                            innerlayout2.setOrientation(LinearLayout.VERTICAL);
                            innerlayout2.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1));

                            outsidelayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,2));
                            if(pinnumber == 2){
                                rightspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,4.5f));
                                leftspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,4.5f));
                                innerlayout2.setVisibility(View.GONE);
                                innerlayout2.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                                innerlayout1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                                LinearLayout superlayout = findViewById(R.id.superlinearlayoutpinlocation);
                                superlayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,2));
                                topspace.setVisibility(View.VISIBLE);
                                topspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,2.25f));
                                bottomspace.setVisibility(View.VISIBLE);
                                bottomspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,2.25f));
                                for(int i = 1; i < 3; i ++){
                                    innerlayout1.addView(getTextView(i));
                                }
                            }
                            else{
                                for(int i = 1; i <= pinnumber/2; i++){
                                    innerlayout2.addView(getTextView(i));
                                }
                                for(int i = pinnumber/2+1; i <= pinnumber; i++){
                                    innerlayout1.addView(getTextView(i));
                                }
                            }
                        }
                        else{
                            leftspace.setVisibility(View.GONE);
                            leftspace.setLayoutParams(new LinearLayout.LayoutParams(0,0));
                            rightspace.setVisibility(View.GONE);
                            rightspace.setLayoutParams(new LinearLayout.LayoutParams(0,0));

                            topspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,2.25f));
                            bottomspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,2.25f));

                            outsidelayout.setOrientation(LinearLayout.VERTICAL);
                            outsidelayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                            innerlayout1.setOrientation(LinearLayout.HORIZONTAL);
                            innerlayout1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1));

                            innerlayout2.setOrientation(LinearLayout.HORIZONTAL);
                            innerlayout2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1));

                            LinearLayout superlayout = findViewById(R.id.superlinearlayoutpinlocation);
                            superlayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,2));
                            for(int i = pinnumber; i > pinnumber/2; i--){
                                innerlayout1.addView(getTextView(i));
                            }
                            for(int i = pinnumber/2; i >=1; i--){
                                innerlayout2.addView(getTextView(i));
                            }

                        }
                        built = true;
                    }
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public TextView getTextView(int i){
        TextView textView = new TextView(getApplicationContext());
        textView.setBackgroundResource(R.drawable.back);
        if(orientation != VERTICAL) {
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        }else{
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        }
        textView.setTextSize(14);
        textView.setText(Integer.toString(i));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        if(i == loc){
            textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
            textView.setTextColor(Color.BLACK);
        }
        textViews.add(textView);
        return textView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_settings){
            Intent i = new Intent(getBaseContext(),settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons,menu);
        return true;
    }

    public void nextPin(View view){
        try {
            int pastPos = loc;
            loc++;
            if (loc == textViews.size()+1) {
                loc = 1;
            }
            removeHighlight(pastPos);
            addHighlight(loc);
            updateValues();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prevPin(View view){
        try {
            int pastPos = loc;
            loc--;
            if (loc < 1) {
                loc = textViews.size();
            }
            removeHighlight(pastPos);
            addHighlight(loc);
            updateValues();
        } catch (Exception ignored) {

        }
    }

    public void removeHighlight(int loc){
        for(TextView t : textViews){
            if(t.getText().toString().equals(Integer.toString(loc))){
                t.setBackgroundResource(R.drawable.back);
                t.setTextColor(Color.WHITE);
                return;
            }
        }
    }

    public void addHighlight(int loc){
        for(TextView t : textViews){
            if(t.getText().toString().equals(Integer.toString(loc))){
                t.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
                t.setTextColor(Color.BLACK);
                return;
            }
        }
    }

    public void fillHashMap(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                orientations.put("out1",1);
                orientations.put("out2",1);
                orientations.put("out3",1);
                orientations.put("out4",1);
                orientations.put("out5",1);
                orientations.put("out6",1);
                orientations.put("out7",1);
                orientations.put("out8",1);
                orientations.put("out9",1);

                orientations.put("in1",1);
                orientations.put("in2",1);

                orientations.put("in3",2);
                orientations.put("in4",2);

                orientations.put("exp11out",1);
                orientations.put("exp11in",2);
            }
        });
    }
}
