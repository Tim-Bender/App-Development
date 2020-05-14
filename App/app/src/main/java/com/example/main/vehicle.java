package com.example.main;

import java.util.ArrayList;
import java.sql.*;
/**
 * Author: Timothy Bender
 * timothy.bender@spudnik.com
 * 530-414-6778
 * Please see README before updating anything
 */

/* This vehicle class will contain all necessary information about the vehicle the user needs. It will receive a vehicle id from android kernel,
 * Search through for it. Then store information about each connection.
 */
public class vehicle {

    //vehicle id input by the user, passed through by the android kernel
    private String vehicleId;
    //arraylist containing connection objects. Will represent all possible connections on this machine.
    private ArrayList<connection> connections = new ArrayList<connection>();

    //Overloaded constructor. Either input the machine id now or do it later using set methods. Don't forget that the database scanner needs to run!
    vehicle(String id){
        this.vehicleId = id;
    }
    vehicle(){}

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }
    public String getVehicleId(){
        return this.vehicleId;
    }

    /**
     * DO NOT UPDATE THIS METHOD WITHOUT THINKING IT THROUGH COMPLETELY. This method is in charge of all database retrieval!
     */
    private void readDataBase(){

    }


}
