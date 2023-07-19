package com.example.myapplicationandroidseeingeyeapplication;
import java.util.*;
import java.lang.*;

// times are stored in Integers that represent times in milliseconds
// time restarts every 10,000 milliseconds or every ten seconds
// The constructer takes ints or Integers and performs modular division by 10,000

public class EventTime {
    private Integer time_val;
    private static int life_time;
    private static int TIME_INTERVAL = 10000;
    private static int MICRO_PER_MILLI = 1000;

    // methods for using with int and Integer values that are already in terms of milliseconds
    public EventTime (int t) {
        this.time_val = new Integer (t % this.TIME_INTERVAL);
    }

    public EventTime (Integer t) {
        this (t.intValue ());
    }

    // methods for using with Long and long values obtained as the times of sensor readings that are in microseconds
    public EventTime (long t) {
        long temp = (t / this.MICRO_PER_MILLI);
        long temp2 = (temp % this.TIME_INTERVAL);
        this.time_val = new Integer ((int)temp2);
    }

    public EventTime (Long t) {
        this (t.longValue ());
    }

    public int getTime () {
        return this.time_val.intValue ();
    }

    public Integer getTimeObject () { return this.time_val; }

    // method to see if an image is still valid based on its event time
    // The time passed in should be the current time to be compared to the event time
    // If the current time is less than the event time then different logic must be used to see if the 
    // image is expired or not
    public boolean getValid (int time) {
        if (time < this.getTime ()) {
            int time_passed = (time + (this.TIME_INTERVAL - this.getTime ()));
            if (time_passed > this.life_time) {
                return false;
            } else {
                return true;
            }
        } else {
            int time_passed = (time - this.getTime ());
            if (time_passed > this.life_time) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean getValid (Integer time) {
        return this.getValid (time.intValue ());
    }

    public boolean getValid (long time) {
        long temp = (time / this.MICRO_PER_MILLI);
        int temp2 = (int)(temp % this.TIME_INTERVAL);
        return this.getValid (temp2);
    }

    public boolean getValid (Long time) {
        return this.getValid (time.longValue ());
    }

    public void setTime (int time) {
        this.time_val = new Integer (time);
    }

    public void setTime (Integer time) {
        this.setTime (time.intValue ());
    }

    public void setTime (long time) {
        long temp = (time / this.MICRO_PER_MILLI);
        int temp2 = (int)(temp % this.TIME_INTERVAL);
        this.setTime (temp2);
    }

    public void setTime (Long time) {
        this.setTime (time.longValue ());
    }

    // method for setting how long an image will be valid to be processed for in milliseconds
    public static void setLifeTime (int time) {
        life_time = time;
    }

    public static void setLifeTime (Integer time) {
        setLifeTime (time.intValue ());
    }

    public static void setLifeTime (long time) {
        long temp = (time / MICRO_PER_MILLI);
        int temp2 = (int)(temp % TIME_INTERVAL);
        setLifeTime (temp2);
    }

    public static void setLifeTime (Long time) {
        setLifeTime (time.longValue ());
    }
}