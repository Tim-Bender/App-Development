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

package com.Diagnostic.Spudnik.CustomObjects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Synchronize firebase storage bucket files with locally-kept files.
 * <p>
 * The simplest way to initiate a database update is just: "new UpdateDataBase()".
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see <a href="https://firebase.google.com/docs/storage">FireBase Storage Documentation</a>
 * @see com.google.firebase.storage
 * @see com.google.firebase.auth.FirebaseUser
 * @see AsyncTask
 * @see BufferedReader
 * @since dev 1.0.0
 */

public class UpdateDatabase {
    /**
     * Context of the app's current activity. Used to send broadcasts and communicate with the main UI thread
     */
    private final Context context;
    /**
     * Constants used for control flow and broadcast filtering
     */
    public static final int UPDATE_BEGUN = 0, UPDATE_COMPLETE = 1, UPDATE_FAILED = 2, TERMS_OF_SERVICE_UPDATED = 3;
    /**
     * Action String used to filter by broadcast receivers on the main UI thread.
     */
    public static final String action = "com.Diagnostic.Spudnik.CustomObjects.UpdateDatabase.Update"; //this can really be anything but must be unique
    /**
     * Used to keep track of the number of files that were updated.
     */
    private int numberOfFilesUpdated = 0;

    /**
     * <p>Constructor, a context is required to be passed from the parent activity. This context allows us to locate file directories...
     * Update will be triggered automatically from object creation</p>
     *
     * @param context Context of the current Activity on main UI thread
     * @since dev 1.0.0
     */
    @SuppressLint("CommitPrefEdits")
    public UpdateDatabase(@NonNull final Context context) {
        this.context = context;
        updateDataBase(); //will automatically trigger an update upon object creation
    }

