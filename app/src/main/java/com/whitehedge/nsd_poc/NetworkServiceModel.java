package com.whitehedge.nsd_poc;

import java.net.InetAddress;

/**
 * Created by Mahesh Chakkarwar on 19-07-2016.
 */
public class NetworkServiceModel {
    private String serviceName;
    private int servicePort;
    private InetAddress serviceAddress;

    public NetworkServiceModel() {
        this.serviceName = "";
        this.servicePort = 0;
        this.serviceAddress =null;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public InetAddress getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(InetAddress serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    @Override
    public String toString() {
        return "NetworkServiceModel{" +
                "serviceName='" + serviceName + '\'' +
                ", servicePort=" + servicePort +
                ", serviceAddress=" + serviceAddress +
                '}';
    }
}
