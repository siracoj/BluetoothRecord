package com.audioserver;

import java.io.IOException;

import javax.bluetooth.LocalDevice;

public class ServerCommand {
public static void main(String[] args) throws IOException {
        
        //display local device address and name
        LocalDevice localDevice = LocalDevice.getLocalDevice();
        System.out.println("Address: "+localDevice.getBluetoothAddress());
        System.out.println("Name: "+localDevice.getFriendlyName());
        
        BTServer BTCommand=new BTServer();
        BTCommand.startServer();
        
    }

}
