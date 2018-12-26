package com.example.android.main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

public class UDP_Client {
    private static String serverString = "192.168.10.1";
    private static int port = 8889;
    public static String Message = "command";

    @SuppressLint("NewApi")
    public void send()
    {
        AsyncTask<Void, Void, Void> async_client = new MyTask();

        if (Build.VERSION.SDK_INT >= 11) async_client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_client.execute();
    }

    private static class MyTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... params) {
            DatagramSocket socket = null;

            try {
                InetAddress host = InetAddress.getByName(serverString);
                socket = new DatagramSocket(port);
                byte[] data = Message.getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
                //socket.setBroadcast(true);
                socket.send(packet);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (socket != null) {
                    socket.close();
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    };
}
