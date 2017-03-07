package com.whitehedge.nsd_poc;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private NSDHelper nsdHelper;
    private Button idBtnStartService;
    private TextView idTvMsgStack;
    private EditText idEtMsg;
    private Button idBtnSend;
    private Service service;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().toString();
            addChatLine(message);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idBtnStartService = (Button) findViewById(R.id.id_btn_start_service);
        idTvMsgStack = (TextView) findViewById(R.id.id_tv_msg_stack);
        idEtMsg = (EditText) findViewById(R.id.id_et_msg);
        idBtnSend = (Button) findViewById(R.id.id_btn_send);
        nsdHelper = new NSDHelper(MainActivity.this,handler);
        nsdHelper.initializeNSD();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nsdHelper != null)
            nsdHelper.discoverServices();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (nsdHelper != null) {
            nsdHelper.stopDiscovery();
        }
        if (service != null) {
            nsdHelper.unregisterService();
            service.stopService();
        }
    }

    public void startService(View view) {
        if (service == null) {
            service = new Service(MainActivity.this,handler, nsdHelper);
        } else {
            Toast.makeText(MainActivity.this, "Service already Started", Toast.LENGTH_SHORT).show();
        }
    }

    public void connectService(View view) {
        nsdHelper.connectService(handler);
    }

    public void addChatLine(String line) {
        idTvMsgStack.append("\n" + line);
    }

    public void sendMessage(View view) {
        if (!idEtMsg.getText().toString().isEmpty())
            service.sendMessage(idEtMsg.getText().toString());
    }
}
