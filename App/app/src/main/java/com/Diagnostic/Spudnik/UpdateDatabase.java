package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author timothy.bender
 * @version dev 1.0.0
 *
 * Welcome to the UpdateDataBase class, this class is used to synchronize the files stored within the Spudnik Diagnostic Firebase Storage Bucket
 * with the phone's local copies. The simplest way to invoke a database update using this class is just: "new UpdateDatabase"
 * The simplest way to initiate a database update is just: "new UpdateDataBase()". No need to store it. It will be picked up by the garbage connector when its done.
 */
class UpdateDatabase{

    private final Context context;

    /**
     * Constructor, a context is required to be passed from the parent activity. This context allows us to locate file directories...
     * @param c Context
     */
    @SuppressLint("CommitPrefEdits")
    UpdateDatabase(final Context c){
        context = c;
        updateDataBase();
    }

    /**
     * Asynchronously check's the firebase database bucket for updated files, downloads them and updates the  amachine id's data file.
     * First we use ConnectivityManager to determine whether or not we are connected to a network, if we are then we attempt the update.
     * All event listeners are asynchronous and thus must be nested within one another for correct runtime execution.
     */
    private void updateDataBase(){
        AsyncTask.execute(new Runnable() { //Everything will be done asynchronously
            @Override
            public void run() {
                final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); //Check if a network is active
                Network activeNetwork = cm.getActiveNetwork();
                if(activeNetwork != null && FirebaseAuth.getInstance().getCurrentUser() != null) {   //If we are connected go ahead and try updating. Also must be logged in...
                    StorageReference reference = FirebaseStorage.getInstance().getReference().getRoot().child("DataBase"); //Storage reference to firebase bucket, at its child "Database"

                    final File rootpath = new File(context.getFilesDir(), "database"); //Rootpath to local database folder

                    if (!rootpath.exists())  //if the rootpath doesn't exist, we create the folder. This is necessary on first boot
                        rootpath.mkdirs();
                    new File(rootpath,"machineids").delete();    //delete the current list of machine id's.

                    reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() { //get all the items in at the firebase reference location
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (final StorageReference item : listResult.getItems()) {
                                final File localFile = new File(rootpath, item.getName().toLowerCase()); //get the local version of the file for comparison
                                item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() { //Get the metadata of the item.
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        if (localFile.lastModified() < storageMetadata.getUpdatedTimeMillis()) {    //if the file either doesn't exist locally, or is outdated.. we download
                                            localFile.delete(); //delete the old version
                                            item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { //download the file
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    if(!item.getName().equals("dealerids"))
                                                    writeMachineIdFile(item); //At this point the file is downloaded, and now we just need to update the machineids data file
                                                }
                                            });
                                        } else if (localFile.lastModified() > storageMetadata.getUpdatedTimeMillis()) {
                                            if(!item.getName().equals("dealerids"))//If we don't need to download the file, we do still need to
                                            writeMachineIdFile(item);                                             //add it to the list
                                        }
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() { //If it fails, oh well.
                        @Override
                        public void onFailure(@NonNull Exception e) {}
                    });
                }
            }
        });
    }

    /**
     * This method will update the machineids data file. It will take its current contents, and then append the new item's name onto the end.
     * It is done Asynchronously so that our download file threads above may not be interrupted.
     * @param item StorageReference
     */
    private void writeMachineIdFile(final StorageReference item) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                File toEdit = new File(new File(context.getFilesDir(),"database"), "machineids"); //reference to the machineids data file
                try {
                    String line = (toEdit.exists()) ? new BufferedReader(new InputStreamReader(new FileInputStream(toEdit))).readLine() : null; //read the line, ternary operator
                    String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("_", "") + ","; //format the item's name. Strip the .csv, and _ off
                    editedItemName = (line != null) ? line + editedItemName : editedItemName; //ternary operator.
                    FileWriter fw = new FileWriter(toEdit); //create the new FileWriter
                    fw.append(editedItemName).flush(); //append the new name on.
                    fw.close();//close the file
                } catch (IOException ignored) {}
            }
        });
    }
}

