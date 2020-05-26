package com.example.main;

import android.os.Parcel;
import android.os.Parcelable;

public class connection implements Parcelable,Comparable<connection> {
    /**
     * Author: Timothy Bender
     * timothy.bender@spudnik.com
     * 530-414-6778
     * Please see README before updating anything
     */

    /* This connection class will hold in memory the information for one pin connection.
     * This class will be used by vehicle, in an array list implementation to store all the connections for one machine.
     */
    private String id,direction,name,units,plug,s4,type;
    public static final int SORT_BY_DIRECTION = 0,SORT_BY_S4 = 1,SORT_BY_NAME = 2, SORT_BY_UNITS = 3, SORT_BY_TYPE = 4;
    private static int SORT_BY = 0;
    //construtor is overloaded. Either pass in no data, and add later using add/getter methods, or pass it all in at the same time.
    connection(String id,String dir, String s, String nm, String un, String type){
        this.id = id;
        this.direction = dir;
        this.name = nm;
        this.units = un;
        this.s4 = s;
        this.type = type;
    }
    //overloaded default constructor. If using this, you must add all the information using set methods
    connection(){
    }


    protected connection(Parcel in) {
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


    @Override
    public int compareTo(connection o) {
        try {
            if (o != null) {
                switch (this.SORT_BY) {
                    case SORT_BY_DIRECTION:
                        return (Integer.compare(this.convertStringToInt(this.direction),this.convertStringToInt(o.direction)));
                    case SORT_BY_NAME:
                        System.out.println("Sorting by name");
                        return (Integer.compare(this.convertStringToInt(this.name),this.convertStringToInt(o.name)));
                    case SORT_BY_TYPE:
                        return (Integer.compare(this.convertStringToInt(this.type),this.convertStringToInt(o.type)));
                    case SORT_BY_UNITS:
                        return (Integer.compare(this.convertStringToInt(this.units),this.convertStringToInt(o.units)));
                    default:
                        return (Integer.compare(this.convertStringToInt(this.s4),this.convertStringToInt(o.s4)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return(this.getS4().compareTo(o.getS4()));
    }

    public int convertStringToInt(String str1){
       str1 = str1.replaceAll("\\D+","");
        return(Integer.parseInt(str1));

    }
    protected String inout(){
        try {
            String temp = this.getDirection().toLowerCase();
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

    public static int getSortBy() {
        return SORT_BY;
    }

    public static void setSortBy(int sortBy) {
        SORT_BY = sortBy;
    }

    public String toString(){
        return (this.id+" " + this.direction + " " + this.name + " " + this.units + " " + this.units + " " + this.type);
    }

    //set methods
    public void setId(String id) {
        this.id = id;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlug(String plug) {
        this.plug = plug;
    }

    public void setS4(String s4) {
        this.s4 = s4;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUnits(String units) {
        this.units = units;
    }


    //get methods
    public String getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public String getPlug() {
        return plug;
    }

    public String getS4() {
        return s4;
    }

    public String getType() {
        return type;
    }

    public String getUnits() {
        return this.units;
    }


    public connection getConnectionObject(){return this;}
}
