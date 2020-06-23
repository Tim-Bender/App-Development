package com.Diagnostic.Spudnik;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

class UpdateDatabase{
    private SharedPreferences.Editor editor;
    private FirebaseStorage firebaseStorage;
    private Context context;

    /**
     * Constructor
     * @param c Context
     */
    @SuppressLint("CommitPrefEdits")
    UpdateDatabase(final Context c){
        context = c;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        editor = preferences.edit();
        firebaseStorage = FirebaseStorage.getInstance();
        updateDataBase();
    }

    /**
     * Asynchronously check's the firebase database bucket for updated files, downloads them and updates the acceptable machine id's data file.
     * First we use ConnectivityManager to determine whether or not we are connected to a network, if we are then we attempt the update.
     * All event listeners are asynchronous and thus must be nested within one another for correct runtime execution.
     */
    private void updateDataBase(){
        AsyncTask.execute(new Runnable() { //Everything will be done asynchronously
            @Override
            public void run() {
                final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); //Check if a network is active
                Network activeNetwork = cm.getActiveNetwork();
                final boolean isConnected = activeNetwork != null;
                if(isConnected) {   //If we are connected go ahead and try updating
                    StorageReference reference = firebaseStorage.getReference().getRoot().child("DataBase"); //Storage reference to firebase bucket, at its child "Database"

                    final File rootpath = new File(context.getFilesDir(), "database"); //rootpath to local database folder
                    File temp1 = new File(context.getFilesDir(), "");
                    File temp2 = new File(temp1, "machineids");  //delete the current list of machine id's.
                    temp2.delete();

                    if (!rootpath.exists()) { //if the rootpath doesn't exist, we create the folder. This is necessary on first boot
                       rootpath.mkdirs();
                    }
                    reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() { //get all the items in at the firebase reference location
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (final StorageReference item : listResult.getItems()) {
                                final File localFile = new File(rootpath, item.getName()); //get the local version of the file for comparison
                                item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() { //Get the metadata of the item.
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        if (localFile.lastModified() < storageMetadata.getUpdatedTimeMillis()) {    //if the file either doesn't exist locally, or is outdated.. we download
                                           localFile.delete(); //delete the old version
                                            item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { //download the file
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    //At this point the file is downloaded, and now we just need to update the machineids data file
                                                    File root = new File(context.getFilesDir(), ""); //rootpath to the folder containing machine ids. The parent of rootpath ^
                                                    FileWriter fw;
                                                    File toEdit = new File(root, "machineids"); //reference to the machineids data file
                                                    try {
                                                        String line = "", toPrint;
                                                        if (toEdit.exists()) { //We will need to append the newly downloaded file to the contents in machineids
                                                            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(toEdit)));
                                                            line = reader.readLine();
                                                        }
                                                        //add it to the file, and close the file.
                                                        String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("_", "") + ",";
                                                        toPrint = (line != null) ? line + editedItemName : editedItemName; //ternary operator.
                                                        fw = new FileWriter(toEdit);
                                                        fw.append(toPrint);
                                                        fw.flush();
                                                        fw.close();
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } else if (localFile.lastModified() > storageMetadata.getUpdatedTimeMillis()) { //If we don't need to download the file, we do still need to
                                            File root = new File(context.getFilesDir(), "");                      //add the name to machineids data file, works the same as above ^
                                            FileWriter fw;
                                            File toEdit = new File(root, "machineids");
                                            try {
                                                String line = "", toPrint;
                                                if (toEdit.exists()) {
                                                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(toEdit)));
                                                    line = reader.readLine();
                                                }
                                                String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("_", "") + ",";
                                                toPrint = (line != null) ? line + editedItemName : editedItemName;
                                                fw = new FileWriter(toEdit);
                                                fw.append(toPrint);
                                                fw.flush();
                                                fw.close();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }


                                        }
                                    }
                                });
                            }
                            editor.putBoolean("databaseupdated", true);
                            editor.commit();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
                }
            }
        });
    }

    public void writeMachineIdFile(StorageReference item){
        File root = new File(context.getFilesDir(), ""); //rootpath to the folder containing machine ids. The parent of rootpath ^
        FileWriter fw;
        File toEdit = new File(root, "machineids"); //reference to the machineids data file
        try {
            String line = "", toPrint;
            if (toEdit.exists()) { //We will need to append the newly downloaded file to the contents in machineids
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(toEdit)));
                line = reader.readLine();
            }
            //add it to the file, and close the file.
            String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("_", "") + ",";
            toPrint = (line != null) ? line + editedItemName : editedItemName; //ternary operator.
            fw = new FileWriter(toEdit);
            fw.append(toPrint);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

