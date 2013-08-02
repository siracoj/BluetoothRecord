package com.audioseparate;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "Audio Separate - MainActivity: ";
    private static final boolean DEBUG = true;
    private AudioManager mAudioManager = null;
    private Context mContext = null;
    private BluetoothHeadset mBluetoothHeadset;
    private boolean scoON = false;
    private int selected = 0;
    private String[] choices = null;
    private String[] addresses = null;
    /* Broadcast receiver for the SCO State broadcast intent.*/
    private final BroadcastReceiver mSCOHeadsetAudioState = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
        //if(DEBUG)
            // Log.e(TAG, " mSCOHeadsetAudioState--->onReceive");

        	int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);

        	if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED) {
        		DisplayToast("BT Recording is Ready");
        		scoON = true;
        		Intent recordIntent = new Intent(context, Record.class);
        		recordIntent.putExtras(intent);
        		startActivity(recordIntent);
        		
        		
        	} else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED) {
        		DisplayToast("BT Recording Disabled");
        		scoON = false;
        		
        }
      }
    };
    //PC Connect Connection Receiver
    private final BroadcastReceiver PCCommand = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

        	if(!scoON){
        		
        	}
        }
    };
    
  
  // Define Service Listener of BluetoothProfile
  private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
  		public void onServiceConnected(int profile, BluetoothProfile proxy) {
  			if (profile == BluetoothProfile.HEADSET) {
  				mBluetoothHeadset = (BluetoothHeadset) proxy;
  				List<BluetoothDevice> pairedDevices = mBluetoothHeadset.getConnectedDevices();
  			    // If there are paired devices
  			    if (pairedDevices.size() > 0) {
  			    	startSCO();
  			    	for (BluetoothDevice device : pairedDevices) {
  			    		Log.e(TAG, "BT Device :"+device.getName()+ " , BD_ADDR:" + device.getAddress());       //Print out Headset name      
  			    	}
  			    } else {
  			    	Toast.makeText(mContext, "Could not find a connected Headset, please connect a headset", Toast.LENGTH_LONG).show();
  			        return;
  			    }
  			}
  		}	
  		public void onServiceDisconnected(int profile) {
  			if (profile == BluetoothProfile.HEADSET) {
  				mBluetoothHeadset = null;
  			}
  		}
 };

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mContext = this;
        
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        int REQUEST_ENABLE_BT = RESULT_OK;
        // Check whether BT is enabled
        if (!mBluetoothAdapter.isEnabled()) {			//checks if bluetooth is enabled, if not it asks for permission to enable it
		    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
		if(REQUEST_ENABLE_BT != RESULT_OK){
			return;
		}
    }
    

    public void SCOSetup(View view){
    	Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        Iterator<BluetoothDevice> iter = devices.iterator();
        choices = new String[devices.size()];
        addresses = new String[devices.size()];
        for(int i = 0; i<devices.size(); i++){
        	BluetoothDevice temp = iter.next();
        	choices[i] = temp.getName();
        	addresses[i] = temp.getAddress();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, choices);
        
        
        
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        
        builder.setTitle("Bluetooth PC")
        	   .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.dismiss();
					}
				  })
			   .setCancelable(false)
			   .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						
						PCConnect(addresses[selected]);
						dialog.dismiss();
					}
				  })
               .setSingleChoiceItems(adapter, -1,
                      new OnClickListener() {
            	   		@Override
            	   		public void onClick(DialogInterface dialog, int which) {
            	   			selected = which;
            	   			String msg = "You Selected: " + choices[selected];
            				Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
            	   			
            	   		}
                      
               });
               

					


        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        
        dialog.show();
    }
    public void PCConnect(String PCAddr){
    	IntentFilter newintent = new IntentFilter();
        newintent.addAction("CONNECT_PC");
        mContext.registerReceiver(PCCommand, newintent);
        
        mBluetoothAdapter.getProfileProxy(mContext, mProfileListener, BluetoothProfile.HEADSET);
        
        
        BTConnect BTPC = new BTConnect(PCAddr);
        BTPC.Start();
       
       
        
        
    	
        
        
    }
    public void startSCO(){
        IntentFilter newintent = new IntentFilter();
        newintent.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        mContext.registerReceiver(mSCOHeadsetAudioState, newintent);
        
        // get the Audio Service context
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager == null){
                Log.e(TAG, "Audiomanager is null");
                finish();
                return;
        }
        
    	if(DEBUG)
    		Log.e(TAG, "SCO Start Attempted");
   		if(!scoON){
   			scoON = true;
  			mAudioManager.startBluetoothSco();
  		}else{
    		DisplayToast("Audio Stream already started, or starting");
   		}
    }
    @Override
    public void onPause(){
    	super.onPause();
    	
    }
    @Override
    public void onResume(){
    	super.onResume();
    		
    }
    
    @Override
    public void onStop(){
    	onDestroy();
    }

    public void onDestroy(){
    	super.onDestroy();
    }
    private void DisplayToast(String msg)
    {
         Toast.makeText(getBaseContext(), msg,
                 Toast.LENGTH_SHORT).show();
    }

}

