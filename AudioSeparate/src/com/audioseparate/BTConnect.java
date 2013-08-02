package com.audioseparate;
 
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
 
 
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
 
public class BTConnect{
  
  private static final String TAG = "Bluetooth Command Connect";
  private BluetoothAdapter btAdapter = null;
  private BluetoothSocket btSocket = null;
  private OutputStream outStream = null;
  
  private String address = null;
  
  // Get SSP Service
  private static final UUID MY_UUID =
      UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
 
  public BTConnect(String address){
	  this.address = address;
  }
 
  public void Start() {
 
    Log.d(TAG,"...Attempting client connect...");
    btAdapter = BluetoothAdapter.getDefaultAdapter();
 
    // Set up a pointer to the remote node using it's address.
    BluetoothDevice device = btAdapter.getRemoteDevice(address);
 
    try {
      btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
    } catch (IOException e) {
      Log.e("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
    }
 
    // Establish the connection.  This will block until it connects.
    try {
      btSocket.connect();
      Log.d(TAG,"...Connection established and data link opened...");
    } catch (IOException e) {
      try {
        btSocket.close();
      } catch (IOException e2) {
    	  Log.e(TAG,"In onResume() and unable to close socket during connection failure" + e2.getMessage() + ".");
      }
    }
 
    // Create a data stream so we can talk to server.
    Log.d(TAG,"\n...Sending message to server...");
 
    try {
      outStream = btSocket.getOutputStream();
    } catch (IOException e) {
    	Log.e(TAG, "In onResume() and output stream creation failed:" + e.getMessage() + ".");
    }
 
    String message = "Hello from Android.\n";
    byte[] msgBuffer = message.getBytes();
    try {
      outStream.write(msgBuffer);
    } catch (IOException e) {
      String msg = "In onResume() and an exception occurred during write: " + e.getMessage();
      msg = msg +  ".\n\nCheck that the SPP UUID: " + MY_UUID.toString() + " exists on server.\n\n";
       
      Log.e(TAG, msg);      
    }
  }
 
  public void Pause() {
 
	  Log.d(TAG,"\n...In onPause()...");
 
    if (outStream != null) {
      try {
        outStream.flush();
      } catch (IOException e) {
    	  Log.e(TAG, "In onPause() and failed to flush output stream: " + e.getMessage() + ".");
      }
    }
 
    try     {
      btSocket.close();
    } catch (IOException e2) {
    	Log.e(TAG, "In onPause() and failed to close socket." + e2.getMessage() + ".");
    }
  }
 
 
   
   
}