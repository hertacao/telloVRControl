package com.example.android.main.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.example.android.main.MainActivity;

public class UDP_Client {
    private String serverString;
    private int port;
    private String INTENT_ACTION = Intent.ACTION_RUN;
    public String message = "command";

    public UDP_Client(String serverString, int port) {
        this.serverString = serverString;
        this.port = port;
    }

    public void setINTENT_ACTION(String intentAction) {
        this.INTENT_ACTION = intentAction;
    }

    @SuppressLint("NewApi")
    public void send() {
        AsyncTask<Void, Void, String> async_client = new MyTask(INTENT_ACTION, serverString, port, message);

        if (Build.VERSION.SDK_INT >= 11) async_client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_client.execute();
    }

    public static class MyTask extends AsyncTask<Void, Void, String> {
        private String INTENT_ACTION;
        private String serverString;
        private int port;
        private String message;

        private MyTask(String INTENT_ACTION, String serverString, int port, String message) {
            super();
            this.INTENT_ACTION = INTENT_ACTION;
            this.serverString = serverString;
            this.port = port;
            this.message = message;
        }

        @Override
        protected String doInBackground(Void... params) {
            DatagramSocket socket = null;

            try {
                InetAddress host = InetAddress.getByName(serverString);
                socket = new DatagramSocket(port);

                byte[] data = message.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(data, data.length, host, port);
                socket.send(sendPacket);

                socket.setSoTimeout(3000);

                byte[] buffer = new byte[1024];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);

                while(true) {        // received data until timeout
                    try {
                        socket.receive(receivedPacket);
                        return new String(buffer, 0, receivedPacket.getLength());
                    }
                    catch (SocketTimeoutException e) {
                        // timeout exception.
                        Log.e("UDP_client:", "Timeout reached!!! " + e);
                        socket.close();
                        return "error";
                    }
                }

            } catch (Exception e) {
                return "error";
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }

        protected void onPostExecute(String reply) {
            Intent intent = new Intent(INTENT_ACTION);
            intent.putExtra("message", message);
            intent.putExtra("reply", reply);
            MainActivity.getAppContext().sendBroadcast(intent);
        }
    }
}
