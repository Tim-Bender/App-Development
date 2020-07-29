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

import android.content.Context;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Parcelable container of all information partaining to a machine.
 * Contains numerous methods for database construction.
 * On app startup, ArrayLists containing the valid vehicleIDs and dealerID's are created on an Async Thread
 * During inputserial activity, it will construct the most important data structure, the ArrayList of connections.
 * During Parcelable implementation, the object is essentially reset, and thus the HashMap of pinnumbers must be re-input when every new object is created.
 * <p>
 * Another primary method is the sortconnections class described below
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see android.os.Parcelable
 * @see Connection
 * @since dev 1.0.0
 */
public class vehicle implements Parcelable { //Parcelable implementation allows cross Activity passing of this object
    /**
     * Vehicle id string
     */
    private String vehicleId;
    /**
     * Stores all connections. Is build inside of buildDataBase() method
     */
    private ArrayList<Connection> Connections = new ArrayList<>(36);
    /**
     * Contains the unique "directions" I.E. In1, out2 etc
     */
    private ArrayList<String> uniqueConnections = new ArrayList<>(24);
    /**
     * This contains all of the unique pins. Used to filter out duplicate pin entries
     */
    private ArrayList<String> uniquePins = new ArrayList<>(24);
    /**
     * List of acceptable dealer id's
     */
    private ArrayList<String> dealers = new ArrayList<>(10); //this stores the dealerids
    /**
     * List of acceptable vehicle id numbers
     */
    private ArrayList<String> vehicleIds = new ArrayList<>(15); //this stores the vehicle ids
    /**
     * Used to store which connection the user is currently viewing
     */
    private int loc = 0;
    /**
     * Stores the total number of pins
     */
    private int pinCount = 0;
    /**
     * Inputstreamreader holder
     */
    private InputStreamReader isr;
    /**
     * Map used to match connections with their pin arrangement number. I.E. Out1 is a 24pin connector....
     */
    private Map<String, Integer> pinnumbers = new HashMap<>(15);

    /**
     * Constructor, not very interesting. The pinnumber map must be filled, so we call the method which completes that.
     *
     * @since dev 1.0.0
     */
    public vehicle() {
        setPinnumbers(); //this must be done every time we create a new object
    }


    /**
     * The following methods are all part of the Parcelable implementation.
     *
     * @param in Parcel in
     */

    private vehicle(Parcel in) {
        vehicleId = in.readString();
        uniqueConnections = in.createStringArrayList();
        loc = in.readInt();
        pinCount = in.readInt();
        dealers = in.createStringArrayList();
        uniquePins = in.createStringArrayList();
        vehicleIds = in.createStringArrayList();
        setPinnumbers(); //pinnumbers map must be refilled each time
    }

    /**
     * Writes values to parcel
     *
     * @param dest  Parcel
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
     *
     * @return int
     */
    @Override
    public int describeContents() {
        return 0;
    }


