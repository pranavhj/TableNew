package com.example.tablenew;

import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient {

    private static final String TAG = "TableNew";

    private String serverIp = "192.168.1.8";
    private int serverPort = 1234;

    public TCPClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
    }

    /**
     * Send a command and return the response. Blocking — must be called from a background thread.
     */
    public synchronized String sendMessage(String message) {
        try {
            Log.d(TAG, "TCP → " + serverIp + ":" + serverPort + " cmd=" + message);
            Socket sock = new Socket(serverIp, serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
            writer.println(message);
            String resp = reader.readLine();
            sock.close();
            Log.d(TAG, "TCP ← resp=" + resp);
            return resp != null ? resp : "";
        } catch (IOException e) {
            Log.e(TAG, "TCP error: " + e.getMessage(), e);
            return "ERROR: " + e.getMessage();
        }
    }
}
