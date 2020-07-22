package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 *
 * Welcome to the pinlocation activity. It's primary job is to display the location of pins in on-board orientations
 */
public class pinlocation extends AppCompatActivity {

    /**This will keep track of which pin we are currently looking at*/
    private int loc;
    /**The connection we are currently looking at*/
    private connection myConnection;
    /**Vehicle object*/
    private vehicle myvehicle;
    /**Map used to map the connection to on board orientation. Aka out1 is vertical*/
    private Map<String,Integer> orientations = new HashMap<>();
    /**A vertical orientation is defined as a 1*/
    private static final int VERTICAL = 1;
    /**Used for control logic*/
    private boolean built = false;
    /**Each box in the grid is a textview, we will keep a list of them here so we may iterate through them.*/
    private ArrayList<TextView> textViews = new ArrayList<>();
    /**Current orientation we are in*/
    private int orientation;


    /**
     * Normal uninteresting oncreate method
     * @since dev 1.0.0
     * @param savedInstanceState Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlocation);
        Toolbar toolbar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Location");
        toolbar.setTitleTextColor(Color.WHITE);

        myvehicle = getIntent().getParcelableExtra("myvehicle"); //retrieve the vehicle object from parcelable intent
        Objects.requireNonNull(myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections")); //retrieve the list of connections from parcelable intent
        myvehicle.sortConnections(); //sort the connections for good measure
        myConnection = getIntent().getParcelableExtra("myConnection"); //get the current connection from parcelable intent
        loc = Integer.parseInt(Objects.requireNonNull(myConnection).getS4()); //get the current location from parcelable intent
        fillHashMap(); //fill the hashmap of orientations
    }

    /**
     * @since dev 1.0.0
     * Used to build the layout and update values
     */
    @Override
    protected void onStart(){
        super.onStart();
        if(!built)
            buildLayout(); //build the layout
        updateValues(); //update the textviews

    }

    /**
     * @since dev 1.0.0
     * Used to update the textviews
     */
    @SuppressLint("SetTextI18n")
    private void updateValues(){
        TextView textView = findViewById(R.id.pinlocationdirection);
        String temp = myConnection.getDirection();
        String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
        textView.setText(s1 + temp.substring(1));  //concatenate them together
        textView = findViewById(R.id.pinlocationconnectorinformation);
        textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections()
                .get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\nConnectorVoltage\nVoltage"); //put it all together
    }

    /** This method is used to create the gridlayout depending on the orientation, number of pins and other factors.
     * It is a polymorphic mess that i hope to replace with a grid layout in the future. Since i hope to replace it,
     * and its obscenely complicated to begin with i will not be including in-line comments.
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    private void buildLayout(){
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

    /**This method is used by the above buildLayout method to generate a new textview
     * @since dev 1.0.0
     * @param i The pin number to display on the textbox
     */
    @SuppressLint("SetTextI18n")
    private TextView getTextView(@NonNull int i){
        TextView textView = new TextView(getApplicationContext()); //create a new textview
        if(orientation != VERTICAL)
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        else
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        textView.setTextSize(14);
        textView.setText(Integer.toString(i));
        textView.setGravity(Gravity.CENTER);
        if(i == loc){ //if this the pin we are currently looking at, we change the background and textcolor.
            textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground); //set the background
            textView.setTextColor(Color.BLACK); //set the textcolor
        }
        else{ //this is not the box you are looking for..... haha did you get that reference? if you didn't you aren't a real nerd just saying....
            textView.setBackgroundResource(R.drawable.back); //set the background to the white square
            textView.setTextColor(Color.WHITE);  //set the textcolor to white
        }
        textViews.add(textView); //add the textview to our arraylist of textviews
        return textView; //return it to be added to the layout
    }


    /**
     * Nextpin button redirect. Will determine the next pin to highlight and then flip highlight's accordingly
     * @since dev 1.0.0
     * @param view View
     */
    public void nextPin(View view){
        removeHighlight(loc); //remove highlight at the current spot
        loc = (loc++ == textViews.size()) ? 1 : loc++; //ternary operator. Determine if we have overflowed list
        addHighlight(loc); //add the highlight at the current spot
        updateValues(); //update textviews
    }

    /**
     * Prevpin button redirect. Will determine the prev pin to highlight and then flip highlight's accordingly.
     * @since dev 1.0.0
     * @param view View
     */
    public void prevPin(View view){
        removeHighlight(loc); //remove the highlight at the current position
        loc = (loc == 1) ? textViews.size() : --loc; //ternary operator. Determine if we have underflowed list
        addHighlight(loc); //add the highlight at the new spot
        updateValues(); //update textviews
    }

    /**
     * This method is used by nextPin and prevPin above to remove the highlight at a desired location
     * @since dev 1.0.0
     * @param loc Pin number/loc for which to remove the highlight
     */
    private void removeHighlight(@NonNull int loc){
        for(TextView t : textViews){ //iterate through the textview arraylist to find the correct one
            if(t.getText().toString().equals(Integer.toString(loc))){ //if we have a match....
                t.setBackgroundResource(R.drawable.back); //remove highlight
                t.setTextColor(Color.WHITE); //flip textcolor
                break; //break out
            }
        }
    }

    /**
     * This method is used by nextPin and prevPin above to add a highlight at the desired location
     * @since dev 1.0.0
     * @param loc Pin number/loc for which to add the highlight
     */
    private void addHighlight(@NonNull int loc){
        for(TextView t : textViews){ //iterate through the textview arraylist to find the correct one
            if(t.getText().toString().equals(Integer.toString(loc))){ //if we have a match....
                t.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground); //add highlight
                t.setTextColor(Color.BLACK); //flip textcolor
                break;
            }
        }
    }

    /**
     * This method will fill the hasmap which is responsible for maping directions to their orientation. 1 is vertical, 2 is horizontal
     */
    private void fillHashMap(){
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
}