    /**
     * Primary database builder. This will fill the instance field ArrayList connections with connection objects. This should only run once
     * It will run on an async thread in the background to avoid UI thread blocking
     *
     * @since dev 1.0.0
     */
    public void buildDataBase() {
        Connections.clear(); //clear the old connections and uniqueconnections arraylists to avoid overlap
        uniqueConnections.clear();
        //Asynchronous of course
        AsyncTask.execute(() -> {
            try {
                BufferedReader reader = new BufferedReader(isr); //Reader
                String line;  //string to the store the line read by buffered reader
                while ((line = reader.readLine()) != null) { //Until we run out of lines, read lines.
                    String[] tokens = line.toLowerCase().split(","); //split the line at commas, and lowercase everything
                    addConnection(new Connection(vehicleId, tokens[0], tokens[1],
                            tokens[2], tokens[3], tokens[4])); //build the new connection
                    if (!getUniqueConnections().contains(tokens[0])) //if we have a unique connection, then we add it to the list.
                        addUniqueconnection(tokens[0]);
                }
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * Here is our database builder for the dealer id's. This is a small database but it is useful to keep it as a csv file to allow for update-ability. It runs similarly to the one above
     *
     * @param i InputStream
     * @since dev 1.0.0
     */
    private void buildDealers(@NonNull final InputStreamReader i) {
        dealers.clear(); //clear the dealer's arraylist so we dont have memory overlap
        AsyncTask.execute(() -> {
            try {
                BufferedReader reader = new BufferedReader(i); //build our new bufferedreader
                String line; //string to store each line
                while ((line = reader.readLine()) != null) { //read each line
                    line = line.toLowerCase(); //cast it to lowercase
                    if (!dealers.contains(line))  //if it is a unique id we add it to the list
                        dealers.add(line);
                }
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * This is the database builder of acceptable vehicle id numbers. Like the others above it is relegated to a background thread.
     *
     * @param i Inputstream
     * @since dev 1.0.0
     */
    private void buildVehicleIds(@NonNull final InputStreamReader i) {
        vehicleIds.clear(); //wipe the old vehicle ids just to be sure
        AsyncTask.execute(() -> { //lambda
            try {
                String line = new BufferedReader(i).readLine(); //initiate bufferedreader readline and grab the first line
                String[] holder = line.toLowerCase().split(","); //split items on comma's and cast into string array
                for (String s : holder) { //iterate through each string within the array
                    if (!vehicleIds.contains(s))  //if the vehicleid is unique we add it to the list
                        vehicleIds.add(s);
                }
            } catch (Exception ignored) {
            }
        });
    }

    /**
     * This will check if a passed dealerid is in the list of acceptable ids
     *
     * @param dealerid String
     * @return boolean
     * @since dev 1.0.0
     */
    public boolean checkDealer(@NonNull String dealerid) {
        return dealers.contains(dealerid);
    }

    /**
     * This method will determine the closest match vehicle id from the id that was entered. It will then return that id.
     *
     * @param machineid MachineId
     * @return String
     * @since dev 1.0.0
     */
    public String determineComparison(@NonNull String machineid) {
        int maximum = Integer.MIN_VALUE;
        String toReturn = "null"; //default return if we dont match with anything
        if (machineid.length() > 0 && !vehicleIds.isEmpty()) {
            for (String id : vehicleIds) { //we will iterate through the ids
                char[] storage = machineid.toCharArray(), //cast the two into char arrays
                        idCharArray = id.toCharArray();
                int points = 0; //higher points of comparison the better for the id
                if (idCharArray[0] != storage[0] || storage.length != idCharArray.length) //if the first letters are not the same, or they aren't the same length. Skip it
                    continue;
                for (int i = 0; i < idCharArray.length; i++) { //iterate through every character
                    if (i < storage.length) {
                        if (storage[i] == idCharArray[i]) //if theres a character match the id earns a point
                            points++;
                        else if (idCharArray[i] != 'x' && idCharArray[i] != 'X') //if they dont match, but we are comparing against an x or an X then they don't lose a point
                            points--; //take a point away.
                    }
                }
                if (maximum < points) { //if we have a new maximum match set the variables
                    maximum = points;
                    toReturn = id;
                }
            }
            if (maximum <= 0) //if we have 0 or fewer comparison points, then there was no match
                return toReturn;
        }
        return toReturn;
    }

    /**
     * This method will pre-build the vehicle object by constructing the preliminary lists of acceptable
     * vehicleids and dealerids. This should be completed before inputserial.class is ever started.
     *
     * @param context Context
     * @since dev 1.0.0
     */
    public void preBuildVehicleObject(@NonNull final Context context) {
        try {
            buildVehicleIds(new InputStreamReader(new FileInputStream(new File(new File(  //create and pass inputstreamreaders to the appropriate methods
                    context.getFilesDir(), "database"), "machineids"))));
            buildDealers(new InputStreamReader(new FileInputStream(new File(new File( //build the dealers, create a new fileinputstream
                    context.getFilesDir(), "database"), "dealerids"))));
        } catch (IOException ignored) {
        }
    }

    /**
     * Will return a string depending on whether or not the connection is input or output
     *
     * @return "Input" or "Output"
     * @since dev 1.0.0
     */
    public String inout() {
        String temp = uniqueConnections.get(loc), toReturn;
        toReturn = (temp.contains("in") || temp.contains("In")) ? "Input" : "Output"; //ternary operator
        return toReturn;
    }

    /**
     * Sorting implementation using default Mergesort. Time complexity of Olog(n).
     * Users are allowed to sort the pins by either pin number or by name.
     * This is implemented using a switch and final constants.
     *
     * @since dev 1.0.0
     */
    public void sortConnections() {
        Collections.sort(Connections); //sort them
    }

    /**
     * This method will fill the pinnumbers hashmap. This map is used to map connectors to their total pin numbers.
     *
     * @since dev 1.0.0
     */
    private void setPinnumbers() {
        pinnumbers.put("in1", 14);
        pinnumbers.put("in2", 14);
        pinnumbers.put("in3", 22);
        pinnumbers.put("in4", 22);
        pinnumbers.put("out1", 24);
        pinnumbers.put("out2", 24);
        pinnumbers.put("out3", 24);
        pinnumbers.put("out4", 2);
        pinnumbers.put("out5", 2);
        pinnumbers.put("out6", 2);
        pinnumbers.put("out7", 2);
        pinnumbers.put("out8", 2);
        pinnumbers.put("out9", 2);
        pinnumbers.put("exp11_out", 24);
        pinnumbers.put("exp11_in", 22);
    }

    /**
     * The rest of the methods here are support methods
     * Primarily get set and add methods.
     */
    public int getMap(String direction) throws NullPointerException {
        return pinnumbers.get(direction);
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public int getPinCount() {
        return pinCount;
    }

    public void setPinCount(int pinCount) {
        this.pinCount = pinCount;
    }

    private void addConnection(Connection toAdd) {
        this.Connections.add(toAdd);
    }

    private void addUniqueconnection(String s) {
        this.uniqueConnections.add(s.toLowerCase());
    }

    public ArrayList<String> getUniqueConnections() {
        return this.uniqueConnections;
    }

    public ArrayList<Connection> getConnections() {
        return Connections;
    }

    public void setConnections(ArrayList<Connection> c) {
        Connections = c;
    }

    public void addUniquePin(String s) {
        uniquePins.add(s.toLowerCase().trim());
    }

    public int getLoc() {
        return loc;
    }

    public void setLoc(int loc) {
        this.loc = loc;
    }

    public void setIs(InputStreamReader s) {
        this.isr = s;
    }

    public String toString() {
        return ("Id: " + vehicleId + "\n Connections: " + Connections.size() + "\n Unique Connections: " + uniqueConnections.size());
    }

    public ArrayList<String> getUniquePins() {
        return uniquePins;
    }

    public ArrayList<String> getVehicleIds() {
        return this.vehicleIds;
    }
}
