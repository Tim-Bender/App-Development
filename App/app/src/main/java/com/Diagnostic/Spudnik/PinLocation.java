/*
 *
 *  Copyright (c) 2020, Spudnik LLc <https://www.spudnik.com/>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are not permitted in any form.
 *
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION, DEATH, or SERIOUS INJURY or DAMAGE)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.Diagnostic.Spudnik.Bluetooth.BroadcastActionConstants;
import com.Diagnostic.Spudnik.CustomObjects.Pin;
import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Display the location of the selected pin in the on board orientation for the s4 board.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 */
public class PinLocation extends AppCompatActivity {

    /**
     * This will keep track of which pin we are currently looking at
     */
    private int loc;
    /**
     * The connection we are currently looking at
     */
    private Pin myPin;
    /**
     * Vehicle object
     */
    private Vehicle myvehicle;
    /**
     * Map used to map the connection to on board orientation. Aka out1 is vertical
     */
    private Map<String, Integer> orientations = new HashMap<>();
    /**
     * A vertical orientation is defined as a 1
     */
    private static final int VERTICAL = 1;
    /**
     * Used for control logic
     */
    private boolean built = false;
    /**
     * Each box in the grid is a textview, we will keep a list of them here so we may iterate through them.
     */
    private ArrayList<TextView> textViews = new ArrayList<>();
    /**
     * Current orientation we are in
     */
    private int orientation;

    private PinLocation.BluetoothBroadcastReceiver receiver;
    /**
     * Normal uninteresting oncreate method
     *
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinlocation);
        Toolbar toolbar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        setTitle("Pin Location");
        getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);

        myvehicle = getIntent().getParcelableExtra("myvehicle"); //retrieve the vehicle object from parcelable intent
        Objects.requireNonNull(myvehicle).setPins(getIntent().getParcelableArrayListExtra("connections")); //retrieve the list of connections from parcelable intent
        myvehicle.sortConnections(); //sort the connections for good measure
        myPin = getIntent().getParcelableExtra("myConnection"); //get the current connection from parcelable intent
        loc = Integer.parseInt(Objects.requireNonNull(myPin).getS4()); //get the current location from parcelable intent
        fillHashMap(); //fill the hashmap of orientations
        IntentFilter filter = new IntentFilter();
        for(BroadcastActionConstants b : BroadcastActionConstants.values())
            filter.addAction(b.getString());
        receiver = new PinLocation.BluetoothBroadcastReceiver();
        registerReceiver(receiver, filter);
    }

    /**
     * @since dev 1.0.0
     * Used to build the layout and update values
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (!built)
            buildLayout(); //build the layout
        updateValues(0f); //update the textviews
    }

    @Override
    protected void onDestroy(){
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    /**
     * @since dev 1.0.0
     * Used to update the textviews
     */
    @SuppressLint("SetTextI18n")
    private void updateValues(float f) {
        TextView textView = findViewById(R.id.pinlocationdirection);
        String temp = myPin.getDirection();
        String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
        textView.setText(s1 + temp.substring(1));  //concatenate them together
        textView = findViewById(R.id.pinlocationconnectorinformation);
        textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections()
                .get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\nConnectorVoltage\n" + f +"v"); //put it all together
    }

