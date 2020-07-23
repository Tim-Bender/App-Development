package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;
/**
 * Welcome to the pin selection activity.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @since dev 1.0.0
 * @see vehicle
 * @see ItemTouchHelper
 */

public class selectpin extends AppCompatActivity {
    /**vehicle object*/
    private vehicle myvehicle;
    /**Will be used to update textviews*/
    private TextView  textView;
    /**An Arraylist of connection objects. Will be parsed, and then used by the recyclerview*/
    private ArrayList<connection> connections = new ArrayList<>();
    /**Custom adapter for our recyclerview*/
    private ConnectionAdapter myAdapter;

    /**
     *
     * Some interesting stuff going on in this onCreate. First we setup our recycler view. and then we setupon an itemtouchhelper which allows
     * for the "deleting" of elements by swiping them off the screen.
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_selectpin);

        Toolbar toolbar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(toolbar);
        setTitle("Pin Selection");
        toolbar.setTitleTextColor(Color.WHITE);

        myvehicle = getIntent().getParcelableExtra("myvehicle");
        Objects.requireNonNull(myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
        textView = findViewById(R.id.connectorid);

        RecyclerView recyclerView = findViewById(R.id.selectpinrecyclerview); //setup our recyclerview
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new ConnectionAdapter(this,connections,myvehicle); //we will be using our custom adapter for this recyclerview
        recyclerView.setAdapter(myAdapter); //set the adapter

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { //setup our itemtouchhelper
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT){ //if you swipe right or left..
                    if (connections.size() != 1) { //and it isnt a 1 pin connection
                        connections.remove(viewHolder.getAdapterPosition()); //then we remove the item from the arraylist
                        myAdapter.notifyItemRemoved(viewHolder.getAdapterPosition()); //and we notify the adapter that there as been a change so it may animate it.
                    }
                }

            }
        });
        helper.attachToRecyclerView(recyclerView); //attach the helper above to our recyclerview
    }

    /**
     * We will use onResume to ensure that our recyclerview is up to date
     * @since dev 1.0.0
     */
    @Override
    protected void onResume(){
        super.onResume();
        myAdapter.notifyDataSetChanged(); //notify that the dataset has changed
        updatevalues();
    }

    /**
     * We will sort our connections by s4 number and then pass them to buildconnections
     * @since dev 1.0.0
     */
    @Override
    protected void onStart(){
        super.onStart();
        myvehicle.sortConnections(); //sort the connections
        if(connections.isEmpty()) //build them
            buildConnections();
    }

    /**
     * This method will parse the connections further and fill our arraylists.
     * @since dev 1.0.0
     */
    private void buildConnections(){
        if(connections.isEmpty()) {
            String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
            int counter = 0;
            for (connection c : myvehicle.getConnections()) {
                if (c.getDirection().contains(temp.toLowerCase())) {
                    if (!myvehicle.getUniquePins().contains(c.getDirection())) {
                        myvehicle.addUniquePin(c.getDirection());
                        myvehicle.setPinCount(myvehicle.getPinCount() + 1);
                    }
                    if (counter > 0 && c.getS4().equals(connections.get(counter - 1).getS4()))
                        connections.get(counter - 1).setName(connections.get(counter - 1).getName() + " / " + c.getName());
                    else {
                        connections.add(c);
                        counter++;
                    }
                }
            }
            myAdapter.notifyDataSetChanged(); //after we parse it, we notify the adapter that the dataset has changed
        }
    }

    /**
     * This method will update the textfields
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    private void updatevalues(){
        String temp = myvehicle.getUniqueConnections().get(myvehicle.getLoc());
        String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
        textView.setText(s1 + temp.substring(1));
        textView = findViewById(R.id.numberofpinstextfield);
        textView.setText(myvehicle.getMap(myvehicle.getUniqueConnections().get(myvehicle.getLoc())) + "p " + myvehicle.inout() + " Connector\n Connector Voltage\n=12.5VDC");
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
