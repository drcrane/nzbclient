/*  
 * Copyright 2005 Patrick Wolf
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cheesmo.nzb.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.SocketException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.commons.net.nntp.NNTPClient;

public class NNTPConnection {
	boolean inUse = false;
	NNTPClient client =  new NNTPClient();
	ClientConfig config;
	
	protected void setInUse(boolean value) {
		this.inUse = value;
	}
	
	public boolean isInUse() {
		return this.inUse;
	}
	
	public NNTPConnection(ClientConfig config) {
		this.config = config;
	}
	
	
	private void tryConnect() throws SocketException, IOException {

		if (!client.isConnected()) {
			try {
				client.connect(config.getServer(), config.getPort());
				client.authenticate(config.getUsername(), config.getPassword());
			} catch (Exception e) {
				//let's try one more time
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				client.connect(config.getServer(), config.getPort());
				client.authenticate(config.getUsername(), config.getPassword());
			}
		}
	}
	
	public void tryDisconnect() {
		if (client != null) {
			try {
				client.disconnect();
			} catch (Throwable e) {
				//we tried
			}
		}
	}

	public boolean downloadSegment(String group, String string, String downloadName) {
		File file;
		file = new File(config.getCacheDir() + java.io.File.separator  + downloadName);
		FileOutputStream fos = null;
		try {
			tryConnect();
			client.selectNewsgroup(group);
			Reader reader = client.retrieveArticleBody(string);
			
			if (reader != null) {
				fos = new FileOutputStream(file);
				char [] buffer = new char[512];
				int charsRead = reader.read(buffer);
				while (charsRead > -1 && client.isConnected()) {
					fos.write(Charset.forName("ISO-8859-1").encode(CharBuffer.wrap(buffer, 0, charsRead)).array());
					charsRead = reader.read(buffer);
				}
				
				if (!client.isConnected()) {
					System.out.println("Client disconnected.");
				}
				reader.close();
				fos.flush();
				fos.close();
				return true;
			} else {
				//System.out.println("Problem retrieving " + string);
			}
			
		} catch (SocketException e) {
			System.out.println("Client disconnected.");
			System.out.println("Deleting incomplete segment: " + downloadName);
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException ioe) {
				System.err.println("Could not close outputstream, WEIRD!.");
				e.printStackTrace();
			}
			if (file.exists()) {
				/* file would be incomplete, will cause a problem for decoding */
				file.delete();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
