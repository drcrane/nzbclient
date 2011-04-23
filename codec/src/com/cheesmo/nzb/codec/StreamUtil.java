package com.cheesmo.nzb.codec;

import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {
	public static int CR = 0x0D;
	public static int LF = 0x0A;
	public static int NULL = 0x00;
	
	/**
	 * Read a line of data from the specified input stream into a byte array.
	 * Characters are read until either a line feed (LF), or a carriage return
	 * line feed (CRLF) are read.  Neither the CR or the LF are included in the
	 * resulting buffer.
	 * 
	 * @param input stream to read from
	 * @param buffer array to store the data in
	 * @return the number of bytes in the buffer that are part of the line read
	 * 
	 * @throws IOException
	 */
	public static int readLine(InputStream input, byte [] buffer) throws IOException {

		int prev = -1;
		for (int i = 0; i < buffer.length; i++) {
			int read = input.read();
			if (read == LF || read == NULL) {
				if (i == 0)
					return 0;
				if (prev == CR) {
					return i - 1;
				} else {
					return i;
				}
			} else {
				prev = read;
				buffer [i] = (byte) read;
			}
		}
		return -1;
	}
}
