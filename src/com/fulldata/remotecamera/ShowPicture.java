package com.fulldata.remotecamera;

import java.io.*;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.Toast;

public class ShowPicture extends Activity {

	static Socket sSck = null;
	ImageView mIv = null;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@SuppressLint("ShowToast")
		public void handleMessage(Message msg) {
			byte[] data = (byte[]) msg.obj;
			if(data!=null)
			{
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				mIv.setImageBitmap(bitmap);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Connect CutDown", Toast.LENGTH_LONG);
				onBackPressed();
			}
		}
	};

	public Handler getHandler() {
		return handler;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_picture);
		mIv = (ImageView) findViewById(R.id.imageView1);

		Runnable r = new Runnable(){

			@Override
			public void run() {
				if (sSck != null) {
					try {
						InputStream is = sSck.getInputStream();
						while(true)
						{
							byte[] data = DataPack.recvDataPack(is);
							Message message = Message.obtain();
							message.obj = data;
							handler.sendMessage(message);
						}
						
					} catch (IOException e) {
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

}
