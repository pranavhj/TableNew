package com.example.tablenew;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPClient {

    private static final String TAG = "TableNew";

    private String serverIp = "192.168.1.8"; // Replace with your server's IP address
    private int serverPort = 1234; // Replace with your desired port

    private Socket socket = null;
    private String errorDuringSocketInit = null;
    String response = null;
    public TCPClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    public String StartSocket(String message) throws Exception
    {
        try
        {
            Log.d(TAG, "TCP → " + serverIp + ":" + serverPort + " cmd=" + message);
            socket = new Socket(serverIp, serverPort);
            errorDuringSocketInit = null;
            response = null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println(message);
            response = reader.readLine();
            socket.close();
            Log.d(TAG, "TCP ← resp=" + response);

        } catch (IOException e)
        {
            Log.e(TAG, "TCP error: " + e.getMessage(), e);
            errorDuringSocketInit = e.getMessage() + " " + e.getCause().getLocalizedMessage();
            throw e;
        }
        return response;
    }

    public synchronized String sendMessage(String message)
    {
        try
        {
            socket = null;

            Thread t1 = new Thread(() ->
            {
                try {
                    StartSocket(message);
                } catch (Exception e)
                {
                    e.printStackTrace();
                    errorDuringSocketInit = e.getMessage() + " " + e.getCause().getLocalizedMessage();
//                    return e.getMessage();
                }
            });
            t1.start();
            t1.join();

            if (errorDuringSocketInit != null  ||   response == null) {
                throw new RuntimeException(errorDuringSocketInit);
            }


        }
        catch (Exception e)
        {
            return e.getMessage();
        }

        return response;
    }


    public void sendMessage2(final String msg) {

        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run() {

                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
                    Socket s = new Socket("10.0.0.254", 9147);

                    OutputStream out = s.getOutputStream();

                    PrintWriter output = new PrintWriter(out);

                    output.println(msg);
                    output.flush();
                    BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    final String st = input.readLine();


                    output.close();
                    out.close();
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }
}
