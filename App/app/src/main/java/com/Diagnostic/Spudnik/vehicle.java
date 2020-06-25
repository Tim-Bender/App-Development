package com.Diagnostic.Spudnik;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.auth.FirebaseAuth;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author timothy.bender
 * @version dev1.0.0
 * This is vehicle super class. It will serve as a Parcelable container of all information partaining to a machine.
 * It also contains numerous methods for database construction.
 * On app startup, ArrayLists containing the valid vehicleIDs and dealerID's are created on an Asyc Thread
 * During inputserial activity, it will construct the most important data structure, the ArrayList of connections.
 * During Parcelable implementation, the object is essentially reset, and thus the HashMap of pinnumbers must be re-input when every new object is created.
 *
 * Another primary method is the sortconnections class described below
 */
public class vehicle implements Parcelable { //Parcelable implementation allows cross Activity passing of this object

    private String vehicleId;
    private ArrayList<connection> connections = new ArrayList<>(); //This stores all of the connections
    private ArrayList<String> uniqueConnections = new ArrayList<>(); //Contains the unique "directions" Aka In1, Out2.... Used in connectorselect.class
    private ArrayList<String> uniquePins = new ArrayList<>(); //This will contain
    private ArrayList<String> dealers = new ArrayList<>(); //this stores the dealerids
    private ArrayList<String> vehicleIds = new ArrayList<>(); //this stores the vehicle ids
    private int loc = 0,pinCount=0,lastSorted = SORT_BY_S4;
    private InputStreamReader isr;
    private static final int SORT_BY_S4 = 1;
    private Map<String,Integer> pinnumbers = new HashMap<>();

