package com.Diagnostic.Spudnik;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Display diagnostic information pertaining to the currently selected pin. Pins may be scrolled through horizontally.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see RecyclerView
 * @see SnapHelper
 * @since dev 1.0.0
 */
public class Pindiagnostic extends AppCompatActivity {
    /**
     * Vehicle object
     */
    private vehicle myvehicle;
    /**
     * Our two textviews that will need to be kept updated
     */
    private TextView direction, connectorinformation;
    /**
     * the current connection that we are viewing
     */
    private connection myConnection;
    /**
     * Unique connections, scrollview will be filled by this
     */
    private ArrayList<connection> uniqueConnections;
    /**
     * Current position users are in the scrollview
     */
    private int loc;
    /**
     * Custom view adapter for the recyclerview
     */
    private ConnectionAdapterHorizontal myAdapter;
    /**
     * Will allow us to "snap" to items in the horizontal scrollview
     */
    private SnapHelper snapHelper;
    /**
     * Our recyclerview object, will be in horizontal orientation
     */
    private RecyclerView recyclerView;

    /**
     * Typical onCreate, we do setup the recyclerview with its scrolllistener and snaphelper
     *
     * @param savedInstanceState Bundle
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pindiagnostic);
        //get all of our objects
        myvehicle = getIntent().getParcelableExtra("myvehicle");
        Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
        loc = getIntent().getIntExtra("loc", 0);
        uniqueConnections = getIntent().getParcelableArrayListExtra("uniqueconnections");
        //setup the toolbar as usual
        myConnection = uniqueConnections.get(this.loc);
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        direction = findViewById(R.id.direction);
        connectorinformation = findViewById(R.id.connectorinformation);
        //setup the recyclerview
        recyclerView = findViewById(R.id.horizontalrecyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new ConnectionAdapterHorizontal(this, uniqueConnections, myvehicle);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(Pindiagnostic.this, LinearLayoutManager.HORIZONTAL, false); //make it horizontal
        recyclerView.setLayoutManager(horizontalLayoutManager); //set the layout manager for the recycler view
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView); //attach the snaphelper to the recycler view
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                updateSnapPosition(recyclerView, dx, dy); //when users scroll using the horizontal recyclerview we update the snap position
            }
        });
        recyclerView.setAdapter(myAdapter); //set the adapter to our recyclerview, pulls objects from our arraylist
        updateValues();
    }

    /**
     * This method will be used to snap to the correct position in the recyclerview when the page is first loaded
     *
     * @since dev 1.0.0
     */
    @Override
    protected void onStart() {
        super.onStart();
        recyclerView.scrollToPosition(loc); //snap to the correct position when the page is first loaded
    }

    /**
     * This method will update the textviews
     *
     * @since dev 1.0.0
     */
    @SuppressLint("SetTextI18n")
    private void updateValues() {
        myConnection = uniqueConnections.get(loc);
        String temp = myConnection.getDirection();
        String s1 = temp.substring(0, 1).toUpperCase(); //capitalize the first letter
        direction.setText(s1 + temp.substring(1));
        connectorinformation.setText(myvehicle.getMap(myConnection.getDirection().toLowerCase()) + " " + myConnection.inout() + " Connector\nConnectorVoltage\nVoltage");
        setTitle("Viewing Pin:" + myConnection.getS4());
        myAdapter.notifyDataSetChanged();
    }

    /**
     * Here the "loc" variable is updated and then the textviews will be updated. called whenever the user scrolls.
     *
     * @param recyclerView our recyclerview
     * @param dx           x position
     * @param dy           y position
     * @since dev 1.0.0
     */
    private void updateSnapPosition(@NonNull RecyclerView recyclerView, @NonNull int dx, @NonNull int dy) {
        int newSnapPosition = snapHelper.findTargetSnapPosition(recyclerView.getLayoutManager(), dx, dy);
        if (newSnapPosition != loc && newSnapPosition > -1) { //dont update if it hasn't moved, or is less than 0
            loc = newSnapPosition;
            updateValues();
        }
    }

    /**
     * Button redirect to send users to the pinlocation activity
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void viewpinloc(View view) {
        Intent i = new Intent(getBaseContext(), pinlocation.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections", myvehicle.getConnections());
        i.putExtra("myConnection", myConnection);
        startActivity(i);
    }

    /**
     * Button redirect to send users towards the pintestmode, will pass to warningscreen activity
     *
     * @param view View
     * @since dev 1.0.0
     */
    public void testMode(View view) {
        Intent i = new Intent(getApplicationContext(), warningscreen.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections", myvehicle.getConnections());
        i.putExtra("myConnection", myConnection);
        startActivity(i);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent i = new Intent(getBaseContext(), settings.class);
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
