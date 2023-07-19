package com.example.myapplicationandroidseeingeyeapplication;
import java.util.*;
import java.lang.*;

// contains the image event times as well as the file_names of where the images are being saved

public class ImageValidationList {
    private List <EventTime> images;
    // images that were going to be over-written, but had already been sent to the sever for processing and were still
    // valid at the time that they were going to be over-written
    private List <EventTime> waiting_images;
    private List <String> file_names;
    // list of booleans that track which images have been sent for processing and the result has not yet arrived
    private List <Boolean> sent;
    // index in list of image files that is next in line to be over-written
    private int current_write = 0;
    // index in list of image files that is next in line to be sent for processing
    private int current_send = 0;
    private int list_size;
    // Priority queue for recieved responses to be processed
    PriorityQueue <String> responses;

    public ImageValidationList (int size) {
        this.images = Arrays.asList (new EventTime [size]);
        this.waiting_images = Arrays.asList (new EventTime [size]);
        this.file_names = Arrays.asList (new String [size]);
        this.sent = Arrays.asList (new Boolean [size]);
        this.list_size = size;
        this.responses = new PriorityQueue <String> ();
        String s = new String ("image");
        for (int i = 0; i < this.list_size; i++) {
            s += i;
            s += ".png";
            this.file_names.set(i, s);
            s = new String("image");
            this.images.set(i, new EventTime(0));
            this.waiting_images.set(i, null);
            this.sent.set(i, false);
        }
    }

    public int getImageTimeByID (int id) {
        return this.images.get (id).getTime ();
    }

    public boolean getImageValidByID (int id, int time) {
        return this.images.get (id).getValid (time);
    }

    public boolean getImageValidByID (int id, Integer time) {
        return this.images.get (id).getValid (time);
    }

    public boolean getImageValidByID (int id, Long time) {
        return this.images.get (id).getValid (time);
    }

    public boolean getImageValidByID (int id, long time) {
        return this.images.get (id).getValid (time);
    }

    public void setImageTimeByID (int id, int t) {
        this.images.get (id).setTime (t);
    }

    public void setImageTimeByID (int id, Integer t) {
        this.images.get (id).setTime (t);
    }

    public void setImageTimeByID (int id, Long t) {
        this.images.get (id).setTime (t);
    }

    public void setImageTimeByID (int id, long t) {
        this.images.get (id).setTime (t);
    }

    public int getWaitingImageTimeByID (int id) {
        return this.waiting_images.get (id).getTime ();
    }

    public Integer getWaitingImageTimeObjectByID (int id) {
        return this.waiting_images.get (id).getTimeObject ();
    }

    public boolean getWaitingImageValidByID (int id, int time) {
        return this.waiting_images.get (id).getValid (time);
    }

    public boolean getWaitingImageValidByID (int id, Integer time) {
        return this.waiting_images.get (id).getValid (time);
    }

    public boolean getWaitingImageValidByID (int id, Long time) {
        return this.waiting_images.get (id).getValid (time);
    }

    public boolean getWaitingImageValidByID (int id, long time) {
        return this.waiting_images.get (id).getValid (time);
    }

    public void setWaitingImageTimeByID (int id, int t) {
        this.waiting_images.get (id).setTime (t);
    }

    public void setWaitingImageTimeByID (int id, Integer t) {
        this.waiting_images.get (id).setTime (t);
    }

    public void setWaitingImageTimeByID (int id, Long t) {
        this.waiting_images.get (id).setTime (t);
    }

    public void setWaitingImageTimeByID (int id, long t) {
        this.waiting_images.get (id).setTime (t);
    }

    public String getImageFileNameByID (int id) {
        return this.file_names.get (id);
    }

    public void setImageFileNameByID (int id, String file_name) {
        this.file_names.set (id, file_name);
    }

    // The sent flags tell whether a given image has been sent yet or not
    // This can be used to help determine whether to throw away the result of an object recognition
    // Or whether to overwrite a certain image with another image
    public boolean getIfImageSentByID (int id) {
        return this.sent.get (id).booleanValue ();
    }

    public void setIfImageSentByID (int id, boolean has_been_sent) {
        this.sent.set (id, has_been_sent);
    }

    public void setIfImageSentByID (int id, Boolean has_been_sent) {
        this.setIfImageSentByID (id, has_been_sent.booleanValue ());
    }

    // Method that checks to see if an image is valid, and if it is, it will access the image file
    // put the image file into a string format with the image id appended at the front
    // and then encrypt the string and send it over the network to the server side with TCP
    // will use the current send variable to choose which image to send next and will increment it
    public void sendEncryptedImageWithTCP (long t) {
        if (this.current_send == this.list_size) {
            this.current_send = 0;
        }
        // if true create the image string with the ID appended at the front
        // Then encrypt the string and send with TCP
        if (this.getImageValidByID (this.current_send, t)) {
            this.setIfImageSentByID (this.current_send, true);
        } else {
            this.setIfImageSentByID (this.current_send, false);
        }
    }

    // uses png images for now .png
    // Will return the file name generated for the image
    public String addImageEventForID (int t) {
        if (this.current_write == this.list_size) {
            this.current_write = 0;
        }
        if (this.getImageValidByID (this.current_write, t) &&
                this.getIfImageSentByID (this.current_write) &&
                this.getWaitingImageTimeObjectByID (this.current_write) == null) {
            this.setWaitingImageTimeByID (this.current_write, this.getImageTimeByID (this.current_write));
            this.setIfImageSentByID (this.current_write, false);
        } else if (this.getImageValidByID (this.current_write, t) &&
                this.getIfImageSentByID (this.current_write) &&
                this.getWaitingImageTimeObjectByID (this.current_write) != null) {
            this.setWaitingImageTimeByID (this.current_write, 0);
            this.setIfImageSentByID (this.current_write, false);
        }
        this.setImageTimeByID (this.current_write, t);
        this.setIfImageSentByID (this.current_write, false);
        this.current_write += 1;
        return this.getImageFileNameByID (this.current_write - 1);
    }

    // method to add responses to a priority queue if they are still valid
    public void addResponse (String response, long time) {
        // id will be in the first part of the response string
        int id = 0;
        if ((! this.getIfImageSentByID (id)) && this.getWaitingImageTimeObjectByID (id) != null) {
            if (this.getWaitingImageValidByID (id, time)) {
                Integer null_int = null;
                this.setWaitingImageTimeByID (id, null_int);
                this.responses.add (response);
            }
        } else if (this.getIfImageSentByID (id) && this.getWaitingImageTimeObjectByID (id) == null) {
            if (this.getImageValidByID (id, time)) {
                this.setIfImageSentByID (id, false);
                this.responses.add (response);
            }
        } else {
            return;
        }
    }

    // method to pull responses from the priority queue to have processed 
    public String processResponse () {
        return this.responses.remove ();
    }
}