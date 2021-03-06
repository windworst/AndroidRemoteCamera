package com.fulldata.remotecamera;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
public class CameraClientActivity extends Activity implements OnClickListener {

	Button mConnectButton;
	Button mAutoConnectButton;
	EditText mIpText;
	EditText mPortText;
	RadioButton mBackCameraRadio;
	RadioButton mFrontCameraRadio;

	String DirPath = "/CameraSave";
	int timeout = 5000;

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			SetEnable(true);
			if(msg.obj==null)
			{
				Toast.makeText(getApplicationContext(), "Connect Error",
						Toast.LENGTH_LONG).show();
			}
			else if (msg.obj instanceof Socket) {
				Socket s = (Socket) msg.obj;
				{
					CameraClientView.sSck = s;
					Intent intent = new Intent(CameraClientActivity.this,
							CameraClientView.class);
					startActivity(intent);
				}
			}
			else if(msg.obj instanceof String)
			{
				if((String)msg.obj=="")
				{
					Toast.makeText(getApplicationContext(), "Auto Connect Error",
							Toast.LENGTH_LONG).show();
					return;
				}
				mIpText.setText((String)msg.obj);
				connectServer();
			}
		}
	};

	public Handler getHandler() {
		return handler;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "Exit");
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			finish();
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mConnectButton = (Button) findViewById(R.id.buttonConnect);
		mConnectButton.setOnClickListener(this);

		mAutoConnectButton = (Button) findViewById(R.id.buttonAutoConnect);
		mAutoConnectButton.setOnClickListener(this);

		mIpText = (EditText) findViewById(R.id.TextIp);
		mPortText = (EditText) findViewById(R.id.TextPort);

		mFrontCameraRadio = (RadioButton) findViewById(R.id.radioFrontCamera);
		mBackCameraRadio = (RadioButton) findViewById(R.id.radioBackCamera);
		mFrontCameraRadio.setChecked(true);
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
		mFrontCameraRadio.setEnabled(enable);
		mBackCameraRadio.setEnabled(enable);
		mConnectButton.setEnabled(enable);
		mAutoConnectButton.setEnabled(enable);
		mIpText.setEnabled(enable);
		mPortText.setEnabled(enable);
	}

	public void AutoConnectServer() {
		final int port = Integer.parseInt(mPortText.getText().toString());
		SetEnable(false);
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				DatagramSocket ds = null;
				try {
					ds = new DatagramSocket(port);
					ds.setSoTimeout(3000);
					byte[] data = new byte[100];
					DatagramPacket pack = new DatagramPacket(data, data.length);
					ds.receive(pack);

					String host = pack.getAddress().toString().substring(1);
					
					Message message = Message.obtain();
					message.obj = host;
					handler.sendMessage(message);
				} catch (Exception e) {
					Message message = Message.obtain();
					message.obj = "";
					handler.sendMessage(message);
				}
				finally
				{
					if(ds!=null)
					{
						ds.close();
					}
				}
			}
		});
		t.start();
	}

	public void connectServer() {
		final String ip = mIpText.getText().toString();
		final int port = Integer.parseInt(mPortText.getText().toString());
		final int CameraMode = this.mFrontCameraRadio.isChecked() ? 0 : 1;
		final int width = 500;
		final int height = 500;
		final int quality = 50;

		if (ip.isEmpty() || (port <= 0 || port >= 65536)) {
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
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);
					dos.writeInt(CameraMode);
					dos.writeInt(width);
					dos.writeInt(height);
					dos.writeInt(quality);

					DataPack.sendDataPack(baos.toByteArray(), os, -1);
					// byte[] bf_send = new byte[2];
					// if (isFront) {
					// bf_send[0] = '0';
					// } else {
					// bf_send[0] = '1';
					// }
					// bf_send[1] = '\n';
					// os.write(bf_send);
					// os.flush();

				} catch (Exception e) {
					if (s != null) {
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

				// gotoShowPictureActivity(data);
			}

		});

		socketHandler.start();

	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.buttonConnect:
			connectServer();
			break;
		case R.id.buttonAutoConnect:
			AutoConnectServer();
			break;
		default:
			break;
		}
	}

	// public void gotoShowPictureActivity(byte[] data) {
	// if (data == null || data.length == 0) {
	// Looper.prepare();
	// Toast.makeText(getApplicationContext(), "Connect Error",
	// Toast.LENGTH_LONG).show();
	// Looper.loop();
	// return;
	// }
	//
	// Calendar c = Calendar.getInstance();
	// String datestring = "" + c.get(Calendar.YEAR)
	// + (c.get(Calendar.MONTH) + 1) + c.get(Calendar.DAY_OF_MONTH)
	// + c.get(Calendar.HOUR_OF_DAY) + c.get(Calendar.HOUR)
	// + c.get(Calendar.MINUTE) + c.get(Calendar.SECOND);
	//
	// String Path = Environment.getExternalStorageDirectory() + DirPath;
	// File dir = new File(Path);
	// dir.mkdirs();
	// Path += "/" + datestring + ".jpg";
	// File f = new File(Path);
	// FileOutputStream fo = null;
	// try {
	// fo = new FileOutputStream(f);
	// fo.write(data);
	// fo.flush();
	//
	// } catch (IOException e) {
	// Log.v("PicSave", e.getMessage());
	// } finally {
	// try {
	// if (fo != null)
	// fo.close();
	// } catch (IOException e) {
	// }
	// }
	//
	// Intent Intent = new Intent(MainActivity.this, ShowPicture.class);
	// Intent.putExtra("PICPATH", Path);
	// startActivity(Intent);
	// }
}
