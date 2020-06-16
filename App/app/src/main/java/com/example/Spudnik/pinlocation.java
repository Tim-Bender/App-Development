package com.example.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class pinlocation extends AppCompatActivity {

    private int loc;
    private ArrayList<connection> uniqueConnections;
    private connection myConnection;
    private vehicle myvehicle;
    private SharedPreferences preferences;
    private Map<String,Integer> orientations = new HashMap<>();
    private static final int VERTICAL = 1;
    private final String TAG = this.getClass().getSimpleName();
    private boolean built = false;
    private ArrayList<TextView> textViews = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlocation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Location");
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        try {
            myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            myvehicle.sortConnections(this);
            myConnection = getIntent().getParcelableExtra("myConnection");
            loc = Integer.parseInt(Objects.requireNonNull(myConnection).getS4());
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        fillHashMap();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        if(!built) {
            buildLayout();
        }
        updateValues();

    }

    @Override
    public void onResume(){
        super.onResume();
        if(preferences.getBoolean("nightmode",false)){
            nightMode();
        }
        else{
            dayMode();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateValues(){
        try {
            TextView textView = findViewById(R.id.pinlocationdirection);
            String temp = this.myConnection.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase();
            textView.setText(s1 + temp.substring(1));
            textView = findViewById(R.id.pinlocationconnectorinformation);
            textView.setText(this.myvehicle.getMap(this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc())) + "p " + this.myvehicle.inout() + " Connector");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @SuppressLint("SetTextI18n")
    private void buildLayout(){
        int pinnumber = myvehicle.getMap(myConnection.getDirection());
        int orientation = orientations.get(myConnection.getDirection());

        Space topspace = findViewById(R.id.topspacepinlocation);
        Space bottomspace = findViewById(R.id.bottomspacepinlocation);
        Space leftspace = findViewById(R.id.leftspacepinlocation);
        Space rightspace = findViewById(R.id.rightspacepinlocation);

        LinearLayout outsidelayout = findViewById(R.id.outsidelayoutpinlocation);
        LinearLayout innerlayout1 = findViewById(R.id.innerlayout1);
        LinearLayout innerlayout2 = findViewById(R.id.innerlayout2);

        TextView textView;
        Log.d(TAG,"Orientation:" + orientation);
        Log.d(TAG,"Pinnumber: " + pinnumber);

        if(orientation == VERTICAL){
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
                    textView = new TextView(this);
                    textView.setBackgroundResource(R.drawable.back);
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1));
                    textView.setTextSize(14);
                    textView.setTextColor(Color.BLACK);
                    textView.setText(Integer.toString(i));
                    textView.setGravity(Gravity.CENTER);
                    if(i == loc){
                        textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
                    }
                    innerlayout1.addView(textView);
                    textViews.add(textView);
                }
            }
            else{
               for(int i = 1; i <= pinnumber/2; i++){
                   textView = new TextView(this);
                   textView.setBackgroundResource(R.drawable.back);
                   textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1));
                   textView.setTextSize(14);
                   textView.setText(Integer.toString(i));
                   textView.setGravity(Gravity.CENTER);
                   textView.setTextColor(Color.BLACK);
                   if(i == loc){
                       textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
                   }
                   innerlayout2.addView(textView);
                   textViews.add(textView);
               }
                for(int i = pinnumber/2+1; i <= pinnumber; i++){
                    textView = new TextView(this);
                    textView.setBackgroundResource(R.drawable.back);
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1));
                    textView.setTextSize(14);
                    textView.setText(Integer.toString(i));
                    textView.setGravity(Gravity.CENTER);
                    textView.setTextColor(Color.BLACK);
                    if(i == loc){
                        textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
                    }
                    innerlayout1.addView(textView);
                    textViews.add(textView);
                }
            }
            built = true;
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
                textView = new TextView(this);
                textView.setBackgroundResource(R.drawable.back);
                textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1));
                textView.setTextSize(14);
                textView.setText(Integer.toString(i));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.BLACK);
                if(i == loc){
                    textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
                }
                innerlayout1.addView(textView);
                textViews.add(textView);
            }
            for(int i = pinnumber/2; i >=1; i--){
                textView = new TextView(this);
                textView.setBackgroundResource(R.drawable.back);
                textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1));
                textView.setTextSize(14);
                textView.setText(Integer.toString(i));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.BLACK);
                if(i == loc){
                    textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
                }
                innerlayout2.addView(textView);
                textViews.add(textView);
            }

            built = true;

        }

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
            System.out.println("Past: " + pastPos + " Current " + loc);
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
            System.out.println("Past: " + pastPos + " Current " + loc);
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
                return;
            }
        }
    }

    public void addHighlight(int loc){
        for(TextView t : textViews){
            if(t.getText().toString().equals(Integer.toString(loc))){
                t.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground);
                return;
            }
        }
    }

    public void nightMode(){
        try{
            ConstraintLayout layout = findViewById(R.id.pinlocationconstraintlayout);
            layout.setBackgroundColor(Color.parseColor("#333333"));
            TextView textView = findViewById(R.id.pinlocationdirection);
            textView.setTextColor(Color.WHITE);
            textView.setBackgroundResource(R.drawable.nightmodeback);
            LinearLayout layout1 = findViewById(R.id.pinlocationlinearlayout);
            layout1.setBackgroundResource(R.drawable.nightmodeback);
            textView = findViewById(R.id.pinlocationconnectorinformation);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.pinlocationtextview3);
            textView.setTextColor(Color.WHITE);
            textView = findViewById(R.id.pinlocationvoltage);
            textView.setTextColor(Color.WHITE);
            Button button = findViewById(R.id.prevpin);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
            button = findViewById(R.id.nextpin);
            button.setBackgroundResource(R.drawable.nightmodebuttonselector);
            button.setTextColor(Color.WHITE);
        }catch(Exception ignored){}

    }

    public void dayMode(){
        try{
            ConstraintLayout layout = findViewById(R.id.pinlocationconstraintlayout);
            layout.setBackgroundColor(Color.WHITE);
            TextView textView = findViewById(R.id.pinlocationdirection);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(R.drawable.back);
            LinearLayout layout1 = findViewById(R.id.pinlocationlinearlayout);
            layout1.setBackgroundResource(R.drawable.back);
            textView = findViewById(R.id.pinlocationconnectorinformation);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.pinlocationtextview3);
            textView.setTextColor(Color.BLACK);
            textView = findViewById(R.id.pinlocationvoltage);
            textView.setTextColor(Color.BLACK);
            Button button = findViewById(R.id.prevpin);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
            button = findViewById(R.id.nextpin);
            button.setBackgroundResource(R.drawable.daymodebuttonselector);
            button.setTextColor(Color.BLACK);
        }catch(Exception ignored){}

    }


    public void fillHashMap(){
        //vertical orientation = true
        //horizontal orientation = false
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
}