    private class BluetoothBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastActionConstants.ACTION_CHARACTERISTIC_READ.getString())) {
                byte[] bytes = intent.getByteArrayExtra("bytes");
                if (bytes != null) {
                    updateValues(((bytes[0] << 8) + bytes[1]) / 100f);
                }
                getSupportActionBar().setIcon(R.drawable.bluetoothsymbol);
            }
            else if (intent.getAction().equals(BroadcastActionConstants.ACTION_GATT_DISCONNECTED.getString())){
                getSupportActionBar().setIcon(R.drawable.bluetoothdisconnected);
                Snackbar.make(findViewById(R.id.pinlocationconstraintlayout),"Bluetooth Disconnected",Snackbar.LENGTH_SHORT).show();
            } else if(intent.getAction().equals(BroadcastActionConstants.ACTION_SCANNING.getString())){
                getSupportActionBar().setIcon(R.drawable.bluetoothsearching);
            }  else if(intent.getAction().equals(BroadcastActionConstants.ACTION_WEAK_SIGNAL.getString()))
                Snackbar.make(findViewById(R.id.pinlocationconstraintlayout),"Weak Bluetooth Signal",Snackbar.LENGTH_SHORT).show();
        }
    }
    /**
     * This method is used to create the gridlayout depending on the orientation, number of pins and other factors.
     * It is a polymorphic mess that i hope to replace with a grid layout in the future. Since i hope to replace it,
     * and its obscenely complicated to begin with i will not be including many in-line comments.
     *
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    private void buildLayout() {
        int pinnumber = myvehicle.getMap(myPin.getDirection()); //get all the views we will need to be editing
        orientation = orientations.get(myPin.getDirection());

        Space topspace = findViewById(R.id.topspacepinlocation); //the 4 spaces will allow us to "squish" our layout depending on which orientation we are in
        Space bottomspace = findViewById(R.id.bottomspacepinlocation);
        Space leftspace = findViewById(R.id.leftspacepinlocation);
        Space rightspace = findViewById(R.id.rightspacepinlocation);

        LinearLayout outsidelayout = findViewById(R.id.outsidelayoutpinlocation); //all of our different linear layouts
        LinearLayout innerlayout1 = findViewById(R.id.innerlayout1);
        LinearLayout innerlayout2 = findViewById(R.id.innerlayout2);

        if (orientation == VERTICAL) { //vertical orientation
            topspace.setVisibility(View.GONE); //remove top space
            topspace.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            bottomspace.setVisibility(View.GONE); //remove bottom space
            bottomspace.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            rightspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 4)); //squish from the right and the left
            leftspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 4));
            outsidelayout.setOrientation(LinearLayout.HORIZONTAL); //ensure our layouts are in the correct orientation
            innerlayout1.setOrientation(LinearLayout.VERTICAL);
            innerlayout1.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            innerlayout2.setOrientation(LinearLayout.VERTICAL);
            innerlayout2.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));

            outsidelayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2));
            if (pinnumber == 2) { //if its a 2 pin connector we treat it specially
                rightspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 4.5f));
                leftspace.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 4.5f));
                innerlayout2.setVisibility(View.GONE);
                innerlayout2.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
                innerlayout1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                LinearLayout superlayout = findViewById(R.id.superlinearlayoutpinlocation);
                superlayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2));
                topspace.setVisibility(View.VISIBLE);
                topspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.25f));
                bottomspace.setVisibility(View.VISIBLE);
                bottomspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.25f));
                for (int i = 1; i < 3; i++) { //add both of our textviews to the layout
                    innerlayout1.addView(getTextView(i));
                }
            } else {
                for (int i = 1; i <= pinnumber / 2; i++) { //add all of our textviews to the layout
                    innerlayout2.addView(getTextView(i));
                }
                for (int i = pinnumber / 2 + 1; i <= pinnumber; i++) {
                    innerlayout1.addView(getTextView(i));
                }
            }
        } else { //otherwise we are in horizontal orientation
            leftspace.setVisibility(View.GONE); //remove the left and right spaces
            leftspace.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
            rightspace.setVisibility(View.GONE);
            rightspace.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

            topspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.25f)); //squish from the top and the bottom
            bottomspace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2.25f));

            outsidelayout.setOrientation(LinearLayout.VERTICAL); //ensure that the layout's are in the correct orientations
            outsidelayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            innerlayout1.setOrientation(LinearLayout.HORIZONTAL);
            innerlayout1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

            innerlayout2.setOrientation(LinearLayout.HORIZONTAL);
            innerlayout2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

            LinearLayout superlayout = findViewById(R.id.superlinearlayoutpinlocation);
            superlayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 2));
            for (int i = pinnumber; i > pinnumber / 2; i--) { //add the textviews
                innerlayout1.addView(getTextView(i));
            }
            for (int i = pinnumber / 2; i >= 1; i--) {
                innerlayout2.addView(getTextView(i));
            }
        }
        built = true;
    }

    /**
     * This method is used by the above buildLayout method to generate a new textview
     *
     * @param i The pin number to display on the textbox
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    private TextView getTextView(@NonNull int i) {
        TextView textView = new TextView(getApplicationContext()); //create a new textview
        textView.setTextSize(14);
        textView.setText(Integer.toString(i));
        textView.setGravity(Gravity.CENTER);
        if (orientation != VERTICAL)
            textView.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        else
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        if (i == loc) { //if this the pin we are currently looking at, we change the background and textcolor.
            textView.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground); //set the background
            textView.setTextColor(Color.BLACK); //set the textcolor
        } else { //this is not the box you are looking for..... haha did you get that reference? if you didn't you aren't a real nerd just saying....
            textView.setBackgroundResource(R.drawable.back); //set the background to the white square
            textView.setTextColor(Color.WHITE);  //set the textcolor to white
        }
        textViews.add(textView); //add the textview to our arraylist of textviews
        return textView; //return it to be added to the layout
    }


    /**
     * Nextpin button redirect. Will determine the next pin to highlight and then flip highlight's accordingly
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void nextPin(View view) {
        removeHighlight(loc); //remove highlight at the current spot
        loc = (loc++ == textViews.size()) ? 1 : loc++; //ternary operator. Determine if we have overflowed list
        addHighlight(loc); //add the highlight at the current spot
    }

    /**
     * Prevpin button redirect. Will determine the prev pin to highlight and then flip highlight's accordingly.
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void prevPin(View view) {
        removeHighlight(loc); //remove the highlight at the current position
        loc = (loc == 1) ? textViews.size() : --loc; //ternary operator. Determine if we have underflowed list
        addHighlight(loc); //add the highlight at the new spot
    }

    /**
     * This method is used by nextPin and prevPin above to remove the highlight at a desired location
     *
     * @param loc Pin number/loc for which to remove the highlight
     * @since dev 1.0.0
     */
    private void removeHighlight(@NonNull int loc) {
        for (TextView t : textViews) { //iterate through the textview arraylist to find the correct one
            if (t.getText().toString().equals(Integer.toString(loc))) { //if we have a match....
                t.setBackgroundResource(R.drawable.back); //remove highlight
                t.setTextColor(Color.WHITE); //flip textcolor
                break; //break out
            }
        }
    }

    /**
     * This method is used by nextPin and prevPin above to add a highlight at the desired location
     *
     * @param loc Pin number/loc for which to add the highlight
     * @since dev 1.0.0
     */
    private void addHighlight(@NonNull int loc) {
        for (TextView t : textViews) { //iterate through the textview arraylist to find the correct one
            if (t.getText().toString().equals(Integer.toString(loc))) { //if we have a match....
                t.setBackgroundResource(R.drawable.pinlocationcurrentbuttonbackground); //add highlight
                t.setTextColor(Color.BLACK); //flip textcolor
                break;
            }
        }
    }

    /**
     * This method will fill the hasmap which is responsible for maping directions to their orientation. 1 is vertical, 2 is horizontal
     *
     * @since dev 1.0.0
     */
    private void fillHashMap() {
        orientations.put("out1", 1);
        orientations.put("out2", 1);
        orientations.put("out3", 1);
        orientations.put("out4", 1);
        orientations.put("out5", 1);
        orientations.put("out6", 1);
        orientations.put("out7", 1);
        orientations.put("out8", 1);
        orientations.put("out9", 1);

        orientations.put("in1", 1);
        orientations.put("in2", 1);

        orientations.put("in3", 2);
        orientations.put("in4", 2);

        orientations.put("exp11out", 1);
        orientations.put("exp11in", 2);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(getBaseContext(), Settings.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarbuttons, menu);
        return true;
    }
}
