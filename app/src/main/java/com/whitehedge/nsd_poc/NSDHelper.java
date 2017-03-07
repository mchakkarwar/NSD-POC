package com.whitehedge.nsd_poc;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Mahesh Chakkarwar on 19-07-2016.
 */
//Network Service Discovery class
public class NSDHelper {
    private String className = getClass().getName();
    private Context context;
    private NsdManager nsdManager;
    private NsdManager.RegistrationListener nsdRegistrationListener;
    private NsdManager.DiscoveryListener nsdDiscoveryListener;
    private NsdManager.ResolveListener nsdResolveListener;
    private NsdServiceInfo nsdServiceInfo;
    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String SERVICE_NAME = "NSD-POC";
    private String ourServiceName;
    public HashMap<String, NetworkServiceModel> servicesList = new HashMap<>();
    public NetworkServiceModel networkServiceModel;
    private Handler updateHandler;

    public NSDHelper(Context context, Handler handler) {
        this.context = context;
        this.updateHandler = handler;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    //initialize listeners like registration, discovery, resolver listener
    public void initializeNSD() {
        initializeRegistrationListener();
        initializeDiscoveryListener();
        nsdResolveListener = initializeResolveListener();
    }

    //initialize registration listener
    public void initializeRegistrationListener() {
        nsdRegistrationListener = new NsdManager.RegistrationListener() {
            @Override
            public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {

            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int i) {

            }

            @Override
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
                ourServiceName = nsdServiceInfo.getServiceName();
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
                ourServiceName = "";
            }
        };
    }

    //initialize discovery listenr
    public void initializeDiscoveryListener() {
        nsdDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onStartDiscoveryFailed(String s, int i) {
                LoggingUtil.showVerbose(className, "onStartDiscoveryFailed:-" + s);
            }

            @Override
            public void onStopDiscoveryFailed(String s, int i) {
                LoggingUtil.showVerbose(className, "onStopDiscoveryFailed:-" + s);
            }

            @Override
            public void onDiscoveryStarted(String s) {
                LoggingUtil.showVerbose(className, "onDiscoveryStarted:-" + s);
            }

            @Override
            public void onDiscoveryStopped(String s) {
                LoggingUtil.showVerbose(className, "onDiscoveryStopped:-" + s);
            }

            @Override
            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {

                if (ourServiceName != null && nsdServiceInfo.getServiceName().equals(ourServiceName)) {
                    LoggingUtil.showVerbose(className, "Same machine: " + ourServiceName);
                } else if (nsdServiceInfo.getServiceName().contains(SERVICE_NAME)) {
                    LoggingUtil.showVerbose(className, "onServiceFound:- NSD Service found" + nsdServiceInfo.getServiceName());
                    nsdManager.resolveService(nsdServiceInfo, nsdResolveListener);
                } else {
                    LoggingUtil.showVerbose(className, "onServiceFound:-" + nsdServiceInfo.getServiceName());
                }

            }

            @Override
            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                LoggingUtil.showVerbose(className, "onServiceLost:-" + nsdServiceInfo.getServiceName());
                if (nsdServiceInfo.getHost() != null && servicesList.containsKey(nsdServiceInfo.getHost().getHostAddress())) {
                    servicesList.remove(nsdServiceInfo.getHost().getHostAddress());
                }
                LoggingUtil.showVerbose(className, "Services Found:-" + servicesList.toString());
            }
        };
    }

    public NsdManager.ResolveListener initializeResolveListener() {
        return new NSDResolver();
    }

    //register discover services listener
    public void discoverServices() {
        LoggingUtil.showVerbose(className, "discoverServices" + "Discovering Service");
        if (nsdManager != null && nsdDiscoveryListener != null)
            nsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, nsdDiscoveryListener);
    }

    //stop discovery
    public void stopDiscovery() {
        LoggingUtil.showVerbose(className, "stopDiscovery" + "Discovery Stopped");
        if (nsdManager != null && nsdDiscoveryListener != null)
            nsdManager.stopServiceDiscovery(nsdDiscoveryListener);
    }

    //register service
    public void registerService(int port, String ourServiceName, String serviceType) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setPort(port);
        serviceInfo.setServiceName(ourServiceName);
        serviceInfo.setServiceType(serviceType);
        if (nsdRegistrationListener != null)
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, nsdRegistrationListener);

    }

    //unregister service
    public void unregisterService() {
        if (nsdRegistrationListener != null)
            nsdManager.unregisterService(nsdRegistrationListener);
    }

    //Connecting service
    public void connectService(final Handler updateHandler) {
        Set<String> keySet = servicesList.keySet();
        final Iterator iterator = keySet.iterator();
        while (iterator.hasNext()) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    NetworkServiceModel networkServiceModel = servicesList.get(iterator.next());
                    Socket socket = null;
                    try {
                        socket = new Socket(networkServiceModel.getServiceAddress(), networkServiceModel.getServicePort());
                        ChatClient chatClient = new ChatClient(socket, updateHandler);
                        chatClient.sendMessage("I am" + socket.getLocalAddress().getHostAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

            break;
        }
    }

    //Resolver class for resolving port and ip address of discovered service
    class NSDResolver implements NsdManager.ResolveListener {

        @Override
        public void onResolveFailed(NsdServiceInfo serviceInfo, int i) {
            LoggingUtil.showError(className, "onResolveFailed" + serviceInfo.getServiceName());
        }

        @Override
        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            nsdServiceInfo = serviceInfo;
            LoggingUtil.showVerbose(className, "onServiceResolved" + serviceInfo.getServiceName() + " " + serviceInfo.getHost() + " " + serviceInfo.getPort());
            NetworkServiceModel networkServiceModel = new NetworkServiceModel();
            networkServiceModel.setServiceName(serviceInfo.getServiceName());
            networkServiceModel.setServiceAddress(serviceInfo.getHost());
            networkServiceModel.setServicePort(serviceInfo.getPort());
            if (!servicesList.containsKey(networkServiceModel.getServiceAddress().getHostAddress())) {
                servicesList.put(networkServiceModel.getServiceAddress().getHostAddress(), networkServiceModel);
//                Socket socket = null;
//                try {
//                    socket = new Socket(networkServiceModel.getServiceAddress(), networkServiceModel.getServicePort());
//                    ChatClient chatClient = new ChatClient(socket, updateHandler);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                LoggingUtil.showVerbose(className, "onServiceResolved Services Found:-" + servicesList.toString());
            }
        }
    }
}
