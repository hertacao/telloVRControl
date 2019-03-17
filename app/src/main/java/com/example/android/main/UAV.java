package com.example.android.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.main.UDP.UDP_Client;
import com.example.android.main.UDP.UDP_Server;

public class UAV extends BroadcastReceiver {
    private static final String COMMAND_SERVER = "192.168.10.1";
    private static final int COMMAND_PORT = 8889;
    private static final int STATE_PORT = 8890;
    private static final int VIDEO_PORT = 11111;

    private static final String STATE = "com.example.android.main.STATE";
    private static final String COMMAND = "com.example.android.main.COMMAND";
    private static final String VIDEO = "com.example.android.main.video";

    private UDP_Client client;
    private UDP_Server server;

    private int yaw;
    private int vgx;
    private int vgy;
    private int vgz;
    private int h;

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    private boolean busy = false;

    public UAV() {

    }

    void connect() {
        client = new UDP_Client(COMMAND_SERVER, COMMAND_PORT);
        client.setINTENT_ACTION(COMMAND);
        client.message = "command";
        client.send();

        /*server = new UDP_Server(STATE_PORT);
        server.setINTENT_ACTION(STATE);
        server.setINFO_TYPE("state");
        server.run();*/
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i("UAV:", action);

        if (action.equals(COMMAND)) {
            String message = intent.getStringExtra("message");
            String reply = intent.getStringExtra("reply");
            Log.i("UAV:", "in command");
            if (message.equals("connect")) {
                Log.i("UAV:", "connecting");
                if (reply.equals("ok")) {
                    MainActivity.connectSuccess();
                    MainActivity.getmTextOther().setText(reply);
                    Log.i("UAV:", "connection successful");
                } else if (reply.equals("error")) {
                    MainActivity.connectError();
                    Log.i("UAV:", "connection failed");
                } else {
                    //activity.error();
                }
            } else {
                if (reply.equals("ok")) {
                    busy = false;
                } else if (reply.equals("error")) {
                    client.message = message;
                    client.send();
                } else {
                    //activity.error();
                }
            }
        } else if (action.equals(STATE) || action.equals(Intent.ACTION_RUN)) {
            String received = intent.getStringExtra("state");
            Log.i("UAV: Action.STATE", received);
            parse(received);
            MainActivity.getmTextOther().setText(String.valueOf(this.getYaw()));
        }
    }

    public boolean move(MoveState moveState, float displacement) {
        switch (moveState) {
            case GROUND: {}
            case FORWARD:
                this.forward((int) displacement);
            case BACK:
                this.back((int) displacement);
            case ROTATERIGHT:
                this.rotRight((int) displacement);
            case ROTATELEFT:
                this.rotLeft((int) displacement);
            case LEFT:
                this.left((int) displacement);
            case RIGHT:
                this.right((int) displacement);
            default: {} //drone.hover();
        }
        return true;
    }

    //methods yet to be implemented, basically just send com.example.android.main.UDP text commands from the sdk
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

    void right(int x) {
        client.message = "right " + x;
        client.send();
    }

    void left(int x) {
        client.message = "left " + x;
        client.send();
    }

    void rotRight(int x) {
        client.message = "cw " + x;
        client.send();
    }

    void rotLeft(int x) {
        client.message = "ccw " + x;
        client.send();
    }

    void hover() { //lift off
        client.message = "stop";
        client.send();
    }

    int getYaw() {
        return yaw;
    }

    private void parse(String string) {
        String[] output = string.split(";");
        for (String token : output) {
            String[] pair = token.split(":");
            switch(pair[0]) {
                case "yaw": this.yaw =Integer.parseInt(pair[1]);
                case "vgx": this.vgx =Integer.parseInt(pair[1]);
                case "vgy": this.vgy =Integer.parseInt(pair[1]);
                case "vgz": this.vgz =Integer.parseInt(pair[1]);
                case "h": this.h =Integer.parseInt(pair[1]);
            }
        }
    }
}
