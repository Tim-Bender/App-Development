package com.Diagnostic.Spudnik;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FireSync {
    //TODO 1: Implement maximum filesize for downloads
    //TODO 2: Test the entire api
    //TODO 3: Generate Documentation

    private Context context;
    private final int RUN_MODE;
    static final int UPDATE_ON_CREATE = 0,UPDATE_ON_DEMAND = 1, FILE_DOWNLOADED = 2, FILE_DOWNLOAD_FAILED = 3,DISABLE_AUTHENTICATION_REQUIREMENTS = 4,
            ENABLE_AUTHENTICATION_REQUIREMENTS = 5,UPDATE_OFTEN = 1000,UPDATE_SLOWLY = 10000;
    private Set<FirebaseItem> downloadedFiles;
    private Set<FirebaseItem> containerItems;
    private long totalDownloaded;
    private StorageReference storageReference;
    private File rootpath;
    private int authMode = ENABLE_AUTHENTICATION_REQUIREMENTS;
    private int syncMode = 0,SYNCHRONIZATION_FREQUENCY = UPDATE_OFTEN;
    private long maximumDownloadSize = 1048576;


    public FireSync(Context context,StorageReference reference,File rootpath,int RUN_MODE){
        this.context = context;
        this.RUN_MODE = RUN_MODE;
        this.rootpath = rootpath;
        storageReference = reference;
        downloadedFiles = new HashSet<>();
        containerItems = new HashSet<>();
        totalDownloaded = 0;
        if(RUN_MODE == UPDATE_ON_CREATE){
            synchronizeFolder();
        }
    }

    public FireSync(Context context,StorageReference reference){
        this.context = context;
        this.storageReference = reference;
        this.rootpath = new File(context.getFilesDir(), "database");
        downloadedFiles = new HashSet<>();
        containerItems = new HashSet<>();
        totalDownloaded = 0;
        RUN_MODE = UPDATE_ON_DEMAND ;
    }

    public class FirebaseItem{
        StorageReference reference;
        StorageMetadata metadata;

        FirebaseItem(StorageReference reference, StorageMetadata metadata){
            this.reference = reference;
            this.metadata = metadata;
        }

        long getSize(){
            return metadata.getSizeBytes();
        }
        String getName(){
            return metadata.getName();
        }
        long getUpdatedTime(){
            return metadata.getUpdatedTimeMillis();
        }

    }

    public void start(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    synchronizeFolder();
                    try {
                        Thread.sleep(SYNCHRONIZATION_FREQUENCY);
                    } catch (InterruptedException ignored) {}
                }
            }
        });
    }
    public void synchronizeFolder(){
        AsyncTask.execute(new Runnable() { //Everything will be done asynchronously
            @Override
            public void run() {
                if(getConnectedState() && (FirebaseAuth.getInstance().getCurrentUser() != null || authMode == DISABLE_AUTHENTICATION_REQUIREMENTS)) {   //If we are connected go ahead and try updating. Also must be logged in...

                    if (!rootpath.exists())  //if the rootpath doesn't exist, we create the folder. This is necessary on first boot
                        rootpath.mkdirs();

                    storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() { //get all the items in at the firebase reference location
                        @Override
                        public void onSuccess(ListResult listResult) {
                            containerItems.clear();
                            for (final StorageReference item : listResult.getItems()) {
                                final File localFile = new File(rootpath, item.getName());//get the local version of the file for comparison
                                item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() { //Get the metadata of the item.
                                    @Override
                                    public void onSuccess(final StorageMetadata storageMetadata) {
                                        containerItems.add(new FirebaseItem(item,storageMetadata));
                                        if (localFile.lastModified() < storageMetadata.getUpdatedTimeMillis()) {    //if the file either doesn't exist locally, or is outdated.. we download
                                            localFile.delete(); //delete the old version
                                            item.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() { //download the file
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                     sendBroadCast(FILE_DOWNLOADED);
                                                }
                                            });
                                        } else if (localFile.lastModified() > storageMetadata.getUpdatedTimeMillis()) { //If we don't need to download the file, we do still need to
                                                                                    //add it to the list
                                        }
                                    }
                                });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() { //If it fails, oh well.
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sendBroadCast(FILE_DOWNLOAD_FAILED);
                        }
                    });
                }
            }
        });
    }

    public void updateFileList(){
        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                containerItems.clear();
                for(final StorageReference item : listResult.getItems()){
                    item.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata metadata) {
                            containerItems.add(new FirebaseItem(item,metadata));
                        }
                    });
                }

            }
        });
    }
    public void downloadFile(final String fileName){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getConnectedState()) {
                    StorageReference fileReference = storageReference.child("filename");
                    fileReference.getFile(new File(rootpath, fileName)).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            sendBroadCast(FILE_DOWNLOADED);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sendBroadCast(FILE_DOWNLOAD_FAILED);
                        }
                    });
                }
            }
        });
    }
    public void downloadFile(final StorageReference fileReference){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getConnectedState()) {
                    fileReference.getFile(new File(rootpath, fileReference.getName())).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            sendBroadCast(FILE_DOWNLOADED);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sendBroadCast(FILE_DOWNLOAD_FAILED);
                        }
                    });
                }
            }
        });
    }

    public void downloadFile(final String fileName, final StorageReference fileReference){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getConnectedState()) {
                    StorageReference newFileReference = fileReference.child(fileName);
                    newFileReference.getFile(new File(rootpath, fileReference.getName())).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            sendBroadCast(FILE_DOWNLOADED);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sendBroadCast(FILE_DOWNLOAD_FAILED);
                        }
                    });
                }
            }
        });
    }

    public void synchronizeFile(final String fileName){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getConnectedState()) {
                    new File(rootpath, fileName).delete();
                    StorageReference fileReference = storageReference.child("filename");
                    fileReference.getFile(new File(rootpath, fileName)).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            sendBroadCast(FILE_DOWNLOADED);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sendBroadCast(FILE_DOWNLOAD_FAILED);
                        }
                    });
                }
            }
        });
    }
    public void synchronizeFile(final StorageReference fileReference){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getConnectedState()) {
                    new File(rootpath, fileReference.getName()).delete();
                    fileReference.getFile(new File(rootpath, fileReference.getName())).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            sendBroadCast(FILE_DOWNLOADED);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sendBroadCast(FILE_DOWNLOAD_FAILED);
                        }
                    });
                }
            }
        });
    }

    public void synchronizeFile(final String fileName, final StorageReference fileReference){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (getConnectedState()) {
                    StorageReference newFileReference = fileReference.child(fileName);
                    new File(rootpath, fileName).delete();
                    newFileReference.getFile(new File(rootpath, fileReference.getName())).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            sendBroadCast(FILE_DOWNLOADED);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sendBroadCast(FILE_DOWNLOAD_FAILED);
                        }
                    });
                }
            }
        });
    }

    public void setAuthMode(int authMode){
        if(authMode == DISABLE_AUTHENTICATION_REQUIREMENTS || authMode == ENABLE_AUTHENTICATION_REQUIREMENTS) {
            this.authMode = authMode;
        }
    }

    public Set<File> fileDump(){
        Set<File> files = new HashSet<>();
        Iterator iterator = containerItems.iterator();
        while(iterator.hasNext()){
            FirebaseItem item = (FirebaseItem) iterator.next();
            files.add(new File(rootpath,item.getName()));
        }
        return files;
    }

    private void sendBroadCast(int extra){
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.setAction("com.firebasesync.downloadupdate");
        intent.putExtra("update",extra);
        context.sendBroadcast(intent);
    }

    private boolean getConnectedState(){
        final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); //Check if a network is active
        Network activeNetwork = cm.getActiveNetwork();
        return !(activeNetwork == null);
    }
    public int getSYNCHRONIZATION_FREQUENCY() {
        return SYNCHRONIZATION_FREQUENCY;
    }

    public void setSYNCHRONIZATION_FREQUENCY(int SYNCHRONIZATION_FREQUENCY) {
        this.SYNCHRONIZATION_FREQUENCY = SYNCHRONIZATION_FREQUENCY;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
    public Set<FirebaseItem> getDownloadedFiles() {
        return downloadedFiles;
    }
    public StorageReference getStorageReference() {
        return storageReference;
    }

    public void setStorageReference(StorageReference storageReference) {
        this.storageReference = storageReference;
    }
    public long getTotalDownloaded() {
        return totalDownloaded;
    }

    public void setTotalDownloaded(long totalDownloaded) {
        this.totalDownloaded = totalDownloaded;
    }
    public File getRootpath() {
        return rootpath;
    }

    public void setRootpath(File rootpath) {
        this.rootpath = rootpath;
    }

    public long getMaximumDownloadSize() {
        return maximumDownloadSize;
    }

    public void setMaximumDownloadSize(long maximumDownloadSize) {
        this.maximumDownloadSize = maximumDownloadSize;
    }

}
