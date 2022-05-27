package com.MeehanMetaSpace.swing;
import java.io.*;
import java.util.*;
	class ByteBuffer{
		final ArrayList al=new ArrayList();
		int size=0;
		static class Buf{
			byte[] ref;
			int size;
			Buf(byte[] ref, int size) {
				this.ref = ref;
				this.size=size;
			}

		}

		byte [] getAll(){
			byte []retVal=new byte[size];
			int i=0;
		for (final Iterator it = al.iterator(); i<size && it.hasNext(); ) {
			Buf b = (Buf)it.next();
			for (int j=0;j<b.size && i<size;j++, i++){
				retVal[i]=b.ref[j];
			}
		}
		return retVal;


		}

		void read(byte []buf, int size){
			this.size+=size;
			al.add(  new Buf(buf, size));
		}
		void read(InputStream is) throws IOException{
			byte [] buf=new byte[2048];
				for (int size=1;size>0;){
					size = is.read(buf);
					if (size >0){
						read(buf, size);
					}
				}
}
	 }

