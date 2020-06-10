package com.example.Spudnik;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;

import android.accessibilityservice.AccessibilityService;
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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class pinlocation extends AppCompatActivity {

    private int loc;
    private ArrayList<connection> uniqueConnections;
    private connection myConnection;
    private vehicle myvehicle;
    private TextView textView;
    private Toolbar toolbar;
    private SharedPreferences preferences;
    private Map<String,Integer> orientations = new HashMap<>();
    private static final int VERTICAL = 1, HORIZONTAL = 2;
    private final String TAG = this.getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlocation);
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Location");
        this.toolbar.setTitleTextColor(Color.WHITE);

        try {
            myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            myvehicle.sortConnections(vehicle.SORT_BY_S4,this);
            myConnection = getIntent().getParcelableExtra("myConnection");
            loc = Integer.parseInt(Objects.requireNonNull(myConnection).getS4());
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

            buildLayout();
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
        buildLayout();
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

    private void updateValues(){
        try {
            this.textView = findViewById(R.id.pinlocationdirection);
            String temp = this.myConnection.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase();
            this.textView.setText(s1 + temp.substring(1));
            this.textView = findViewById(R.id.pinlocationconnectorinformation);
            this.textView.setText(this.myvehicle.getMap(this.myvehicle.getUniqueConnections().get(this.myvehicle.getLoc())) + "p " + this.myvehicle.inout() + " Connector");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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
            if(pinnumber == 1){
                for(int i = 1; i < 3; i ++){
                    innerlayout2.setVisibility(View.GONE);
                    textView = new TextView(this);
                    textView.setBackgroundResource(R.drawable.back);
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1));
                    textView.setTextSize(14);
                    if(i == 1) {
                        textView.setText(Integer.toString(i));
                    }
                    else{
                        textView.setText("p");
                    }
                    textView.setGravity(Gravity.CENTER);
                    if(i == loc){
                        textView.setBackgroundResource(R.drawable.nightmodebuttonpressed);
                    }
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
                   if(i == loc){
                       textView.setBackgroundResource(R.drawable.nightmodebuttonpressed);
                   }
                   innerlayout2.addView(textView);
               }
                for(int i = pinnumber/2+1; i <= pinnumber; i++){
                    textView = new TextView(this);
                    textView.setBackgroundResource(R.drawable.back);
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0,1));
                    textView.setTextSize(14);
                    textView.setText(Integer.toString(i));
                    textView.setGravity(Gravity.CENTER);
                    if(i == loc){
                        textView.setBackgroundResource(R.drawable.nightmodebuttonpressed);
                    }
                    innerlayout1.addView(textView);
                }
            }


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
            this.loc++;
            if (this.loc == this.uniqueConnections.size()) {
                this.loc = 0;
            }
            updateValues();
        } catch (Exception ignored) {
        }
    }

    public void prevPin(View view){
        try {
            this.loc--;
            if (this.loc < 0) {
                this.loc = this.uniqueConnections.size() - 1;
            }
            updateValues();
        } catch (Exception ignored) {

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
