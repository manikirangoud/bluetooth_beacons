package com.pam.tools.bluetoothbeacons;

public class DeviceDetails {

    private String name;
    private String address;
    private int rssi;
    private String timeStamp;

    public DeviceDetails(String name, String address, int rssi, String timeStamp) {

        this.name = name;
        this.address = address;
        this.rssi = rssi;
        this.timeStamp = timeStamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }


}
