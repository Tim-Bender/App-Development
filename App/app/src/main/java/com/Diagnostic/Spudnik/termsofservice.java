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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;

/**
 * Display the most up to date version of the Spudnik Terms of Service.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see PDFView
 * @see UpdateDatabase
 * @since dev 1.0.0
 */
public class termsofservice extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsofservice);

        Toolbar toolbar = findViewById(R.id.topAppBar); //typical toolbar setup
        setSupportActionBar(toolbar);
        setTitle("Spudnik Terms of Service");
        toolbar.setTitleTextColor(Color.WHITE);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(UpdateDatabase.action);
        UpdateDatabaseBroadcastReceiver broadcastReceiver = new UpdateDatabaseBroadcastReceiver();
        registerReceiver(broadcastReceiver, filter);
        loadPdf();

    }

    /**
     * This method will load the pdf into the view, it will default to the most up to date one downloaded. If there is no updated version
     * available then it will load from the pre-included version that ships with the installer.
     *
     * @since dev 1.0.0
     */
    private void loadPdf() {
        final PDFView pdfView = findViewById(R.id.pdfView); //grab our pdf view object
        final File pdf = new File(new File(getFilesDir(), "database"), "termsofservice.pdf"); //filepath to where the termsofservice.pdf will be stored
        if (pdf.exists())  //if the filepath exists, then we use that one, since this one will be the updated one
            pdfView.fromFile(pdf)   //load the pdfview view from the file url
                    .enableSwipe(true)
                    .swipeHorizontal(true) //not necessary but why not
                    .enableDoubletap(true) //double tap to zoom enabled
                    .defaultPage(0) //sets the default page of the pdf. since we have one page we just set it to 0
                    .enableAnnotationRendering(false)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true) //sharpens the picture a bit
                    .spacing(0)
                    .pageFitPolicy(FitPolicy.WIDTH) //width justification is preferred, it will fill vertical anyway
                    .load(); //load the pdf
        else
            new UpdateDatabase(this);
    }

    /**
     * UpdateDataBase broadcast receiver. Will listen for events from UpdateDatabase and then load the pdf correctly
     *
     * @since dev 1.0.0
     */
    private class UpdateDatabaseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UpdateDatabase.action)) {
                if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_COMPLETE)
                    loadPdf();
                else if (intent.getIntExtra("data", 2) == UpdateDatabase.UPDATE_FAILED) {
                    final PDFView pdfView = findViewById(R.id.pdfView); //grab our pdf view object
                    pdfView.fromAsset("termsofservice.pdf") //load the pdf from assets
                            .enableSwipe(true)
                            .swipeHorizontal(true)
                            .enableDoubletap(true)
                            .defaultPage(0)
                            .enableAnnotationRendering(false)
                            .password(null)
                            .scrollHandle(null)
                            .enableAntialiasing(true) //sharpens the picture a bit
                            .spacing(0)
                            .pageFitPolicy(FitPolicy.WIDTH)
                            .load();
                }
            }
        }
    }

}