    vehicle(){
        setPinnumbers(); //this must be done every time we create a new object
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
    /**
     * Writes values to parcel
     * @param dest Parcel
     * @param flags flags
     */
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

    /**
     * Creator of the parcelable object. Just don't touch it
     */
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

    /**
     * Don't touch this either... Just leave it as the defaults.
     * @return int
     */
    @Override
    public int describeContents() {
        return 0;
    }



    /**
     * Primary database builder. This will fill the instance field ArrayList connections with connection objects. This should only run once
     * It will run on an async thread in the background to avoid UI thread blocking
     */
    void buildDataBase(){
        connections.clear();
        uniqueConnections.clear();
        AsyncTask.execute(new Runnable() { //Asynchronous of course
            @Override
            public void run() {
                try{
                    BufferedReader reader = new BufferedReader(isr); //Reader
                    String line;
                    while((line = reader.readLine()) != null) { //Until we run out of lines, read lines.
                        String[] tokens = line.toLowerCase().split(","); //split the line at commas, and lowercase everything
                        addConnection(new connection(vehicleId, tokens[0], tokens[1],
                                tokens[2], tokens[3], tokens[4])); //build the new connection
                        if(!getUniqueConnections().contains(tokens[0])){
                            addUniqueconnection(tokens[0]);
                        }
                    }

                } catch (Exception ignored) {}
            }

        });
    }

    /**
     * Here is our database builder for the dealer id's. This is a small database but it is useful to keep it as a csv file to allow for update-ability. It runs similarly to the one above
     * @param i InputStream
     */
    void buildDealers(final InputStreamReader i){
        dealers.clear();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    BufferedReader reader = new BufferedReader(i);
                    String line;
                    while((line = reader.readLine()) != null) {
                        line = line.toLowerCase();
                        if (!dealers.contains(line)) {
                            dealers.add(line);
                        }
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    /**
     * This is the database builder of acceptable vehicle id numbers. Like the others above it is relegated to a background thread.
     * @param i Inputstream
     */

    void buildVehicleIds(final InputStreamReader i){
        vehicleIds.clear();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    String line = new BufferedReader(i).readLine();
                    String[] holder = line.toLowerCase().split(",");
                    for(String s : holder) {
                        if (!vehicleIds.contains(s)) {
                            vehicleIds.add(s);
                        }
                    }
                } catch (Exception ignored) {}
            }
        });
    }

    /**
     * This will check if a passed dealerid is in the list of acceptable ids
     * @param dealerid String
     * @return boolean
     */

    boolean checkDealer(String dealerid){
        return dealers.contains(dealerid);
    }

    /**
     * This method will determine the closest match vehicle id from the id that was entered. It will then return that id.
     * @param machineid MachineId
     * @return String
     */
    public String determineComparison(String machineid){
        int maximum = - 16;
        String toReturn = "null";
        if(machineid.length() > 0 && !vehicleIds.isEmpty()) {
            for (String id : vehicleIds) { //we will iterate through the ids
                char[] storage = machineid.toCharArray(), //cast the two into char arrays
                        idCharArray = id.toCharArray();
                int points = 0; //higher points of comparison the better for the id
                if (idCharArray[0] != storage[0] || storage.length != idCharArray.length) { //if the first letters are not the same, or they aren't the same length. Skip it
                    continue;
                }
                for (int i = 0; i < idCharArray.length; i++) { //iterate through every character
                    if (i < storage.length) {
                        if (storage[i] == idCharArray[i]) { //if theres a character match the id earns a point
                            points++;
                        } else {
                            if (idCharArray[i] != 'x' && idCharArray[i] != 'X') //if they dont match, but we are comparing against an x or an X then they don't lose a point
                                points--; //take a point away.
                        }
                    }
                }
                if (maximum < points) {
                    maximum = points;
                    toReturn = id;
                }
            }
            if(maximum <= 0){ //if we have 0 or fewer comparison points, then there was no match
                return "null";
            }
        }
        return toReturn;
    }

    /**
     * This method will pre-build the vehicle object by constructing the preliminary lists of acceptable
     * vehicleids and dealerids. This should be completed before inputserial.class is ever started.
     * @param context Context
     */
    public void preBuildVehicleObject(final Context context){
        if(FirebaseAuth.getInstance().getCurrentUser() != null) { //If the user is authenticated, then we begin.
            AsyncTask.execute(new Runnable() {  //All this building will be done asynchronously
                @Override
                public void run() {
                    try {
                        AsyncTask.execute(new Runnable() { //this second thread is necessary
                            @Override
                            public void run() {
                                try {
                                    buildVehicleIds(new InputStreamReader(new FileInputStream(      //create and pass inputstreamreaders to the appropriate methods
                                            new File(context.getFilesDir(),"machineids"))));
                                } catch (Exception ignored) {}
                            }
                        });
                        buildDealers(new InputStreamReader(context.getResources().openRawResource(R.raw.dealerids))); //dealerids will come pre-packaged with the app...
                    } catch (Exception ignored) {}
                }
            });
        }
    }

    /**
     * Will return a string depending on whether or not the connection is input or output
     * @return String
     */
    String inout(){
        String temp = this.uniqueConnections.get(this.loc),toReturn;
        toReturn = (temp.contains("in") || temp.contains("In")) ? "Input" : "Output"; //ternary operator
        return toReturn;
    }

    /**
     * Sorting implementation using default Mergesort. Time complexity of Olog(n).
     * Users are allowed to sort the pins by either pin number or by name.
     * This is implemented using a switch and final constants.
     * Finally, since sorting is relegated to an asyc thread, an Intent is passed to the parent Activity by a broadcast manager
     * This is caught by a broadcast manager in the parent activity, and allows for the UI to be updated.
     * @param mycontext Application Context
     */
    void sortConnections(final Context mycontext){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                lastSorted = vehicle.SORT_BY_S4;
                connection.setSortBy(SORT_BY_S4);
                Collections.sort(connections);
                Intent incomingMessageIntent = new Intent("incomingboolean");
                incomingMessageIntent.putExtra("boolean",true);
                LocalBroadcastManager.getInstance(mycontext).sendBroadcast(incomingMessageIntent);
            }
        });

    }

    /**
     * The rest of the methods here are support methods
     * Primarily get set and add methods.
     */
    int getMap(String direction) throws NullPointerException{
        return pinnumbers.get(direction);

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

    void setIs(InputStreamReader s){
        this.isr = s;
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
