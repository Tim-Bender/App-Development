package com.Diagnostic.Spudnik;

import android.os.Parcel;
import android.os.Parcelable;

public class connection implements Parcelable,Comparable<connection> {
    /**
     * Author: Timothy Bender
     * timothy.bender@spudnik.com
     * 530-414-6778
     * Please see README before updating anything

     * This connection object will hold data about an individual connection. vehicle.java contains an arraylist of these objects which represents all the connections on a machine
     * Parcelable is implemented here to allow for transfer of an list of connections between activities.
     * Comparable is also implemented here to allow for easy sorting via the vehicle sort method.
     */
    private String id,direction,name,units,plug,s4,type;
    private static final int SORT_BY_NAME = 2;
    private static int SORT_BY = 0;

    connection(String id,String dir, String s, String nm, String un, String type){
        this.id = id;
        this.direction = dir;
        this.name = nm;
        this.units = un;
        this.s4 = s;
        this.type = type;
    }

    /**
     * Heres the parcelable implementation
     * @param in Parcel
     */

    private connection(Parcel in) {
        id = in.readString();
        direction = in.readString();
        name = in.readString();
        units = in.readString();
        plug = in.readString();
        s4 = in.readString();
        type = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(direction);
        dest.writeString(name);
        dest.writeString(units);
        dest.writeString(plug);
        dest.writeString(s4);
        dest.writeString(type);
    }

    public static final Creator<connection> CREATOR = new Creator<connection>() {
        @Override
        public connection createFromParcel(Parcel in) {
            return new connection(in);
        }

        @Override
        public connection[] newArray(int size) {
            return new connection[size];
        }
    };

    /**
     * Here is the most important compareTo method. If in compare by name mode, it will cast the names of both objects into chararrays and compare them alphabetically
     * If it is in S4 mode then it converts the S4's of both connections into integers, then uses the default Integer.compare() method.
     * @param o Connection
     * @return int
     */
    @Override
    public int compareTo(connection o){
        int comparison = 0;
        try{
            if(o != null){
                if (SORT_BY == SORT_BY_NAME) {
                    int counter = 0;
                    char[] lArray;
                    char[] oArray;
                    lArray = this.name.toCharArray();
                    oArray = o.name.toCharArray();
                    while (comparison == 0 && counter < lArray.length && counter < oArray.length) {
                        comparison = Character.compare(lArray[counter], oArray[counter]);
                        counter++;
                    }
                    return comparison;
                }
                return Integer.compare(Integer.parseInt(this.s4), Integer.parseInt(o.s4));
            }
        } catch (Exception ignored) {

        }
        return comparison;

    }

    String inout(){
        try {
            String temp = this.direction;
            if (temp.contains("in") || temp.contains("In")) {
                return "Input";
            } else {
                return "Output";
            }
        } catch (Exception e) {
            return "NULL";
        }
    }

    static void setSortBy(int sortBy) {
        SORT_BY = sortBy;
    }

    public String toString(){
        return (this.id+" " + this.direction + " " + this.name + " " + this.units + " " + this.units + " " + this.type);
    }

    //set methods
    public void setId(String id) {
        this.id = id;
    }


    public void setName(String name) {
        this.name = name;
    }


    //get methods
    public String getId() {
        return id;
    }

    String getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    String getS4() {
        return s4;
    }

}
