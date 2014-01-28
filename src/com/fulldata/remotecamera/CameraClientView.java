package com.fulldata.remotecamera;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraClientView extends Activity implements OnTouchListener {

	static Socket sSck = null;
	ImageView mIv = null;
	long mViewClickTime = 0;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@SuppressLint("ShowToast")
		public void handleMessage(Message msg) {
			if(msg.obj instanceof String)
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
	
	public Object[] recvDataPack(InputStream is)
	{
		DataInputStream dis = new DataInputStream(is);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int Sign = dis.readInt();
			if(Sign!=0XEEFF)
			{
				return null;
			}
			Integer OperationCode = dis.readInt();
			int len = dis.readInt();
			int buf_len = 1024;
			byte[] data = new byte[buf_len];
			int i = 0;
			while(i<len)
			{
				int nread = buf_len;
				if(nread + i > len)
				{
					nread = len - i;
				}
				nread = dis.read(data, 0, nread);
				if(nread<=0)
				{
					continue;
				}
				i+=nread;
				
				baos.write(data, 0, nread);
			}
			Object[] rets = new Object[2];
			rets[0] = baos.toByteArray();
			rets[1] = OperationCode;
			return rets;
		} catch (IOException e) {
		}
		return null;
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

		Runnable r = new Runnable(){

			@Override
			public void run() {
				if (sSck != null) {
					try {
						InputStream is = sSck.getInputStream();
						while(true)
						{
							Object[] rets = recvDataPack(is);
				
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
	}
	
	public void RemoteTakePicture()
	{
		long currentTime = System.currentTimeMillis();
		if( currentTime - mViewClickTime > 2000 )
		{
			Toast.makeText(getApplicationContext(), "Click twice catch photos", Toast.LENGTH_SHORT).show();
		}
		else
		{
			Runnable r = new Runnable(){
				@Override
				public void run() {
					try {
						OutputStream os = sSck.getOutputStream();
						OutputStreamWriter osw = new OutputStreamWriter(os);
						osw.write("0");
						osw.flush();
					} catch (Exception e) {
					}
				}
			};
			Thread t = new Thread(r);
			t.start();
			Toast.makeText(getApplicationContext(), "Take it", Toast.LENGTH_SHORT).show();
		}
		mViewClickTime = currentTime;
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
			RemoteTakePicture();
			Log.v("Touch","Touch");
		}
		return false;
	}

}
