package com.example.main;

import android.os.Parcel;
import android.os.Parcelable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

/* This vehicle class will contain all necessary information about the vehicle the user needs. It will receive a vehicle id from android kernel,
 * Search through for it. Then store information about each connection.
 */
public class vehicle  implements Parcelable {

    //vehicle id input by the user, passed through by the android kernel
    private String vehicleId;
    //arraylist containing connection objects. Will represent all possible connections on this machine.

    private ArrayList<connection> connections = new ArrayList<>();
    private ArrayList<String> uniqueConnections = new ArrayList<>();
    private ArrayList<String> uniquePins = new ArrayList<>();
    private ArrayList<String> dealers = new ArrayList<>();
    private int loc = 0,pinCount=0,lastSorted = this.SORT_BY_S4;
    private InputStream is;
    public static final int SORT_BY_DIRECTION = 0,SORT_BY_S4 = 1,SORT_BY_NAME = 2, SORT_BY_UNITS = 3, SORT_BY_TYPE = 4;
    private  Map<String,Integer> pinnumbers = new HashMap<>();

    vehicle(String id){
        this.vehicleId = id;

    }


    protected vehicle(Parcel in) {
        vehicleId = in.readString();
        uniqueConnections = in.createStringArrayList();
        loc = in.readInt();
        pinCount = in.readInt();
        dealers = in.createStringArrayList();
        uniquePins = in.createStringArrayList();
        lastSorted = in.readInt();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicleId);
        dest.writeStringList(uniqueConnections);
        dest.writeInt(loc);
        dest.writeInt(pinCount);
        dest.writeStringList(dealers);
        dest.writeStringList(uniquePins);
        dest.writeInt(lastSorted);
    }

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
     * Just don't touch it
     */
    protected void buildDataBase(){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String line;
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (testConnection(vehicleId,tokens[0])) {
                    //String id,String dir, String s, String nm, String un, String type
                    this.addConnection(new connection(tokens[0].toLowerCase(), tokens[1].toLowerCase(), tokens[2].toLowerCase(),
                            tokens[3].toLowerCase(), tokens[4].toLowerCase(), tokens[5].toLowerCase()));
                    if(!this.getUniqueConnections().contains(tokens[1].toLowerCase())){
                        this.addUniqueconnection(tokens[1]);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void buildDealers(InputStream i){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(i, Charset.forName("UTF-8")));
            String line;
            while((line = reader.readLine()) != null) {
                line = line.toLowerCase().trim();
                if (!this.dealers.contains(line)) {
                    this.dealers.add(line);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected boolean checkDealer(String dealerid){
        return this.dealers.contains(dealerid);
    }

    //This will test whether or not an input'ed machine id is valid or not.
    protected boolean testConnection(String vehicleid, String s){
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
    //Will return whether or not a specific connection is an input or output connection
    protected String inout(){
        try {
            String temp = this.getUniqueConnections().get(this.loc).toLowerCase();
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
    protected void sortConnections(int sortby){
        try{
        if(!connections.isEmpty()) {
            switch (sortby) {
                case SORT_BY_DIRECTION:
                    connection.setSortBy(SORT_BY_DIRECTION);
                    Collections.sort(connections);
                    break;
                case SORT_BY_NAME:
                    connection.setSortBy(SORT_BY_NAME);
                    Collections.sort(connections);
                    break;
                case SORT_BY_TYPE:
                    connection.setSortBy(SORT_BY_TYPE);
                    Collections.sort(connections);
                    break;
                case SORT_BY_UNITS:
                    connection.setSortBy(SORT_BY_UNITS);
                    Collections.sort(connections);
                    break;
                default:
                    connection.setSortBy(SORT_BY_S4);
                    Collections.sort(connections);
            }
        }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public int getMap(String direction){
        return this.pinnumbers.get(direction.toLowerCase());
    }
    public void addMap(String direction,int pins){
        this.pinnumbers.put(direction,pins);
    }

    protected void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    protected String getVehicleId(){
        return this.vehicleId;
    }

    protected int getPinCount() {
        return pinCount;
    }

    protected void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    protected void addConnection(connection toAdd){
        this.connections.add(toAdd);
    }
    protected void addUniqueconnection(String s){
        this.uniqueConnections.add(s.toLowerCase());
    }
    protected ArrayList<String> getUniqueConnections(){
        return this.uniqueConnections;
    }
    protected ArrayList<connection> getConnections(){return this.connections;}
    protected void setConnections(ArrayList<connection> c){ this.connections = c;}
    protected void addUniquePin(String s){this.uniquePins.add(s.toLowerCase().trim());}
    //String id,String dir, String s, String nm, String un, String type
    protected int getLoc() {
        return loc;
    }

    protected void setLoc(int loc) {
        this.loc = loc;
    }

    protected void setDealers(ArrayList<String> dealers){
        this.dealers = dealers;
    }
    protected ArrayList<String> getDealer(){
        return this.dealers;
    }

    protected void setIs(InputStream s){
        this.is = s;
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

    protected ArrayList<String> getUniquePins() {
        return uniquePins;
    }

    protected void setUniquePins(ArrayList<String> uniquePins) {
        this.uniquePins = uniquePins;
    }

    public int getLastSorted() {
        return lastSorted;
    }

    public void setLastSorted(int lastSorted) {
        this.lastSorted = lastSorted;
    }
}
