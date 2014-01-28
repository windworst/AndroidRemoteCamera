package com.fulldata.remotecamera;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataPack {
	public static boolean sendDataPack(byte[] data, OutputStream os,int OperationCode) {
		int len = data.length;
		DataOutputStream dos = new DataOutputStream(os);
		try {
			dos.writeInt(0XEEFF);
			dos.writeInt(OperationCode);
			dos.writeInt(len);
			dos.write(data);
			dos.flush();
			return true;
		} catch (IOException e) {
		}
		return false;
	}
	
	public static Object[] recvDataPack(InputStream is)
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
}
