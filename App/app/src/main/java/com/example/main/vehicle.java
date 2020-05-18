package com.example.main;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

/* This vehicle class will contain all necessary information about the vehicle the user needs. It will receive a vehicle id from android kernel,
 * Search through for it. Then store information about each connection.
 */
public class vehicle implements Parcelable {

    //vehicle id input by the user, passed through by the android kernel
    private String vehicleId;
    //arraylist containing connection objects. Will represent all possible connections on this machine.

    private ArrayList<connection> connections = new ArrayList<connection>();
    private ArrayList<String> uniqueConnections = new ArrayList<>();
    private ArrayList<Integer> pinCounts = new ArrayList<>();
    private int loc = 0;
    private InputStream is;
    //Overloaded constructor. Either input the machine id now or do it later using set methods. Don't forget that the database scanner needs to run!
    vehicle(String id){
        this.vehicleId = id;
    }
    vehicle(){} //DO NOT DELETE THIS IT MIGHT NEED TO BE USED

    /**
     * The vehicle class is parcelable, meaning it can be passed between activities
     * @param in
     */
    protected vehicle(Parcel in) {
        vehicleId = in.readString();
        uniqueConnections = in.createStringArrayList();
        loc = in.readInt();
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicleId);
        dest.writeStringList(uniqueConnections);
        dest.writeInt(loc);
    }


    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * Just don't touch it
     */
    public void buildDataBase(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line;

        try{
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (testConnection(vehicleId,tokens[0])) {
                    //String id,String dir, String s, String nm, String un, String type
                    this.addConnection(new connection(tokens[0].toLowerCase(), tokens[1].toLowerCase(), tokens[2].toLowerCase(),
                            tokens[3].toLowerCase(), tokens[4].toLowerCase(), tokens[5].toLowerCase()));
                    if(!this.getUniqueConnections().contains(tokens[1].toLowerCase())){
                        this.addUniqueconnection(tokens[1]);
                        this.addPinCount(1);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This will test whether or not an input'ed machine id is valid or not.
    public boolean testConnection(String vehicleid, String s){
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == 'X'){
                return true;
            }
            if(i<s.length()-1 && i > 2){
                if(s.charAt(i) == '5' && s.charAt(i+1) == '5'){
                    return true;
                }
            }
            if(s.charAt(i) != vehicleid.charAt(i)){
                return false;
            }
        }
        return true;
    }
    public String getVehicleId(){
        return this.vehicleId;
    }

    public void addConnection(connection toAdd){
        this.connections.add(toAdd);
    }
    public void addUniqueconnection(String s){
        this.uniqueConnections.add(s.toLowerCase());
    }
    public ArrayList<String> getUniqueConnections(){
        return this.uniqueConnections;
    }
    public ArrayList<connection> getConnections(){return this.connections;}
    public void addPinCount(int i){this.pinCounts.add(1);}
    //String id,String dir, String s, String nm, String un, String type
    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public void setIs(InputStream s){
        this.is = s;
        if(this.is != null) {
            buildDataBase();
        }
    }
    //Will return whether or not a specific connection is an input or output connection
    public String inout(){
        String temp = this.getUniqueConnections().get(this.loc).toLowerCase();
        if(temp.contains("in") || temp.contains("In")){
            return "Input";
        }
        else{
            return "Output";
        }
    }
    public String toString(){
        return ("Id: "+ this.vehicleId + "\n Connections: " + this.connections.size() + "\n Unique Connections: " + this.uniqueConnections.size());
    }

    public ArrayList<Integer> getPinCounts() {
        return pinCounts;
    }

    public void setPinCounts(ArrayList<Integer> pinCounts) {
        this.pinCounts = pinCounts;
    }
}
