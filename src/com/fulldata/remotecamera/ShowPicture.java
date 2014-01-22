package com.fulldata.remotecamera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import java.lang.Thread;

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

			Bitmap bm = BitmapFactory.decodeFile(Path); 
//			
//	        int imageWidth = bm.getWidth();  
//	        int imageHeight = bm.getHeight();  
//			
//	        int windowWidth = iv.getWidth();
//	        int windowHeight = iv.getHeight();
//	  
//	        float scaleX = ((float)windowWidth) / imageWidth;  
//	        float scaleY = ((float)windowHeight) / imageHeight;  
//	        Log.v("Scale",""+scaleX+":"+scaleY);
//	        float scale = scaleX > scaleY ? scaleY : scaleX;
	        
//	        if(scale>=1)
//	        {
//	        	scale = 1;
//	        }
			
			if(bm!=null)
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				
				bm.compress(CompressFormat.JPEG, (int)70, bos);
				byte[] data = bos.toByteArray(); 
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				iv.setImageBitmap(bitmap);  
			}

			  
		}
	}

}
