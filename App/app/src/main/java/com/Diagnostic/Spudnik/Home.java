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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.Diagnostic.Spudnik.CustomObjects.Vehicle;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Home page. Buttons which will direct the user throughout the rest of the app.
 *
 * @author timothy.bender
 * @version dev1.0.0
 * @since dev 1.0.0
 */

public class Home extends AppCompatActivity {
    /**
     * Vehicle object. Initialized in mainactivity, and usually pre-build there as well.
     */
    private Vehicle myvehicle;

    /**
     * Nothing special in this onCreate. There is a check whether or not the vehicle object's id's have been constructed or not
     *
     * @param savedInstanceState savedInstancestate
     * @since dev 1.0.0
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_home);

        Toolbar myToolBar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(myToolBar);
        setTitle("Home");
        myToolBar.setTitleTextColor(Color.WHITE);

        Snackbar.make(findViewById(R.id.homeconstraintlayout), "Welcome", Snackbar.LENGTH_SHORT).show();
        myvehicle = getIntent().getParcelableExtra("myvehicle"); //get out parcelabled vehicle object
        if (myvehicle.getVehicleIds().isEmpty())
            myvehicle.preBuildVehicleObject(this); //try again to prebuild the vehicle ids and dealer names.
        findViewById(R.id.homediagtoolbutton).setOnClickListener((view) -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); //get the current firebase user
            if (user != null) { //authentication is required before one can access the diagnostic tool
                Intent i = new Intent(getBaseContext(), InputSerial.class);
                i.putExtra("myvehicle", myvehicle); //add the myvehicle object as a parcelable extra in the intent
                startActivity(i);
            } else
                Snackbar.make(findViewById(R.id.homeconstraintlayout), "Please Sign In", Snackbar.LENGTH_SHORT).show();
        });
        findViewById(R.id.homeupdatebutton).setOnClickListener((view) -> Snackbar.make(findViewById(R.id.homeconstraintlayout), "Function Not Supported", Snackbar.LENGTH_SHORT).show());
        findViewById(R.id.homelogbutton).setOnClickListener((view) -> Snackbar.make(findViewById(R.id.homeconstraintlayout), "Function Not Supported", Snackbar.LENGTH_SHORT).show());
        findViewById(R.id.homesettingsbutton).setOnClickListener((view) -> startActivity(new Intent(getApplicationContext(), Settings.class)));
    }

    /**
     * The next two methods will create the toolbar menu item on the top right, this will be on every
     * activity that contains this shortcut.
     *
     * @param item MenuItem
     * @return onOptionsItemSelected(item)
     * @since dev 1.0.0
     */
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
