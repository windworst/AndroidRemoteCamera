package com.fulldata.remotecamera;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {

	Button btn;
	EditText TextIp;
	EditText TextPort;
	RadioButton FrontCamera;
	
	String DirPath = "/CameraSave";
	int timeout = 5;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btn = (Button)findViewById(R.id.buttonConnect);
		btn.setOnClickListener(this);
		
		TextIp = (EditText)findViewById(R.id.TextIp);
		TextPort = (EditText)findViewById(R.id.TextPort);
		
		FrontCamera = (RadioButton)findViewById(R.id.radioFrontCamera);
		FrontCamera.setChecked(true);
	}
	
	public void TakePicture()
	{
		final boolean isFront = this.FrontCamera.isChecked();
		final String ip = TextIp.getText().toString();
		final int port = Integer.parseInt(TextPort.getText().toString());
		
		if(ip.isEmpty() || (port<=0 || port >=655536) )
		{
			Toast.makeText(getApplicationContext(), "Host Setting Error", Toast.LENGTH_LONG).show();
			return;
		}
		
		Thread socketHandler = new Thread(new Runnable(){

			@Override
			public void run() {
				byte [] data = null;
				try {
					Socket s = new Socket();
					SocketAddress sa = new InetSocketAddress(ip,port);
					s.connect(sa,timeout);
					InputStream is = s.getInputStream();
					OutputStream os = s.getOutputStream();
					byte[] bf_send = new byte[1];
					if(isFront)
					{
						bf_send[0] = '0';
					}
					else
					{
						bf_send[0] = '1';
					}
					os.write(bf_send);
					os.flush();
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] buf = new byte[8192];
					while (is.read(buf)!=-1)
					{
						baos.write(buf);
					}
					data  = baos.toByteArray();
					s.close();
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e){
					
				}
				gotoShowPictureActivity(data);
			}
			
		});
		
		socketHandler.start();

	}
	
	public void gotoShowPictureActivity(byte[] data)
	{
		if(data==null)
		{
			Looper.prepare();
			Toast.makeText(getApplicationContext(), "Connect Error", Toast.LENGTH_LONG).show();
			Looper.loop();
			return;
		}
		
		
		  Calendar c = Calendar.getInstance();
		  String datestring = "" + c.get(Calendar.YEAR) + 
		  ( c.get(Calendar.MONTH) + 1) +
		  c.get(Calendar.DAY_OF_MONTH) +
		  c.get(Calendar.HOUR_OF_DAY) +
		  c.get(Calendar.HOUR) +
		  c.get(Calendar.MINUTE) + 
		  c.get(Calendar.SECOND); 
		  
		  String Path = Environment.getExternalStorageDirectory() + DirPath;
		  File dir = new File(Path);
		  dir.mkdirs();
		  Path += "/" + datestring + ".jpg";
		  File f = new File(Path);
		  FileOutputStream fo = null;
		  try {
			  fo = new FileOutputStream(f);
			  fo.write(data);
			  fo.flush();

		} catch (IOException e) {
			Log.v("PicSave", e.getMessage());
		}finally
		{
			try {
				if(fo!=null)
					fo.close();
			} catch (IOException e) {
			}
		}
		 
		
		Intent Intent = new Intent(MainActivity.this,ShowPicture.class);
		Intent.putExtra("PICPATH", Path);
		startActivity(Intent);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch(arg0.getId())
		{
		case R.id.buttonConnect:
			TakePicture();
			break;
		default:break;
		}
	}

}
