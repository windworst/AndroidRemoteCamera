package com.fulldata.remotecamera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class ShowPicture extends Activity {

	ImageView iv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_picture);
		iv = (ImageView) findViewById(R.id.imageView1);
		
		String Path = this.getIntent().getStringExtra("PICPATH").toString();

		{
			Toast.makeText(getApplicationContext(), "Save to "+Path+" Success", Toast.LENGTH_SHORT).show();			  
/*
			        Options options = new Options();  
  
			        options.inJustDecodeBounds = true;  
			        BitmapFactory.decodeFile(Path, options);  
			        int imageWidth = options.outWidth;  
			        int imageHeight = options.outHeight;  

			        int windowWidth = iv.getWidth();
			        int windowHeight = iv.getHeight();
			  
			        int scaleX = imageWidth / windowWidth;  
			        int scaleY = imageHeight / windowHeight;  
			        int scale = 1;  
			        if (scaleX >= scaleY && scaleX >= 1) {  
			            scale = scaleX;  
			        } else if (scaleY >= scaleX && scaleY >= 1) {  
			            scale = scaleY;  
			        }  

			        options.inJustDecodeBounds = false;  

			        options.inSampleSize = scale;  */
			        Bitmap bitmap = BitmapFactory.decodeFile(Path);  
			        iv.setImageBitmap(bitmap);  
			  
		}
	}

}
