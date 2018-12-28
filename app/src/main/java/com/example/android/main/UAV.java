package com.example.android.main;

import UDP.UDP_Client;
import UDP.UDP_Server;

public class UAV implements AsyncResponse {
    private MainActivity activity;
    private static final String COMMAND_SERVER = "192.168.10.1";
    private static final String STATE_SERVER = "0.0.0.0";
    private static final int COMMAND_PORT = 8889;
    private static final int STATE_PORT = 8890;
    private static final int VIDEO_PORT = 11111;

    private UDP_Client client;
    private UDP_Server server;

    UAV(MainActivity activity) {
        this.activity = activity;
    }

    void connect() {
        client = new UDP_Client(this, COMMAND_SERVER, COMMAND_PORT);
        server = new UDP_Server(this, STATE_SERVER, STATE_PORT);
        server.setActionString("command response");
        client.message = "command";
        client.send();
    }

    //methods yet to be implemented, basically just send UDP text commands from the sdk
    void land() {
        client.message = "land";
        client.send();
    }

    void takeoff() {
        client.message = "takeoff";
        client.send();
    }

    void forward(int x) {
        client.message = "forward " + x;
        client.send();
    }

    void back(int x) {
        client.message = "back " + x;
        client.send();
    }

    public void right(int x) {
        client.message = "right " + x;
        client.send();
    }

    public void left(int x) {
        client.message = "left " + x;
        client.send();
    }

    public void rotRight(int x) {
        client.message = "cw " + x;
        client.send();
    }

    public void rotLeft(int x) {
        client.message = "ccw " + x;
        client.send();
    }

    public void hover() { //lift off
        client.message = "stop";
        client.send();
    }

    // TODO
    int getYaw() {
        //server....
        return 0;
    }

    @Override
    public void processFinish(String message, String output) {
        if(message.equals("command")) {
            if(output.equals("ok")) {
                activity.connectSuccess();
            } else if (output.equals("error")) {
                activity.connectError();
            }
        } else {
            //activity.error();
        }
    }
}
