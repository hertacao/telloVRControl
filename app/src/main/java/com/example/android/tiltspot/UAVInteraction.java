package com.example.android.tiltspot;

import android.widget.TextView;
import com.jfastnet.Client;
import com.jfastnet.Config;
import com.jfastnet.Server;
import com.jfastnet.messages.GenericMessage;

import java.util.concurrent.atomic.AtomicInteger;

import java.io.IOException;

public class UAVInteraction {

    //TO DO: implement this. Found a library, link below
    //...challenge is to edit the gradle build file correctly - DONE
    //https://github.com/EsotericSoftware/kryonet - OLD ONE
    //now using https://github.com/klaus7/jfastnet/

    private Client client;
    private Server server;
    private TextView mTextState;

    private static final AtomicInteger received = new AtomicInteger(0);



    public static class PrintMessage extends GenericMessage {

        /** no-arg constructor required for serialization. */
        private PrintMessage() {}

        PrintMessage(Object object) { super(object); }

        @Override
        public void process(Object context) {
            System.out.println(object);
            received.incrementAndGet();
        }
    }


    public void UAVInteraction() throws IOException, InterruptedException {


        //mTextState = (TextView) findViewById(R.id.label_sensor); this would be nice for testing, it isnt this easy though


        //setup connection with a drone, if failed, should throw exception
//        client  = new Client(new Config().setHost("192.168.10.1")); //not sure if this is the right way
//        client.getConfig().setPort(8889);
//
//        client.start();
//
//        client.blockingWaitUntilConnected();
//
//        client.send(new PrintMessage("command"));






//        server.start();
//        server.bind( 0, 8890);
//
//        server.addListener(new Listener() {
//            public void received (Connection connection, Object object) {
//                if (object instanceof String) {
//                    String request = (String)object;
//
//                }
//            }
//        });
    }

    public void connect() {
        Server server = new Server(new Config().setBindPort(8880));
        Client client = new Client(new Config().setPort(8889));

        server.start();
        client.start();
        client.blockingWaitUntilConnected();

        //server.send(new PrintMessage("Hello Client!"));
        client.send(new PrintMessage("command"));

        //while (received.get() < 2) Thread.sleep(100);

        client.stop();
        server.stop();
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

//        String request = "takeoff";
//        client.sendUDP(request);

    }

    public void land() {
    }
}
