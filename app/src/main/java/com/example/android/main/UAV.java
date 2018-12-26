package com.example.android.main;

public class UAV {
    UDP_Client client;

    public UAV() {
    }

    public boolean connect() {
        client = new UDP_Client();
        client.Message = "command";
        client.send();
        return true;
    }

    //methods yet to be implemented, basically just send UDP text commands from the sdk
    public void land() {
        client.Message = "land";
        client.send();
    }

    public void takeoff() {
        client.Message = "takeoff";
        client.send();
    }

    public void forward(int x) {
        client.Message = "forward " + x;
        client.send();
    }

    public void backward(int x) {
        client.Message = "backward " + x;
        client.send();
    }

    public void right(int x) {
        client.Message = "right " + x;
        client.send();
    }

    public void left(int x) {
        client.Message = "left " + x;
        client.send();
    }

    public void rotRight(int x) {
        client.Message = "cw " + x;
        client.send();
    }

    public void rotLeft(int x) {
        client.Message = "ccw " + x;
        client.send();
    }

    public void hover() { //lift off
        client.Message = "stop";
        client.send();
    }
}
