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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

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
 * @see Pin
 * @since dev 1.0.0
 */
public class Vehicle implements Parcelable { //Parcelable implementation allows cross Activity passing of this object

    /**
     * Vehicle id string
     */
    private String vehicleId;
    /**
     * Stores all connections. Is build inside of buildDataBase() method
     */
    private ArrayList<Pin> pins = new ArrayList<>(36);
    /**
     * Contains the unique "directions" I.E. In1, out2 etc
     */
    private ArrayList<String> uniqueConnections = new ArrayList<>(24);
    /**
     * This contains all of the unique pins. Used to filter out duplicate pin entries
     */
    private ArrayList<String> uniquePins = new ArrayList<>(24);
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
    public final static String buildDoneAction = "com.Diagnostic.Spundik.buildDoneAction";
    public final static int BUILD_SUCCESSFUL = 1;

    /**
     * Constructor, not very interesting
     *
     * @since dev 1.0.0
     */
    public Vehicle() {
    }


    /**
     * The following methods are all part of the Parcelable implementation.
     *
     * @param in Parcel in
     */

    private Vehicle(Parcel in) {
        vehicleId = in.readString();
        uniqueConnections = in.createStringArrayList();
        loc = in.readInt();
        pinCount = in.readInt();
        uniquePins = in.createStringArrayList();
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
        dest.writeStringList(uniquePins);
    }

    /**
     * Creator of the parcelable object. Just don't touch it
     */
    public static final Creator<Vehicle> CREATOR = new Creator<Vehicle>() {
        @Override
        public Vehicle createFromParcel(Parcel in) {
            return new Vehicle(in);
        }

        @Override
        public Vehicle[] newArray(int size) {
            return new Vehicle[size];
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
    public void buildDataBase(@NonNull Context context) {
        AsyncTask.execute(() -> {
            if (isr != null) {
                pins.clear(); //clear the old connections and uniqueconnections arraylists to avoid overlap
                uniqueConnections.clear();
                try {
                    BufferedReader reader = new BufferedReader(isr); //Reader
                    String line;  //string to the store the line read by buffered reader
                    while ((line = reader.readLine()) != null) { //Until we run out of lines, read lines.
                        String[] tokens = line.toLowerCase().split(","); //split the line at commas, and lowercase everything
                        if (tokens.length == 5)
                            addConnection(new Pin(vehicleId, tokens[0], tokens[1],
                                    tokens[2], tokens[3], tokens[4])); //build the new connection
                        if (!getUniqueConnections().contains(tokens[0])) //if we have a unique connection, then we add it to the list.
                            addUniqueconnection(tokens[0]);
                    }
                    context.sendBroadcast(new Intent(buildDoneAction).putExtra("success", BUILD_SUCCESSFUL));
                    reader.close();
                    isr.close();
                } catch (Exception ignored) {
                }
            }
        });
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
        Collections.sort(pins); //sort them
    }

    /**
     * The rest of the methods here are support methods
     * Primarily get set and add methods.
     */
    public int getPinCount(String direction) throws NullPointerException {
        if (!direction.contains("exp")) {
            int number = Integer.parseInt(String.valueOf(direction.charAt(direction.length() - 1)));
            if (direction.contains("in")) {
                if (number < 3)
                    return 14;
                else
                    return 22;
            } else if (direction.contains("out")) {
                if (number < 4)
                    return 24;
                else
                    return 2;
            }
        } else {
            if (direction.contains("in"))
                return 22;
            else
                return 24;
        }
        return 0;
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

    private void addConnection(Pin toAdd) {
        this.pins.add(toAdd);
    }

    private void addUniqueconnection(String s) {
        this.uniqueConnections.add(s.toLowerCase());
    }

    public ArrayList<String> getUniqueConnections() {
        return this.uniqueConnections;
    }

    public ArrayList<Pin> getPins() {
        return pins;
    }

    public void setPins(ArrayList<Pin> c) {
        pins = c;
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
        return ("Id: " + vehicleId + "\n Connections: " + pins.size() + "\n Unique Connections: " + uniqueConnections.size());
    }

    public ArrayList<String> getUniquePins() {
        return uniquePins;
    }
    public String getVehicleId() {
        return vehicleId;
    }
}
