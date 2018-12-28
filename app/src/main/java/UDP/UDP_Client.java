package UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

import com.example.android.main.AsyncResponse;

public class UDP_Client {
    private AsyncResponse delegate;
    private String serverString;
    private int port;
    public String message = "command";


    public UDP_Client(AsyncResponse delegate, String serverString, int port) {
        this.delegate = delegate;
        this.serverString = serverString;
        this.port = port;
    }

    @SuppressLint("NewApi")
    public void send() {
        AsyncTask<Void, Void, String> async_client = new MyTask(delegate, serverString, port, message);

        if (Build.VERSION.SDK_INT >= 11) async_client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        else async_client.execute();
    }

    public static class MyTask extends AsyncTask<Void, Void, String> {
        private AsyncResponse delegate;
        private String serverString;
        private int port;
        private String message;

        private MyTask(AsyncResponse delegate, String serverString, int port, String message) {
            super();
            this.delegate = delegate;
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

                byte[] buffer = new byte[2048];
                DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
                return new String(buffer, 0, receivedPacket.getLength());

            } catch (Exception e) {
                return "error";
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
        }

        protected void onPostExecute(String result) {
            //if(result != null) {
                delegate.processFinish(message, result);
            //} else {
            //}
        }
    }
}
