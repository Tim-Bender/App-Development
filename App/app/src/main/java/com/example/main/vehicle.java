package com.example.main;
import android.os.Parcel;
import android.os.Parcelable;
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
    private ArrayList<String> uniqueConnections = new ArrayList<String>();
    //Overloaded constructor. Either input the machine id now or do it later using set methods. Don't forget that the database scanner needs to run!
    vehicle(String id){
        this.vehicleId = id;
    }
    vehicle(){}

    protected vehicle(Parcel in) {
        vehicleId = in.readString();
        uniqueConnections = in.createStringArrayList();
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

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    public String getVehicleId(){
        return this.vehicleId;
    }

    public void addConnection(connection connection){
        this.connections.add(connection);
    }
    public void addUniqueconnection(String s){
        this.uniqueConnections.add(s);
    }
    public ArrayList<String> getUniqueConnections(){
        return this.uniqueConnections;
    }
    public ArrayList<connection> getConnections(){return this.connections;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vehicleId);
        dest.writeStringList(uniqueConnections);
    }
}
