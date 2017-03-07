package com.whitehedge.nsd_poc;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Mahesh Chakkarwar on 19-07-2016.
 */
public class Service {
    String TAG = "Service";
    ServerSocket serverSocket = null;
    Thread thread = null;
    private NSDHelper nsdHelper;
    private Handler updateHandler;
    private ChatClient chatClient;
    private Context context;

    public Service(Context context, Handler handler, NSDHelper nsdHelper) {
        this.context = context;
        this.updateHandler = handler;
        this.nsdHelper = nsdHelper;
        thread = new Thread(new ServerThread());
        thread.start();
    }

    public void stopService() {
        thread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException ioe) {
            LoggingUtil.showError(TAG, "Error when closing server socket.");
        }
    }

    private class ServerThread implements Runnable {

        @Override
        public void run() {

            try {
                // Since discovery will happen via Nsd, we don't need to care which port is
                // used.  Just grab an available one  and advertise it via Nsd.
                serverSocket = new ServerSocket(0);
                nsdHelper.registerService(serverSocket.getLocalPort(), nsdHelper.SERVICE_NAME, nsdHelper.SERVICE_TYPE);
                LoggingUtil.showVerbose(TAG, "Service started on Port" + serverSocket.getLocalPort());
                while (!Thread.currentThread().isInterrupted()) {
                    LoggingUtil.showVerbose(TAG, "ServerSocket Created, awaiting connection");
                    Socket socket = serverSocket.accept();
                    chatClient = new ChatClient(socket, updateHandler);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error creating ServerSocket: ", e);
                e.printStackTrace();
            }
        }
    }

    public void updateMessage(String msg, Boolean flag) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);
        Message message = new Message();
        message.setData(messageBundle);
        updateHandler.sendMessage(message);
    }

    public void sendMessage(String msg) {
        if (chatClient != null) {
            chatClient.sendMessage(msg);
        } else {
            LoggingUtil.showError(TAG, "Connection not established!!!!, unable to send msg");
        }
    }
}
