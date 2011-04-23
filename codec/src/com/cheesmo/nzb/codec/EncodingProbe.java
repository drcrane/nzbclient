/*
 * Probes a SplitFileInputStream to discover the encoding
 * Currently supports UUEnc and yEnc.
 */

package com.cheesmo.nzb.codec;

import java.io.IOException;
import java.io.InputStream;

public class EncodingProbe {
	public static final int ENCODING_UNKNOWN = 0;
	public static final int UU_ENCODED = 1;
	public static final int YENC_ENCODED = 2;
	
	public static int discoverEncoding(InputStream is) throws IOException {
		byte[] line = new byte[512];
		String lineStr;
		int bytesRead;
		int encType = ENCODING_UNKNOWN;
		
		if (is.markSupported()) {
			is.mark(1024 * 1024);
		} else if (is instanceof SplitFileInputStream) {
			/* it is ok, it will work */
		} else {
			throw new IOException("Cannot probe this stream, markSupported() == false.");
		}
		
		for (int i = 0; i < 10; i ++) {
			bytesRead = StreamUtil.readLine(is, line);
			lineStr = new String(line, 0, bytesRead);
			
			if (lineStr.startsWith(YEncConstants.YMARKER_BEGIN)) {
				encType = YENC_ENCODED;
				break;
			}
			
			if (lineStr.startsWith("begin")) {
				encType = UU_ENCODED;
				break;
			}
		
		}
		
		is.reset();
		return encType;
	}
}
