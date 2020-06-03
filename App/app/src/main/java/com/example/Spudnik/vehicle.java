package com.example.Spudnik;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 *
 * This is vehicle super class. It will serve as a Parcelable container of all information partaining to a machine.
 * It also contains numerous methods for database construction.
 * On app startup, ArrayLists containing the valid vehicleIDs and dealerID's are created on an Asyc Thread
 * During inputserial activity, it will construct the most important data structure, the ArrayList of connections.
 * During Parcelable implementation, the object is essentially reset, and thus the HashMap of pinnumbers must be re-input when every new object is created.
 *
 * Another primary method is the sortconnections class described below
 */
public class vehicle implements Parcelable {

    private String vehicleId;
    private ArrayList<connection> connections = new ArrayList<>();
    private ArrayList<String> uniqueConnections = new ArrayList<>();
    private ArrayList<String> uniquePins = new ArrayList<>();
    private ArrayList<String> dealers = new ArrayList<>();
    private ArrayList<String> vehicleIds = new ArrayList<>();
    private int loc = 0,pinCount=0,lastSorted = SORT_BY_S4;
    private InputStream is;
    static final int SORT_BY_S4 = 1,SORT_BY_NAME = 2;
    private  Map<String,Integer> pinnumbers = new HashMap<>();

    vehicle(String id){
        this.vehicleId = id;
        setPinnumbers();

    }

    vehicle(){
        setPinnumbers();

    }


    /**
     * The following methods are all part of the Parcelable implementation.
     * @param in Parcel in
     */

