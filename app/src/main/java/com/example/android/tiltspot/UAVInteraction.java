package com.example.android.tiltspot;

import android.widget.TextView;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;


import java.io.IOException;

public class UAVInteraction {

    //TO DO: implement this. Found a library, link below
    //...challenge is to edit the gradle build file correctly - DONE
    //https://github.com/EsotericSoftware/kryonet

    private Client client;
    private Server server;
    private TextView mTextState;

    public void land() {
    }

    public void UAVInteraction() throws IOException {


        //mTextState = (TextView) findViewById(R.id.label_sensor); this would be nice for testing, it isnt this easy though


        //setup connection with a drone, if failed, should throw exception
        client  = new Client();
        client.start();
        client.connect(5000, "192.168.10.1", 0,8889); //not sure if this assumes tcp or udp, doc at github doesnt say
        server.start();
        server.bind( 0, 8890);

        server.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                if (object instanceof String) {
                    String request = (String)object;

                }
            }
        });
    }



    //methods yet to be implemented, basically just send UDP text commands from the sdk
    public void forward() {

    }

    public void backward() {

    }

    public void right() {

    }

    public void left() {

    }

    public void hover() { //lift off

        String request = "takeoff";
        client.sendUDP(request);

    }
}
