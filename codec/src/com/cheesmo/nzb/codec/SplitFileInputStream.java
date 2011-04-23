package com.cheesmo.nzb.codec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class SplitFileInputStream extends InputStream {
	
	String dir = null;
	String [] filenames;
	FileInputStream currentFileInputStream = null;
	int currentSegment = -1;
	boolean deleteWhenFinished;
	
	public SplitFileInputStream(String dir, String [] filenames, boolean deleteWhenFinished) throws FileNotFoundException {
		this.dir = dir;
		this.filenames = filenames;
		this.deleteWhenFinished = deleteWhenFinished;
		System.out.println("No Fn: " + filenames.length);
		startNextSegment();
	}

	private void startNextSegment() throws FileNotFoundException {
		currentSegment++;
		if (currentSegment < filenames.length) {
			
			//close previous stream, and delete file if necessary
			try {
				if (currentFileInputStream != null) {
					currentFileInputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			currentFileInputStream = null;
			currentFileInputStream = new FileInputStream(new File(dir, filenames[currentSegment]));
			System.out.println("Starting: " + filenames[currentSegment]);
		} else {
			if (currentFileInputStream != null) {
				try {
					currentFileInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			currentFileInputStream = null;
			
			if (deleteWhenFinished)
				deleteFiles();
		}
		
	}

	private void deleteFiles() {
		for (int i = 0; i < filenames.length; i++) {
			File file = new File(dir, filenames[i]);
			boolean success = file.delete();
			
			if (!success) {
				file.deleteOnExit();
			}
		}
	}
	
	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public void mark(int s) {
		System.out.println("Mark Called: " + s);
	}
	
	@Override
	public void reset() throws IOException {
		if (currentFileInputStream != null) {
			currentFileInputStream.close();
		}
		currentFileInputStream = null;
		currentSegment = -1;
		/*
		System.out.println("Reset Called");
		try {
			throw new IOException("Probelmmw");
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		*/
		startNextSegment();
	}

	public int read() throws IOException {
		if (currentFileInputStream == null)
			return -1;
		int val = currentFileInputStream.read();
		if (val == -1) {
			startNextSegment();
			return read();
		}
		return val;
	}
	
	public static void main(String [] args) throws IOException {
		/*try {
			SplitFileInputStream sfis = new SplitFileInputStream("C:/news", "file.file_", 2);
			int val = sfis.read();
			while (val != -1) {
				System.out.print((char)val);
				val = sfis.read();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	public int read(byte[] arg0) throws IOException {
		if (currentFileInputStream == null)
			return -1;
		int val = currentFileInputStream.read(arg0);
		if (val == 0) {
			System.out.println("Honey it is broken!");
		}
		if (val == -1) {
			startNextSegment();
			return read(arg0);
		}
		return val;
	}

	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		if (currentFileInputStream == null)
			return -1;
		int val = currentFileInputStream.read(arg0, arg1, arg2);
		if (val == 0) {
			System.err.println("Honk Honk!!!!");
		}
		if (val == -1) {
			startNextSegment();
			return read(arg0, arg1, arg2);
		}
		return val;
	}

}
