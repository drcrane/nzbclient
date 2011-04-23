/*
 * Simple wrapper for org.saceroots.jarmor.AbstractDecoder
 */

package com.cheesmo.nzb.codec;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.spaceroots.jarmor.UUDecoder;

public class SpacerootsDecoder {
	public static void uudecode(InputStream is, String downloadDir) throws IOException {
		byte[] miniBuf = new byte[1024 * 1024];
		int bytesRead;
		OutputStream os = null;
		
		// What is the name of the bloody file, arg!
		UUDecoder uudec = new UUDecoder(is);
		bytesRead = uudec.read(miniBuf);
		
		if ((uudec.getName() != null || !uudec.getName().equals("")) && bytesRead > 0) {
			os = new FileOutputStream(new File(downloadDir, uudec.getName()));
			os.write(miniBuf, 0, bytesRead);
		} else {
			throw new IOException("Error in stream, no filename found.");
		}
		
		//os = new FileOutputStream(new File(downloadDir, "vfkubiuby"));
		
		while ((bytesRead = uudec.read(miniBuf)) > 0) {
			os.write(miniBuf, 0, bytesRead);
			//System.out.println("Sysby " + bytesRead);
		}
		
		System.out.println("BR: " + bytesRead);
		
		os.flush();
		os.close();
	}
}
