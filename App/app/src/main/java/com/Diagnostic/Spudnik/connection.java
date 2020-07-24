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

package com.Diagnostic.Spudnik;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Hold data about individual connections.
 * <p>
 * Comparable is also implemented here to allow for easy sorting via the vehicle sort method.
 *
 * @author timothy.bender
 * @version dev 1.0.0
 * @see vehicle
 * @see android.os.Parcelable
 * @see java.lang.Comparable
 * @since dev 1.0.0
 */

public class connection implements Parcelable, Comparable<connection> {

    /**
     * Vehicle id
     */
    private String id;
    /**
     * Connector direction. Such as "In1" or "Out4"
     */
    private String direction;
    /**
     * Connector name
     */
    private String name;
    /**
     * Connector units
     */
    private String units;
    /**
     * Connector plug
     */
    private String plug;
    /**
     * Connector s4 pin number
     */
    private String s4;
    /**
     * Connector type
     */
    private String type;

    /**
     * Constructor
     *
     * @param id   Vehicle id
     * @param dir  Connector direction
     * @param s    s4
     * @param nm   name
     * @param un   units
     * @param type type
     */
    connection(String id, String dir, String s, String nm, String un, String type) {
        this.id = id;
        this.direction = dir;
        this.name = nm;
        this.units = un;
        this.s4 = s;
        this.type = type;
    }

    /**
     * Parcelable implementation
     *
     * @param in Parcel
     * @since dev 1.0.0
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
     * Compare the s4's to one another. Allows implementation of Collections.sort on our arraylist of connection objects
     *
     * @param o Connection
     * @return int
     * @see vehicle
     * @since dev 1.0.0
     */
    @Override
    public int compareTo(@NonNull connection o) {
        return Integer.compare(Integer.parseInt(s4), Integer.parseInt(o.s4));
    }

    /**
     * Return whether or not a connection is an input or output.
     *
     * @return String
     * @since dev 1.0.0
     */
    String inout() {
        String temp = direction, toReturn;
        toReturn = (temp.contains("in") || temp.contains("In")) ? "Input" : "Output";
        return toReturn;
    }

    /**
     * Set methods
     */
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

    public String toString() {
        return (id + " " + direction + " " + name + " " + units + " " + units + " " + type);
    }

}
