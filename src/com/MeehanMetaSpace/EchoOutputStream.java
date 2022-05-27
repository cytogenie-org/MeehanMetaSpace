package com.MeehanMetaSpace;
import java.io.*;

public final class EchoOutputStream
	      extends OutputStream {
	private OutputStream[] streams;
	EchoOutputStream(OutputStream[] streams) {
		this.streams = streams;
	}

	public void write(int c) throws IOException {
		for (int i = 0; i < streams.length; i++) {
			streams[i].write(c);
		}
	}

	public void setStreams(OutputStream[] streams) {
		this.streams = streams;
	}

}

