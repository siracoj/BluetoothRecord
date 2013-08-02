package com.audioseparate;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Record extends Activity {
    AudioManager mAudioManager;
    AlertDialog alertDialog = null;
    private static final String LOG_TAG = "AudioSeparate - Record";
    int count = 0;

    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_record);
		if(!Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).canWrite()){
			String msg = "Cannot write the recorded files to: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
			Toast.makeText(this,  msg  , Toast.LENGTH_LONG).show();
			this.finish();
		}
		 mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	}
	public void onPause(){
		super.onPause();
	}
	public void onDestroy(){
		super.onDestroy();
		if(alertDialog != null){
			alertDialog.dismiss();
		}
		
		
	}

	public void onRecord(View view){
		
	
		String mFilename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath();
		String mBTFileName = mFilename + "/BTRecord.3gp";
		//String mMICFileName = mFilename + "/MICRecord.3gp";
		final RecordThread BT = new RecordThread(false,mBTFileName);
		//final RecordThread MIC = new RecordThread(false,mMICFileName);
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
 
			// set title
			alertDialogBuilder.setTitle("Alert");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Recording...")
				.setCancelable(false)
				.setNeutralButton("Stop",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						// if this button is clicked, stop recording
							BT.stopRecording();
			//				MIC.stopRecording();
							mAudioManager.stopBluetoothSco();
							String countmsg = "Changed " + count +" times";
							Toast.makeText(getParent(), countmsg, Toast.LENGTH_LONG).show();
						// To do: start separation and play back here
						
						dialog.dismiss();
					}
				  });
			
		try{
			BT.prepareRecording();
			//MIC.prepareRecording();
		}catch(Exception e){
			Toast.makeText(this, "Recording prepare failed", Toast.LENGTH_SHORT).show();
			BT.stopRecording();
			//MIC.stopRecording();
			this.finish();
		}
		try{
			mAudioManager.stopBluetoothSco();
			BT.start(); //Try to minimize delay here some how...
			//MIC.start();
		}catch(Exception e){
			Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
			Log.e(LOG_TAG, "Record failed");
			BT.stopRecording();
			//MIC.stopRecording();
			mAudioManager.stopBluetoothSco();
			this.finish();
		}
			
 
				// create alert dialog
				alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
			
	}
}
final class RecordThread extends Thread implements Runnable{
	
	public String mFile;
	protected boolean isBT;
	
    private Camera access;
	private MediaRecorder mRecorder;
	private static final String LOG_TAG = "Recording thread";
	
	public RecordThread(boolean isBT, String file){
		this.isBT = isBT;
		this.mFile = file;
	}
	@Override
	public void run() throws RuntimeException{
		
		try{
			this.mRecorder.start();
		}catch(Exception e){
			Log.e(LOG_TAG,"Run Method Exception");
			RuntimeException re = new RuntimeException();
			throw re;
		}
	
	}
	public void prepareRecording() throws Exception{
        this.mRecorder = new MediaRecorder();
        if(isBT){
        	this.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        }else{
        	int camID = Camera.CameraInfo.CAMERA_FACING_BACK;
        	openCam camT = new openCam(camID);
        	camT.start();
        	try{
        		camT.join();
        	}catch(InterruptedException e){
        		Log.e(LOG_TAG, "Camera got Interrupted... restarting prepare");
        		this.prepareRecording();
        	}
        	this.access = camT.getCam();
        	this.access.unlock();
        	this.mRecorder.setCamera(access);
        	this.mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);

        }
        this.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        this.mRecorder.setOutputFile(mFile);
        this.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
        	this.mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            if(access != null){
            	access.release();
            }
            throw e;
        }

        
    }
	

    public void stopRecording() {
        if(access != null){
        	access.release();
        }
    	try{
    		mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
    	}catch(Exception e){		
        mRecorder = null;
    	}
    }
	
}
final class openCam extends Thread implements Runnable{ // thread to get camera
	
	Camera cam = null;
	int camId;
	
	public openCam(int camId){
		this.camId = camId;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			this.cam = Camera.open(this.camId);
		}catch(Exception e){
			Log.e("Get Camera", "Could not open camera");
			if(this.cam != null){
				cam.release();
			}
		}
	}
	public Camera getCam() throws Exception{
		if(this.cam == null){
			Log.e("Get Camera", "Failed to get Camera");
			throw new Exception();
		}else{
			return this.cam;
		}
	}
	
}