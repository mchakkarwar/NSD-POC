package com.whitehedge.nsd_poc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Mahesh Chakkarwar on 20-07-2016.
 */
public class ReceivingThread implements Runnable {
    String TAG = getClass().getName();
    private Socket socket;
    private Handler updateHandler;

    public ReceivingThread(Socket socket, Handler handler) {
        this.socket = socket;
        this.updateHandler = handler;
    }

    @Override
    public void run() {
        BufferedReader input;
        try {
            input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            while (!Thread.currentThread().isInterrupted()) {

                String messageStr = null;
                messageStr = input.readLine();
                if (messageStr != null) {
                    Log.d(TAG, "Read from the stream: " + messageStr);
                    updateMessage(messageStr, true);
                } else {
                    Log.d(TAG, "The nulls! The nulls!");
                    break;
                }
            }
            input.close();

        } catch (IOException e) {
            Log.e(TAG, "Server loop error: ", e);
        }

    }
    public void updateMessage(String msg, Boolean flag) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);
        Message message = new Message();
        message.setData(messageBundle);
        updateHandler.sendMessage(message);
    }
}
