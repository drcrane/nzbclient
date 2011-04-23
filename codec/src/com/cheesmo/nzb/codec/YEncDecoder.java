/*  
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
package com.cheesmo.nzb.codec;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class YEncDecoder implements YEncConstants {
	
	String destDir;
	
	//set by firest constructor
	String fileToDecode = null;
	boolean delete = false;

	//second constructor
	InputStream toDecode;
	boolean bitsMissing = false;

	static final String CLASSNAME = YEncDecoder.class.getName();
	private static Logger logger = Logger.getLogger(CLASSNAME);
	
	public YEncDecoder(String fileToDecode, String destDir, boolean deleteOnCompletion) {
		logger.entering(CLASSNAME, "YEncDecoder");
		this.fileToDecode = fileToDecode;
		this.destDir = destDir;
		this.delete = deleteOnCompletion;
		logger.exiting(CLASSNAME, "YEncDecoder");
	}
	
	public YEncDecoder(InputStream toDecode, String destDir) {
		this.toDecode = toDecode;
		this.destDir = destDir;
	}

	/**
	 * Decode and return the file name.
	 * @return
	 * @throws DecodingException
	 */
	public String decode() throws DecodingException {
		
		boolean decoding = false;
		try {
			BufferedInputStream input = null;
			if (fileToDecode != null)
				input = new BufferedInputStream(new FileInputStream(fileToDecode), 1024 * 16);
			else 
				input = new BufferedInputStream(toDecode);
			
			byte [] bytes = new byte[512];
			int bytesRead = StreamUtil.readLine(input, bytes);
			if (bytesRead == -1) {
				return null;
			}
			long bytesWritten = 0;
			String line = new String(bytes, 0, bytesRead);
			String outputName = null;
			OutputStream out = null;
			FileOutputStream fos = null;
			
			while (line != null) {
				if (line.startsWith(YMARKER_BEGIN)) {
					if (decoding) {
						throw new DecodingException(YMARKER_BEGIN + " not expected.");
					}
					decoding = true;
					if (out == null) {
						YBegin ybegin = new YBegin(line);
						System.out.println("-- " + line);
						outputName = ybegin.name;
						System.out.println("Name: " + outputName);
						out = new BufferedOutputStream(fos = new FileOutputStream(new File(destDir, outputName)));
					}
				} else if (line.startsWith(YMARKER_END)) {
					if (!decoding) {
						throw new IOException(YMARKER_END + " not expected.");
					}
					decoding = false;
				} else if (line.startsWith(YMARKER_PART)) {
					YPart ypart = new YPart(line);
					/* maybe could write zeros to the file here */
					/* alternatively we could flush the file
					 * here and then check the position,
					 * this way is probably better though. */
					if ((ypart.begin - 1) != bytesWritten) {
						bitsMissing = true;
						out.flush();
						fos.getChannel().position(ypart.begin - 1);
					}
				} else if (decoding) {
					bytesWritten += decodeLine(bytes, bytesRead, out);
				}
				bytesRead = StreamUtil.readLine(input, bytes);
				if (bytes != null && bytesRead > 1 && bytes[0] == '=' && bytes[1] == 'y')
					line = new String(bytes, 0, bytesRead);	//parse the ypart
				else if (bytesRead == -1){
					input.close();
					
					//For zero-length files, out is never initialized
					if (out != null) {
						out.flush();
						out.close();
					} else {
						//TODO:  Record error?  null should be returned
					}
					
					if (delete) {
						System.out.println("Removing " + fileToDecode + ":  " + (new File(fileToDecode)).delete());
					}
					return outputName;
				} else {
					line = "";
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean segmentsMissing() {
		return bitsMissing;
	}
	
	/**
	 * Decodes bytes.  The first length bytes of the byte array are decoded to
	 * the specified output stream.
	 * 
	 * @param bytes array of encoded bytes to decode
	 * @param length number of bytes in the array to decode
	 * @param out stream to write the results to
	 * @return bytes written to outputstream
	 * @throws IOException
	 */
	private int decodeLine(byte [] bytes, int length, OutputStream out) throws IOException {
		boolean escape = false;
		int byteCounter = 0;
		for (int i = 0; i < length; i++) {
			int b = (bytes[i]);
			if (!escape && b == ESCAPE_MODIFIER) {
				escape = true;
			} else if (escape) {
				out.write((b - 64 - 42));
				byteCounter++;
				escape = false;
			} else {
				out.write((b - 42));
				byteCounter++;
			}
		}
		return byteCounter;
	}
	
	public static class YPart {
		int begin = -1;
		int end = -1;
		
		public YPart(String text) throws DecodingException {
			StringTokenizer tokenizer = new StringTokenizer(text);
			if (!tokenizer.nextToken().equals(YEncConstants.YMARKER_PART)) {
				throw new DecodingException("Not valid " + YEncConstants.YMARKER_PART);
			}
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.startsWith(YEncConstants.YTAG_BEGIN)) {
					begin = Integer.parseInt(token.substring(YEncConstants.YTAG_BEGIN.length()));
				} else if (token.startsWith(YEncConstants.YTAG_END)) {
					end = Integer.parseInt(token.substring(YEncConstants.YTAG_END.length()));
				}
			}
		}
	}
	
	public static class YBegin {
		
		int line = -1;
		int size = -1;
		int total = -1;
		String name = null;
		
		//=ybegin line=128 size=123456 name=mybinary.dat
		public YBegin(String text) throws DecodingException {
			StringTokenizer tokenizer = new StringTokenizer(text);
			
			//Check that beginning starts with =ybegin
			if (!tokenizer.nextToken().equals(YEncConstants.YMARKER_BEGIN)) {
				throw new DecodingException("Not valid " + YEncConstants.YMARKER_BEGIN);
			}
			
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.startsWith(YEncConstants.YTAG_LINE)) {
					line = Integer.parseInt(token.substring(YEncConstants.YTAG_LINE.length()));
				} else if (token.startsWith(YEncConstants.YTAG_SIZE)) {
					size = Integer.parseInt(token.substring(YEncConstants.YTAG_SIZE.length()));
				} else if (token.startsWith(YEncConstants.YTAG_TOTAL)) {
					total = Integer.parseInt(token.substring(YEncConstants.YTAG_TOTAL.length()));
				}
			}
			
			//name is always remainder of line--must have name
			int index = text.indexOf(YEncConstants.YTAG_NAME);
			if (index > -1) {
				name = text.substring(index + YEncConstants.YTAG_NAME.length());
			} else {
				throw new DecodingException("No name specified in =ybegin");
			}
		}
		
		public String toString() {
			logger.entering(CLASSNAME, "toString");
			StringBuffer toReturn = new StringBuffer();
			toReturn.append("[ybegin]");
			if (name != null) {
				toReturn.append(" Name: " + name);
			}
			if (line > -1) {
				toReturn.append(", Line: " + line);
			}
			if (size > -1) {
				toReturn.append(", Size: " + size);
			}
			if (total > -1) {
				toReturn.append(", Total: " + total);
			}
			logger.exiting(CLASSNAME, "toString");
			return toReturn.toString();
		}

	}
	
	public static class YEnd {
		
		int size = -1;
		String crc32 = null;
		
		//=yend size=584 crc32=ded29f4f 
		public YEnd(String text) throws DecodingException {
			StringTokenizer tokenizer = new StringTokenizer(text);
			
			//Check that beginning starts with =yend
			if (!tokenizer.nextToken().equals(YEncConstants.YMARKER_END)) {
				throw new DecodingException("Not valid " + YEncConstants.YMARKER_END);
			}
			
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (token.startsWith(YEncConstants.YTAG_SIZE)) {
					size = Integer.parseInt(token.substring(YEncConstants.YTAG_SIZE.length()));
				} else if (token.startsWith(YEncConstants.YTAG_CRC32)) {
					crc32 = token.substring(YEncConstants.YTAG_CRC32.length());
				}
			}
		}
		
		public String toString() {
			StringBuffer toReturn = new StringBuffer();
			toReturn.append("[yend]");
			if (size > -1)
				toReturn.append(" Size: " + size);
			if (crc32 != null)
				toReturn.append(", Crc32: " + crc32);
			return toReturn.toString();
		}
	}
	
	public static void main(String [] args) throws DecodingException {

		YEncDecoder decoder ;//= new YEncDecoder("c:/00000005.ntx", "c:/", false);
	//	decoder.decode();
		
		String [] fileNames = new String[] {"00000020.ntx","00000021.ntx"};
		try {
			SplitFileInputStream sfis = new SplitFileInputStream("C:/", fileNames, true);
			decoder = new YEncDecoder(sfis, "C:/");
			decoder.decode();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//YDecoder decoder = new YDecoder(new SplitFileInputStream(".")
	//	decoder.decode();
	}
	
	
}
