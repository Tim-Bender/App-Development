package com.Diagnostic.Spudnik;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.File;

/**
 * @author timothy.bender
 * @version dev 1.0.0
 *
 * Welcome to the terms of service activity. This activity is responsible for displaying an up-to-date version of the spudnik terms of service.
 * It makes use of the Pdfviewer package.
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
        loadPdf();
    }

    private void loadPdf(){
        final PDFView pdfView = findViewById(R.id.pdfView); //grab our pdf view object
        final File pdf = new File(new File(getFilesDir(),"database"),"termsofservice.pdf"); //filepath to where the termsofservice.pdf will be stored
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

        else {     //if the filepath does not exist, then we haven't updated the database yet and we will fall back on the included verison
            new UpdateDatabase(termsofservice.this);
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