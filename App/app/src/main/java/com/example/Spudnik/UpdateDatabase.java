package com.example.Spudnik;

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
     */
    private void updateDataBase(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                Network activeNetwork = cm.getActiveNetwork();
                boolean isConnected = activeNetwork != null;
                if(isConnected) {
                    StorageReference reference = firebaseStorage.getReference().getRoot().child("DataBase");

                    final File rootpath = new File(context.getFilesDir(), "database");
                    File temp1 = new File(context.getFilesDir(), "");
                    File temp2 = new File(temp1, "machineids");
                    temp2.delete();

                    if (!rootpath.exists()) {
                       rootpath.mkdirs();
                    }
                    reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult) {
                            for (final StorageReference item : listResult.getItems()) {
                                final int numberOfFiles = listResult.getItems().size();
                                final int[] fileNumber = { 1 };
                                final File localFile = new File(rootpath, item.getName());
                                item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                    @Override
                                    public void onSuccess(StorageMetadata storageMetadata) {
                                        if (localFile.lastModified() < storageMetadata.getUpdatedTimeMillis()) {
                                           localFile.delete();
                                            item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    File root = new File(context.getFilesDir(), "");
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
                                                        fileNumber[0]++;
                                                        if(fileNumber[0] == numberOfFiles){
                                                            Toast.makeText(context, "Update Complete", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        } else if (localFile.lastModified() > storageMetadata.getUpdatedTimeMillis()) {
                                            File root = new File(context.getFilesDir(), "");
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
                                                fileNumber[0]++;
                                                if(fileNumber[0] == numberOfFiles){
                                                    Toast.makeText(context, "Update Complete", Toast.LENGTH_SHORT).show();
                                                }
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
}
