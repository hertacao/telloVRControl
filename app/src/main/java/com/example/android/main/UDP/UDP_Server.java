package com.example.android.main.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.example.android.main.MainActivity;

public class UDP_Server {
    private int port;
    private String INTENT_ACTION = Intent.ACTION_RUN;
    private String INFO_TYPE = "";
    private MyTask async_server;

    public UDP_Server(int port) {
        this.port = port;
    }

    @SuppressLint("NewApi")
    public void run() {
        async_server = new MyTask(INTENT_ACTION, INFO_TYPE, port);

        if (Build.VERSION.SDK_INT >= 11) async_server.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_server.execute();
    }

    public void stopUDPServer() {
        if(async_server != null) {
            async_server.isActive = false;
        }
    }

    public void setINFO_TYPE(String infoType) {
        this.INFO_TYPE = infoType;
    }

    public void setINTENT_ACTION(String intentAction) {
        this.INTENT_ACTION = intentAction;
    }

    private static class MyTask extends AsyncTask<Void, Void, String> {
        private String INTENT_ACTION;
        private String INFO_TYPE;
        private int port;
        boolean isActive = true;

        private MyTask(String INTENT_ACTION, String INFO_TYPE, int port) {
            super();
            this.INTENT_ACTION = INTENT_ACTION;
            this.INFO_TYPE = INFO_TYPE;
            this.port = port;
        }

        @Override
        protected String doInBackground(Void... params) {
            DatagramSocket socket = null;

            try {
                socket = new DatagramSocket(port);

                byte[] buffer = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while(isActive)
                {
                    socket.receive(packet);

                    String received = new String(buffer, 0, packet.getLength());

                    Log.i("UDP Server: package", received);
                    Intent intent = new Intent(INTENT_ACTION);
                    intent.putExtra(INFO_TYPE, new String(buffer, 0, packet.getLength()));
                    MainActivity.getAppContext().sendBroadcast(intent);
                }

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
