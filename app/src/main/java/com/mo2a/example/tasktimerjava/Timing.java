package com.mo2a.example.tasktimerjava;

import java.io.Serializable;
import java.util.Date;

public class Timing implements Serializable {
    private static final long serialVersionUID= 20190420L;
    private static final String TAG = "Timing";

    private long _id;
    private Task task;
    private long startTime;
    private long duration;

    public Timing(Task task) {
        this.task = task;
        Date currentTime= new Date();
        startTime= currentTime.getTime()/1000;
        duration= 0;
    }

     long get_id() {
        return _id;
    }

     void set_id(long _id) {
        this._id = _id;
    }

     Task getTask() {
        return task;
    }

     void setTask(Task task) {
        this.task = task;
    }

     long getStartTime() {
        return startTime;
    }

     void setStartTime(long startTime) {
        this.startTime = startTime;
    }

     long getDuration() {
        return duration;
    }

     void setDuration() {
        Date currentTime= new Date();
        duration = (currentTime.getTime()/1000) - startTime;
    }
}