    //parcelable constructor to rebuild object
    private vehicle(Parcel in) {
        vehicleId = in.readString();
        uniqueConnections = in.createStringArrayList();
        loc = in.readInt();
        pinCount = in.readInt();
        dealers = in.createStringArrayList();
        uniquePins = in.createStringArrayList();
        lastSorted = in.readInt();
        vehicleIds = in.createStringArrayList();
        setPinnumbers();
    }
    //writes to new object constructor ^
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicleId);
        dest.writeStringList(uniqueConnections);
        dest.writeInt(loc);
        dest.writeInt(pinCount);
        dest.writeStringList(dealers);
        dest.writeStringList(uniquePins);
        dest.writeInt(lastSorted);
        dest.writeStringList(vehicleIds);
    }
    //creator
    public static final Creator<vehicle> CREATOR = new Creator<vehicle>() {
        @Override
        public vehicle createFromParcel(Parcel in) {
            return new vehicle(in);
        }
        @Override
        public vehicle[] newArray(int size) {
            return new vehicle[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }



    /**
     * Primary database builder. This will fill the instance field ArrayList connections with connection objects. This should only run once
     * It will run on an async thread in the background to avoid UI thread blocking
     */
    void buildDataBase(){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String line;
                    while((line = reader.readLine()) != null) {
                        String[] tokens = line.split(",");
                        if (testConnection(vehicleId,tokens[0])) {
                            addConnection(new connection(tokens[0].toLowerCase(), tokens[1].toLowerCase(), tokens[2].toLowerCase(),
                                    tokens[3].toLowerCase(), tokens[4].toLowerCase(), tokens[5].toLowerCase()));
                            if(!getUniqueConnections().contains(tokens[1].toLowerCase())){
                                addUniqueconnection(tokens[1]);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    /**
     * Here is our database builder for the dealer id's. This is a small database but it is useful to keep it as a csv file to allow for updateability. It runs similarly to the one above
     * @param i InputStream
     */
    void buildDealers(final InputStream i){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8));
                    String line;
                    while((line = reader.readLine()) != null) {
                        line = line.toLowerCase();
                        if (!dealers.contains(line)) {
                            dealers.add(line);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This is the database builder of acceptable vehicle id numbers. Like the others above it is relegated to a background thread.
     * @param i Inputstream
     */

     void buildVehicleIds(final InputStream i){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8));
                    String line;
                    String[] holder;
                    while((line = reader.readLine()) != null) {
                        line = line.toLowerCase().trim();
                        holder = line.split(",");
                        if (!vehicleIds.contains(holder[0])) {
                            vehicleIds.add(holder[0]);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

     boolean checkDealer(String dealerid){
        return this.dealers.contains(dealerid);
    }

    /**
     * Used to determine if an input'ed vehicle id is valid.
     * @param vehicleid String
     * @param s String
     * @return boolean
     */
    private boolean testConnection(String vehicleid, String s){
        try {
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == 'X') {
                    return true;
                }
                if (i < s.length() - 1 && i > 2) {
                    if (s.charAt(i) == '5' && s.charAt(i + 1) == '5') {
                        return true;
                    }
                }
                if (s.charAt(i) != vehicleid.charAt(i)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Will return a string depending on whether or not the connection is input or output
     * @return String
     */
    String inout(){
        try {
            String temp = this.uniqueConnections.get(this.loc);
            if (temp.contains("in") || temp.contains("In")) {
                return "Input";
            } else {
                return "Output";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "NULL";
        }
    }

    /**
     * Sorting implementation using default Mergesort. Time complexity of Olog(n).
     * Users are allowed to sort the pins by either pin number or by name.
     * This is implemented using a switch and final constants.
     * Finally, since sorting is relegated to an asyc thread, an Intent is passed to the parent Activity by a broadcast manager
     * This is caught by a broadcast manager in the parent activity, and allows for the UI to be updated.
     * @param sort Sort-By
     * @param mcontext Application Context
     */
     void sortConnections(final int sort, final Context mcontext){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    lastSorted = sort;
                    if(!connections.isEmpty()) {
                        if (sort == SORT_BY_NAME) {
                            connection.setSortBy(SORT_BY_NAME);
                            Collections.sort(connections);
                        } else {
                            connection.setSortBy(SORT_BY_S4);
                            Collections.sort(connections);
                        }
                        Intent incomingMessageIntent = new Intent("incomingboolean");
                        incomingMessageIntent.putExtra("boolean",true);
                        LocalBroadcastManager.getInstance(mcontext).sendBroadcast(incomingMessageIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    /**
     * The rest of the methods here are support methods
     * Primarily get set and add methods.
     */
    int getMap(String direction){
        return this.pinnumbers.get(direction);
    }

    void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    int getPinCount() {
        return pinCount;
    }

    void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    private void addConnection(connection toAdd){
        this.connections.add(toAdd);
    }

    private void addUniqueconnection(String s){
        this.uniqueConnections.add(s.toLowerCase());
    }

    ArrayList<String> getUniqueConnections(){
        return this.uniqueConnections;
    }

    ArrayList<connection> getConnections(){return this.connections;}

    void setConnections(ArrayList<connection> c){ this.connections = c;}

    void addUniquePin(String s){this.uniquePins.add(s.toLowerCase().trim());}

    int getLoc() {
        return loc;
    }

    void setLoc(int loc) {
        this.loc = loc;
    }

    void setIs(InputStream s){
        this.is = s;
    }

    private void setPinnumbers(){
        this.pinnumbers.put("in1",14);
        this.pinnumbers.put("in2",14);
        this.pinnumbers.put("in3",22);
        this.pinnumbers.put("in4",22);
        this.pinnumbers.put("out1",24);
        this.pinnumbers.put("out2",24);
        this.pinnumbers.put("out3",24);
        this.pinnumbers.put("out4",2);
        this.pinnumbers.put("out5",2);
        this.pinnumbers.put("out6",2);
        this.pinnumbers.put("out7",2);
        this.pinnumbers.put("out8",2);
        this.pinnumbers.put("out9",2);
        this.pinnumbers.put("exp11_out",24);
        this.pinnumbers.put("exp11_in",22);
    }


    public String toString(){
        return ("Id: "+ this.vehicleId + "\n Connections: " + this.connections.size() + "\n Unique Connections: " + this.uniqueConnections.size());
    }

    ArrayList<String> getUniquePins() {
        return uniquePins;
    }

    ArrayList<String> getVehicleIds(){
        return this.vehicleIds;
    }
}