    /**
     * <p>Asynchronously check's the firebase database bucket for updated files, downloads them and updates the machine id's data file.
     * First we use ConnectivityManager to determine whether or not we are connected to a network, if we are then we attempt the update.
     * All event listeners are asynchronous and thus must be nested within one another for correct runtime execution.</p>
     *
     * @since dev 1.0.0
     */
    private void updateDataBase() {
        //Everything will be done asynchronously
        AsyncTask.execute(() -> {
            final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE); //Check if a network is active
            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork != null && FirebaseAuth.getInstance().getCurrentUser() != null) {   //If we are connected go ahead and try updating. Also must be logged in...
                StorageReference reference = FirebaseStorage.getInstance().getReference().getRoot().child("DataBase"); //Storage reference to firebase bucket, at its child "Database"
                broadcastUpdate(UPDATE_BEGUN); //send a broadcast that we have begun the update
                final File rootpath = new File(context.getFilesDir(), "database"); //Rootpath to local database folder

                if (!rootpath.exists())  //if the rootpath doesn't exist, we create the folder. This is necessary on first boot
                    rootpath.mkdirs();
                new File(rootpath, "machineids").delete();    //delete the current list of machine id's.
                final int[] fileTotalNumber = new int[1]; // will be used to track the number of total files in the firebase bucket, so we can broadcast when are done
                final int[] fileNumber = new int[]{0}; //will be used to track which file we are current at in the list. when we reach the end we know we are done
                //get all the items in at the firebase reference location
                //Failure on firebase bucket item list request
                reference.listAll().addOnSuccessListener(listResult -> {
                    fileTotalNumber[0] = listResult.getItems().size() - 1; //get the number of items in the firebase storage bucket
                    for (final StorageReference item : listResult.getItems()) { //begin iterating through each storage item
                        final File localFile = new File(rootpath, item.getName().toLowerCase()); //get the local version of the file for comparison
                        //Get the metadata of the item.
                        //failure on item metadata request
                        item.getMetadata().addOnSuccessListener(storageMetadata -> {
                            if (localFile.lastModified() < storageMetadata.getUpdatedTimeMillis()) {    //if the file either doesn't exist locally, or is outdated.. we download
                                localFile.delete(); //delete the old version
                                //download the file
                                //failure on item download
                                item.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                                    if (!item.getName().equals("dealerids") && !item.getName().equals("termsofservice.pdf")) //we dont want to add "dealerids and termsofservice.pdf to the list of machine ids
                                        writeMachineIdFile(item);  //write the name of the file to the list of acceptable machine ids
                                    if (item.getName().equals("termsofservice.pdf"))
                                        broadcastUpdate(TERMS_OF_SERVICE_UPDATED);
                                    fileNumber[0]++; //iterate up to the next file
                                    numberOfFilesUpdated++; //iterate up
                                    if (fileNumber[0] == fileTotalNumber[0]) //if we have downloaded our final file then we are done
                                        broadcastUpdate(UPDATE_COMPLETE); //broadcast then that the update is complete
                                }).addOnFailureListener(e -> {
                                    broadcastUpdate(UPDATE_FAILED); //update failure
                                }).addOnCanceledListener(() -> broadcastUpdate(UPDATE_FAILED));
                            } else if (localFile.lastModified() > storageMetadata.getUpdatedTimeMillis()) { //if there is not a newer version, we still need to add the filename
                                if (!item.getName().equals("dealerids") && !item.getName().equals("termsofservice.pdf")) //we dont want to add "dealerids and termsofservice.pdf to the list of machine ids
                                    writeMachineIdFile(item); //write the name of the file to the list of acceptable machine ids
                                fileNumber[0]++; //iterate up
                                if (fileNumber[0] == fileTotalNumber[0]) //if we have iterated to the last file then we are done
                                    broadcastUpdate(UPDATE_COMPLETE); //update that the update is complete

                            }
                            /*The rest of the method are all error catchers. The onFailureListener's will react if a download, meta data fetch, or firebase bucket
                             * list request fails. The else blocks will trigger if there is another error, such as the user being null or
                             * a lack of internet connectivity.
                             */
                        }).addOnFailureListener(e -> {
                            broadcastUpdate(UPDATE_FAILED); //update failure
                        }).addOnCanceledListener(() -> broadcastUpdate(UPDATE_FAILED));
                    }
                }).addOnFailureListener(e -> {
                    broadcastUpdate(UPDATE_FAILED); //update failure
                }).addOnCanceledListener(() -> broadcastUpdate(UPDATE_FAILED));
            } else //failure in network connectivity or user is null
                broadcastUpdate(UPDATE_FAILED); //update failure
        });
    }

    /**
     * <p>This method will update the machineids data file. It will take its current contents, and then append the new item's name onto the end.
     * It is done Asynchronously so that our download file threads above may not be interrupted.</p>
     *
     * @param item StorageReference
     * @since dev 1.0.0
     */
    private void writeMachineIdFile(@NonNull final StorageReference item) {
        AsyncTask.execute(() -> {
            File toEdit = new File(new File(context.getFilesDir(), "database"), "machineids"); //reference to the machineids data file
            try {
                String line = (toEdit.exists()) ? new BufferedReader(new InputStreamReader(new FileInputStream(toEdit))).readLine() : null; //read the line, ternary operator
                String editedItemName = item.getName().toLowerCase().replace(".csv", "").replace("_", "") + ","; //format the item's name. Strip the .csv, and _ off
                editedItemName = (line != null) ? line + editedItemName : editedItemName; //ternary operator.
                FileWriter fw = new FileWriter(toEdit); //create the new FileWriter
                fw.append(editedItemName).flush(); //append the new name on.
                fw.close();//close the file
            } catch (IOException e) {
                broadcastUpdate(UPDATE_FAILED); //update failure
            }
        });
    }

    /**
     * <p>This method is used to broadcast beginning, completion, and error messages to the activities.
     * The action is used by broadcast receivers to perform filtering.</p>
     *
     * @param result Result code
     * @since dev 1.0.0
     */
    private void broadcastUpdate(@NonNull final int result) {
        Intent intent = new Intent(); //an intent will be broadcasted
        intent.setAction(action); //set the action for later filtering
        intent.putExtra("data", result); //attach the result of the update
        if (result == UPDATE_COMPLETE)
            intent.putExtra("updatedfiles", numberOfFilesUpdated);//if the update is complete we include the number of files that were updated in the broadcast
        context.sendBroadcast(intent); //send the broadcast

    }
}

