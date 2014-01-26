package com.fulldata.remotecamera;

import java.io.*;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraClientView extends Activity {

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
//				Matrix matrix = new Matrix();
//				matrix.postRotate(90);
//				Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				mIv.setImageBitmap(bitmap);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Connect CutDown", Toast.LENGTH_LONG);
				try {
					if(sSck!=null)
					{
						sSck.close();
						sSck = null;
					}
				} catch (Exception e) {
				}
				finish();
			}
		}
	};

	public Handler getHandler() {
		return handler;
	}
	
	public byte[] recvDataPack(InputStream is)
	{
		DataInputStream dis = new DataInputStream(is);
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
			return baos.toByteArray();
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_picture);
		mIv = (ImageView) findViewById(R.id.imageView1);

		Runnable r = new Runnable(){

			@Override
			public void run() {
				if (sSck != null) {
					try {
						InputStream is = sSck.getInputStream();
						while(sSck != null)
						{
							byte[] data = recvDataPack(is);
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
