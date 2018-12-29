package com.example.android.main.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.example.android.main.AsyncResponse;

public class UDP_Server {
    private AsyncResponse delegate;
    private String serverString;
    private int port;
    private String ACTION_STRING = "";
    private MyTask async_server;

    public UDP_Server(AsyncResponse delegate, String serverString, int port) {
        this.delegate = delegate;
        this.serverString = serverString;
        this.port = port;
    }

    @SuppressLint("NewApi")
    public void run() {
        async_server = new MyTask(delegate, serverString, port, ACTION_STRING);

        if (Build.VERSION.SDK_INT >= 11) async_server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_server.execute();
    }

    public void stopUDPServer() {
        if(async_server != null) {
            async_server.isActive = false;
        }
    }

    public void setActionString(String actionString) {
        ACTION_STRING = actionString;
    }

    private static class MyTask extends AsyncTask<Void, Void, String> {
        private AsyncResponse delegate;
        private String serverString;
        private int port;
        boolean isActive = true;
        private String ACTION_STRING;

        private MyTask(AsyncResponse delegate, String serverString, int port, String ACTION_STRING) {
            super();
            this.serverString = serverString;
            this.port = port;
            this.ACTION_STRING = ACTION_STRING;
        }

        @Override
        protected String doInBackground(Void... params) {
            DatagramSocket socket = null;

            try {
                InetAddress host = InetAddress.getByName(serverString);
                socket = new DatagramSocket(port, host);

                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                return new String(buffer, 0, packet.getLength());

                /*while(isActive)
                {
                    socket.receive(packet);

                    Intent i = new Intent();
                    i.setAction(ACTION_STRING);
                    i.putExtra(new String(buffer, 0, packet.getLength()), true);
                    MainContext.getApplicationContext().sendBroadcast(i);
                }*/

            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (socket != null)
                {
                    socket.close();
                }
            }
            return null;
        }
    }
}
