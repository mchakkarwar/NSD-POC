package com.whitehedge.nsd_poc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Mahesh Chakkarwar on 20-07-2016.
 */
public class ChatClient {
    private String TAG=getClass().getName();
    private Socket clientSocket;
    Handler updateHandler;
    public ChatClient(Socket socket, Handler handler) {
        this.clientSocket = socket;
        this.updateHandler=handler;
        Thread receivingThread = new Thread(new ReceivingThread(clientSocket,updateHandler));
        receivingThread.start();
    }

    public void sendMessage(String msg) {
        try {
            if (clientSocket == null) {
                LoggingUtil.showError(TAG, "Socket is null, wtf?");
            } else if (clientSocket.getOutputStream() == null) {
                LoggingUtil.showError(TAG, "Socket output stream is null, wtf?");
            }

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(clientSocket.getOutputStream())), true);
            out.println(msg);
            out.flush();
            updateMessage(msg, true);
        } catch (UnknownHostException e) {
            LoggingUtil.showError(TAG, "Unknown Host:-" + e);
        } catch (IOException e) {
            LoggingUtil.showError(TAG, "I/O Exception" + e);
        } catch (Exception e) {
            LoggingUtil.showError(TAG, "Error3" + e);
        }
        LoggingUtil.showVerbose(TAG, "Client sent message: " + msg);
    }
    public void updateMessage(String msg, Boolean flag) {
        Bundle messageBundle = new Bundle();
        messageBundle.putString("msg", msg);
        Message message = new Message();
        message.setData(messageBundle);
        updateHandler.sendMessage(message);
    }

}
