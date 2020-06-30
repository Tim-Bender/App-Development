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

public class Pindiagnostic extends AppCompatActivity {
    private vehicle myvehicle;
    private TextView direction,connectorinformation;
    private connection myConnection;
    private ArrayList<connection> uniqueConnections;
    private int loc;
    private ConnectionAdapterHorizontal myAdapter;
    private SnapHelper snapHelper;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_pindiagnostic);
        try {
            myvehicle = getIntent().getParcelableExtra("myvehicle");
            Objects.requireNonNull(this.myvehicle).setConnections(getIntent().<connection>getParcelableArrayListExtra("connections"));
            loc = getIntent().getIntExtra("loc", 0);
            uniqueConnections = getIntent().getParcelableArrayListExtra("uniqueconnections");

            myConnection = uniqueConnections.get(this.loc);
            Toolbar toolbar = findViewById(R.id.topAppBar);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            direction = findViewById(R.id.direction);
            connectorinformation = findViewById(R.id.connectorinformation);
            recyclerView = findViewById(R.id.horizontalrecyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            myAdapter = new ConnectionAdapterHorizontal(this,uniqueConnections,myvehicle);
            LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(Pindiagnostic.this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(horizontalLayoutManager);
            snapHelper = new PagerSnapHelper();
            snapHelper.attachToRecyclerView(recyclerView);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    updateSnapPosition(recyclerView,dx,dy);
                }
            });
            recyclerView.setAdapter(myAdapter);
            updateValues();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        recyclerView.scrollToPosition(loc);
    }

    @SuppressLint("SetTextI18n")
    public void updateValues(){
        try {
            myConnection = uniqueConnections.get(loc);
            String temp = myConnection.getDirection();
            String s1 = temp.substring(0, 1).toUpperCase();
            direction.setText(s1 + temp.substring(1));
            connectorinformation.setText(myvehicle.getMap(myConnection.getDirection().toLowerCase()) + " " + myConnection.inout() + " Connector\nConnectorVoltage\nVoltage");
            setTitle("Viewing Pin:" + myConnection.getS4());
            myAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateSnapPosition(RecyclerView recyclerView,int dx, int dy){
        int newsnapPosition = snapHelper.findTargetSnapPosition(recyclerView.getLayoutManager(),dx,dy);
        if(newsnapPosition != loc && newsnapPosition >-1){
            loc = newsnapPosition;
            updateValues();
        }


    }

    public void viewpinloc(View view){
        Intent i = new Intent(getBaseContext(), pinlocation.class);
        i.putExtra("myvehicle", myvehicle);
        i.putParcelableArrayListExtra("connections",myvehicle.getConnections());
        i.putExtra("myConnection",myConnection);
        startActivity(i);
    }

    public void testMode(View view){
        try {
            Intent i = new Intent(getApplicationContext(), warningscreen.class);
            i.putExtra("myvehicle", myvehicle);
            i.putParcelableArrayListExtra("connections",myvehicle.getConnections());
            i.putExtra("myConnection",myConnection);
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
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
}
