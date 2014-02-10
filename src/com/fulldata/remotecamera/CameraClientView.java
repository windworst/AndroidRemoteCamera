package com.fulldata.remotecamera;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class CameraClientView extends Activity implements OnTouchListener, OnClickListener, OnSeekBarChangeListener {

	static Socket sSck = null;
	ImageView mIv = null;
	Button mButtonTakePhoto = null;
	SeekBar mSeekBarQuality = null;
	TextView mTextViewQuality = null;
	long mViewClickTime = 0;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@SuppressLint("ShowToast")
		public void handleMessage(Message msg) {
			if(msg.obj==null)
			{
				finish();
			}
			else if(msg.obj instanceof String)
			{
				String savePath = (String)msg.obj;
				if(savePath=="")
				{
					savePath = "Failed";
				}
				Toast.makeText(getApplicationContext(), "Save: "+savePath, Toast.LENGTH_LONG).show();
			}
			else if(msg.obj instanceof Object[])
			{
				Object[] rets = (Object[])msg.obj;
				byte[] data = (byte[])rets[0];
				if(data!=null)
				{
					Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
					mIv.setImageBitmap(bitmap);
				}
				else
				{
					finish();
				}
			}
		}
	};

	public Handler getHandler() {
		return handler;
	}
	
	WakeLock mWakeLock = null;
	private void acquireWakeLock() {

		if (null == mWakeLock) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
					| PowerManager.ON_AFTER_RELEASE, "LOCK");
			if (null != mWakeLock) {
				mWakeLock.acquire();
			}
		}
	}

	// 释放设备电源锁
	private void releaseWakeLock() {
		if (null != mWakeLock) {
			mWakeLock.release();
			mWakeLock = null;
		}
	}
	
	public String savetoPic(byte[] data,String Path) {
		Calendar c = Calendar.getInstance();
		String datestring = "" + c.get(Calendar.YEAR)
				+ String.format("%02d",(c.get(Calendar.MONTH) + 1)) 
				+ String.format("%02d", c.get(Calendar.DAY_OF_MONTH))
				+ String.format("%02d", c.get(Calendar.HOUR_OF_DAY))
				+ String.format("%02d", c.get(Calendar.MINUTE)) 
				+ String.format("%02d", c.get(Calendar.SECOND));

		File dir = new File(Path);
		dir.mkdirs();
		Path += "/" + datestring + ".jpg";

		File f = new File(Path);
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(f);
			fo.write(data);
			fo.flush();
			fo.close();
			return Path;

		} catch (IOException e) {
			Log.v("PicSave", e.getMessage());
		} finally {
			try {
				if (fo != null)
					fo.close();
			} catch (IOException e) {
			}
		}
		return "";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_picture);
		mIv = (ImageView) findViewById(R.id.CameraImageView);
		mIv.setOnTouchListener(this);
		
		mButtonTakePhoto = (Button)findViewById(R.id.buttonTakePhoto);
		mButtonTakePhoto.setOnClickListener(this);
		
		mTextViewQuality = (TextView)findViewById(R.id.textViewQuality);
		mSeekBarQuality = (SeekBar)findViewById(R.id.seekBarQuality);
		mSeekBarQuality.setOnSeekBarChangeListener(this);
		
		setQualityTextView(50);
		mSeekBarQuality.setProgress(50);

		Runnable r = new Runnable(){

			@Override
			public void run() {
				if (sSck != null) {
					try {
						InputStream is = sSck.getInputStream();
						while(true)
						{
							Object[] rets = DataPack.recvDataPack(is);
				
							if(rets!=null) //Handle rets
							{
								byte[] data  = (byte[]) rets[0];
								Integer OperationCode = (Integer)rets[1];
								
								Message message = Message.obtain();
								switch(OperationCode)
								{
								case 0:
									String mDirPath = "/savePic";
									String Path = Environment.getExternalStorageDirectory() + mDirPath ;
									String savePath = savetoPic(data,Path);
									
									message.obj = savePath;
									handler.sendMessage(message);
									break;
								case 1:
									message.obj = rets;
									handler.sendMessage(message);
									break;
								default:break;
								}
							}
							else //Close
							{
								try {
									if(sSck!=null)
									{
										sSck.close();
										sSck = null;
									}
								} catch (Exception e) {
								}
								break;
							}
	
						}
						
					} catch (Exception e) {

					}
					finally
					{
						Message message = Message.obtain();
						message.obj = null;
						handler.sendMessage(message);
					}
					
				}
			}

		};
		Thread t = new Thread(r);
		t.start();
		
		acquireWakeLock();
	}
	
	public void onDestroy()
	{
		releaseWakeLock();
		super.onDestroy();
	}
	
	public void RemoteTakePicture()
	{
		Runnable r = new Runnable(){
			@Override
			public void run() {
				try {
					OutputStream os = sSck.getOutputStream();
					
					byte[] data = new byte[1];
					data[0] = (byte)0;
					os.write(data);
					os.flush();
				} catch (Exception e) {
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
		Toast.makeText(getApplicationContext(), "Take it", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onBackPressed() {
		if(sSck != null)
		{
			try {
				sSck.close();
			} catch (IOException e) {
			}
			sSck = null;
		}
		super.onBackPressed();
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		{
			long currentTime = System.currentTimeMillis();
			if( currentTime - mViewClickTime > 2000 )
			{
				Toast.makeText(getApplicationContext(), "Click twice catch photos", Toast.LENGTH_SHORT).show();
			}
			else
			{
				RemoteTakePicture();
			}
			mViewClickTime = currentTime;
		}
		return false;
	}

	@Override
	public void onClick(View arg0) {
		switch(arg0.getId())
		{
		case R.id.buttonTakePhoto:
			RemoteTakePicture();
			break;
		default:break;
		}
	}
	
	void setQualityTextView(int progress)
	{
		mTextViewQuality.setText("quality:"+progress+"%");
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		if(arg1<5)
		{
			arg1 = 5;
			arg0.setProgress(arg1);
		}
		setQualityTextView(arg1);
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		final int quality = arg0.getProgress();
		Runnable r = new Runnable(){
			@Override
			public void run() {
				try {
					OutputStream os = sSck.getOutputStream();
					
					byte[] data = new byte[1];
					data[0] = (byte)quality;
					os.write(data);
					os.flush();
				} catch (Exception e) {
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
	}

}
