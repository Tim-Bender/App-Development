package com.example.main;

public class connection {
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


}
