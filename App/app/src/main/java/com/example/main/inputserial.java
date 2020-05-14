package com.example.main;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class inputserial extends AppCompatActivity {
    protected static vehicle myvehicle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inputserial);
    }


    public void go(View view){

        EditText edittext = (EditText)findViewById(R.id.inputid);
        String vehicleId = edittext.getText().toString();
        if(vehicleId.length() < 6){
            Toast.makeText(this,"Enter a proper serial number",Toast.LENGTH_SHORT).show();
        }
        else {
            this.myvehicle = new vehicle(vehicleId);
            Toast.makeText(this, vehicleId, Toast.LENGTH_LONG).show();
        }
        Intent i = new Intent(getBaseContext(), connectorselect.class);
        startActivity(i);
        finish();

    }
}
