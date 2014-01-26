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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class MainActivity extends Activity implements OnClickListener {

	Button btn;
	EditText TextIp;
	EditText TextPort;
	RadioButton BackCamera;
	RadioButton FrontCamera;

	String DirPath = "/CameraSave";
	int timeout = 5000;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			SetEnable(true);
			Socket s = (Socket) msg.obj;
			if (s !=null ) {
				ShowPicture.sSck = s; 
				Intent intent = new Intent(MainActivity.this,ShowPicture.class);
				startActivity(intent);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "Connect Error", Toast.LENGTH_LONG).show();
			}
		}
	};

	public Handler getHandler() {
		return handler;
	}

	@Override 
    public boolean onCreateOptionsMenu(Menu menu) { 
		menu.add(0,0,0,"Exit"); 
        return super.onCreateOptionsMenu(menu); 
         
    } 
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case 0:
         finish();
        return true;
    default: break;
    }
    return false;
 }
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btn = (Button) findViewById(R.id.buttonConnect);
		btn.setOnClickListener(this);

		TextIp = (EditText) findViewById(R.id.TextIp);
		TextPort = (EditText) findViewById(R.id.TextPort);

		FrontCamera = (RadioButton) findViewById(R.id.radioFrontCamera);
		BackCamera = (RadioButton) findViewById(R.id.radioBackCamera);
		FrontCamera.setChecked(true);
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		Intent i = new Intent(Intent.ACTION_MAIN);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addCategory(Intent.CATEGORY_HOME);
		startActivity(i);
	}

	public void SetEnable(boolean enable) {
		FrontCamera.setEnabled(enable);
		BackCamera.setEnabled(enable);
		btn.setEnabled(enable);
		TextIp.setEnabled(enable);
		TextPort.setEnabled(enable);
	}

	public void connectServer() {
		final boolean isFront = this.FrontCamera.isChecked();
		final String ip = TextIp.getText().toString();
		final int port = Integer.parseInt(TextPort.getText().toString());

		if (ip.isEmpty() || (port <= 0 || port >= 655536)) {
			Toast.makeText(getApplicationContext(), "Host Setting Error",
					Toast.LENGTH_LONG).show();
			return;
		}

		SetEnable(false);

		Thread socketHandler = new Thread(new Runnable() {

			@Override
			public void run() {
				Socket s = null;
				try {
					s = new Socket();
					SocketAddress sa = new InetSocketAddress(ip, port);
					s.connect(sa, timeout);
					OutputStream os = s.getOutputStream();
					byte[] bf_send = new byte[2];
					if (isFront) {
						bf_send[0] = '0';
					} else {
						bf_send[0] = '1';
					}
					bf_send[1] = '\n';
					os.write(bf_send);
					os.flush();

				} catch (Exception e) {
					if(s!=null)
					{
						try {
							s.close();
						} catch (IOException e1) {
						}
						s = null;
					}
					Log.v("Socket", e.getMessage());
				}

				Message message = Message.obtain();
				message.obj = s;
				handler.sendMessage(message);

				//gotoShowPictureActivity(data);
			}

		});

		socketHandler.start();

	}
	
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.buttonConnect:
			connectServer();
			break;
		default:
			break;
		}
	}

//	public void gotoShowPictureActivity(byte[] data) {
//		if (data == null || data.length == 0) {
//			Looper.prepare();
//			Toast.makeText(getApplicationContext(), "Connect Error",
//					Toast.LENGTH_LONG).show();
//			Looper.loop();
//			return;
//		}
//
//		Calendar c = Calendar.getInstance();
//		String datestring = "" + c.get(Calendar.YEAR)
//				+ (c.get(Calendar.MONTH) + 1) + c.get(Calendar.DAY_OF_MONTH)
//				+ c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.HOUR)
//				+ c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);
//
//		String Path = Environment.getExternalStorageDirectory() + DirPath;
//		File dir = new File(Path);
//		dir.mkdirs();
//		Path += "/" + datestring + ".jpg";
//		File f = new File(Path);
//		FileOutputStream fo = null;
//		try {
//			fo = new FileOutputStream(f);
//			fo.write(data);
//			fo.flush();
//
//		} catch (IOException e) {
//			Log.v("PicSave", e.getMessage());
//		} finally {
//			try {
//				if (fo != null)
//					fo.close();
//			} catch (IOException e) {
//			}
//		}
//
//		Intent Intent = new Intent(MainActivity.this, ShowPicture.class);
//		Intent.putExtra("PICPATH", Path);
//		startActivity(Intent);
//	}
}
