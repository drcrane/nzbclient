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

/*
 * java -cp nzbclient/lib/commons-io-1.2.jar:nzbclient/lib/commons-net-1.4.1.jar:nzbclient/codec/bin/:nzbclient/nanoxml/bin/:nzbclient/nzbclient/bin/:nzbclient/model/bin/ com.cheesmo.nzb.client.NzbClient filetodownload.nzb
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.cheesmo.nzb.codec.DecodingException;
import com.cheesmo.nzb.codec.EncodingProbe;
import com.cheesmo.nzb.codec.SpacerootsDecoder;
import com.cheesmo.nzb.codec.SplitFileInputStream;
import com.cheesmo.nzb.codec.YEncDecoder;
import com.cheesmo.nzb.model.File;
import com.cheesmo.nzb.model.NZB;
import com.cheesmo.nzb.model.Segment;
import com.cheesmo.nzb.util.NzbUtils;

public final class NzbClient {

	public static final String CURRENT_NZB = "CurrentNZB.nzbclient";
	public static final String COMPLETED_FILES = "CompletedFilesList.nzbclient";
	public static final String CORRUPT_FILES = "CorruptFiles.nzbclient";
	
	private ClientConfig config;
	private Options options;
	private ConnectionPool pool;
	private List<String> downloadedFiles;
	private List<String> corruptFiles;

	public NzbClient(String[] args) {

		config = new ClientConfig();
		options = new Options(args); 
		
		if (options.isModifyPrefs()) {
			config.editPrefs();
			return;
		}
		
		if (options.isResetPrefs()) {
			config.reset();
			return;
		}
		
		String nzbPath = getNzbPath(args);

		java.io.File file = new java.io.File(config.getDownloadDir());
		if (!file.exists()) {
			file.mkdirs();
			System.out.println("Creating download directory " + file);
		}

		file = new java.io.File(config.getCacheDir());
		if (!file.exists()) {
			file.mkdirs();
			System.out.println("Creating cache directory " + file);
		}
		
		if (options.isJustDecode()) {
			List<String> fileList = this.getListOfFiles(config.getCacheDir());
			//launchDecode(fileList);
			return;
		}
		
		pool = new ConnectionPool(config);
		
		if (nzbPath == null) {
			System.out.println("NZB file must be specified.\n");
			printUsage();
		} else if (!(new java.io.File(nzbPath).canRead())) {
			System.out.println("Cannot read " + nzbPath );
		} else {
			start(nzbPath);
		}
	}
	
	private List<String> getListOfFiles(String directory) {
		List<String> fileList = new ArrayList<String>();
		java.io.File dir = new java.io.File(directory);
		String[] chld = dir.list();
		if (chld == null) {
			System.out.println("Specified directory does not exist or is not a directory.");
			System.exit(0);
		} else {
			for (int i = 0; i < chld.length; i++) {
				String fileName = chld[i];
				fileList.add(fileName);
			}
		}
		return fileList;
	}
	
	private static String getNzbPath(String [] args) {
		for (int i = 0; i < args.length; i++) {
			if (!args[i].startsWith("-")) {
				return args[i];
			}
		}
		return null;
	}

	public static List<String> loadFileList(String filename) {
		List<String> fileList;
		fileList = new ArrayList<String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(filename));
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (!line.equals(""))
					fileList.add(line.trim());
			}
			br.close();
		} catch (IOException ioe) {
			System.err.println("Failed to load filelist from: " + filename);
		}
		return fileList;
	}
	
	public static void saveFileList(String filename, List<String> fileList) {
		try {
			java.io.File file;
			file = new java.io.File(filename);
			if (file.exists()) {
				file.delete();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for (int i = 0; i < fileList.size(); i++) {
				bw.write(fileList.get(i) + "\r\n");
			}
			bw.flush();
			bw.close();
		} catch (IOException ioe) {
			System.err.println("Failed to save filelist to: " + filename);
		}
		return;
	}
	
	public void new_start(String nzbPath) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + CORRUPT_FILES, corruptFiles);
				NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + COMPLETED_FILES, downloadedFiles);
			}			
		});
		
		NZB nzb = getNzb(nzbPath);
		List <File> files = nzb.getFiles();
		corruptFiles = loadFileList(config.getCacheDir() + java.io.File.separator + CORRUPT_FILES);
		downloadedFiles = loadFileList(config.getCacheDir() + java.io.File.separator + COMPLETED_FILES);
//		int fileCount = 1;
		for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
			File file = i.next();
			
			if (downloadedFiles.contains(file.getSubject())) {
				System.out.println("Already got: " + file.getSubject());
				continue;
			}
			
			
		}
		NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + COMPLETED_FILES, downloadedFiles);
		NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + CORRUPT_FILES, corruptFiles);
	}
	
	public void start(String nzbPath) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + CORRUPT_FILES, corruptFiles);
				NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + COMPLETED_FILES, downloadedFiles);
			}			
		});
		
		NZB nzb = getNzb(nzbPath);
		List <File> files = nzb.getFiles();
		
		/*
		if (1 == 1) {
			return;
		}
		*/
		
		corruptFiles = loadFileList(config.getCacheDir() + java.io.File.separator + CORRUPT_FILES);
		downloadedFiles = loadFileList(config.getCacheDir() + java.io.File.separator + COMPLETED_FILES);
		int fileCount = 1;
		for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
			File file = i.next();
			
			if (downloadedFiles.contains(file.getSubject())) {
				System.out.println("Already got: " + file.getSubject());
				fileCount++;
				continue;
			}
			
			int segCount = 1;
			
			System.out.println("File " + fileCount + "/" + files.size() + " " + file.getSubject());
			List<String> segmentNames = new ArrayList<String>();
			List<DownloadThread> downloadThreads = new ArrayList<DownloadThread>();

			for (Iterator<Segment> j = file.getSegments().iterator(); j.hasNext(); ) {
				Segment seg = j.next();
				String downloadName = Integer.toString(fileCount) + "_" + Integer.toString(file.getSubject().hashCode()) + "_" + Integer.toString(seg.getNumber()) + ".yenc";
				if ((new java.io.File(config.getCacheDir() + java.io.File.separator + downloadName)).exists()) {
					//System.out.println("Already got segment: " + downloadName);
					segCount ++;
					segmentNames.add(downloadName);
					continue;
				}

				//Thread thread = createDownloadSegThread(segCount, file.getGroups().get(0).getName(), "<" + seg.getString() + ">", downloadName);
				
				DownloadThread thread = createDownloadSegThread(pool, file.getGroups().get(0).getName(), "<" + seg.getString() + ">", downloadName);
				thread.start();
				try {
					//Give some time for the thread to get started so that the joins complete in a good order.
					Thread.sleep(100);
				} catch (InterruptedException e) {

				}
				downloadThreads.add(thread);
				// Moved to when threads are finished so that we can use it 
				// to decode the file if some posts are missing.
				//segmentNames.add(downloadName);
			}

			//Wait for all the threads to finish
			boolean failure = false;
			for (Iterator<DownloadThread> t = downloadThreads.iterator(); t.hasNext(); ) {
				try {
					DownloadThread thread = t.next();
					thread.join();
					if (thread.getResult()) {
						System.out.println("\t" + segCount + "/" + file.getSegments().size() + " of " + file.getSubject());
						segmentNames.add(thread.downloadFileName);
					} else {
						System.err.println("\t" + segCount + "/" + file.getSegments().size() + " of " + file.getSubject() + " failed.");
						failure = true;
					}
					segCount++;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (!failure) {
				launchDecode(segmentNames, corruptFiles);
				downloadedFiles.add(file.getSubject());
			} else {
				System.err.println("Couldn't download all segments.");
				launchDecode(segmentNames, corruptFiles);
				if (segmentNames.size() > 0) {
					downloadedFiles.add(file.getSubject());
				}
				/*
				 * Segments do not need cleaning up, already done by SplitFileInputStream
				 * (but only if the user wanted ;-))
				for (Iterator<String> j = segmentNames.iterator(); j.hasNext(); ) {
					//clean up segments
					new java.io.File(config.getCacheDir() + java.io.File.separator  + j.next()).delete();
				}
				*/
			}
			fileCount++;
		}
		NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + COMPLETED_FILES, downloadedFiles);
		NzbClient.saveFileList(config.getCacheDir() + java.io.File.separator + CORRUPT_FILES, corruptFiles);
	}
	
	private void launchDecode(List<String> segmentNames, List<String> corruptFiles) {
		try {
			//Make sure we decode in correct order
			Collections.sort(segmentNames, new Comparator<String> () {

				public int compare(String id1, String id2) {
					id1 = id1.substring(id1.indexOf("_") + 1);
					Integer segmentNumber1 = Integer.parseInt(id1.substring(id1.indexOf("_") + 1, id1.indexOf(".")));
					id2 = id2.substring(id2.indexOf("_") + 1);
					Integer segmentNumber2 = Integer.parseInt(id2.substring(id2.indexOf("_") + 1, id2.indexOf(".")));
					return segmentNumber1.compareTo(segmentNumber2);
				}});

			//if (1 == 1)
			//return;
			
			SplitFileInputStream sfis = new SplitFileInputStream(config.getCacheDir(), (String[])segmentNames.toArray(new String[segmentNames.size()]), !options.isKeepParts());
			System.out.print("Decoding... ");
			
			if (EncodingProbe.discoverEncoding(sfis) == EncodingProbe.UU_ENCODED) {
				System.out.println("UUEncoding... giving it a try.");
				
				SpacerootsDecoder.uudecode(sfis, config.getDownloadDir());
			}
			else
			if (EncodingProbe.discoverEncoding(sfis) == EncodingProbe.YENC_ENCODED) {
				System.out.println("yEncoded!!");
				YEncDecoder decoder = new YEncDecoder(sfis, config.getDownloadDir());
				String fileDecoded = decoder.decode();
				if (decoder.segmentsMissing() && fileDecoded != null) {
					corruptFiles.add(fileDecoded);
				}
				
				if (fileDecoded != null) {
					System.out.println("Decoding " + fileDecoded + " complete.");
				} else {
					System.out.println("Couldn't decode.");
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DecodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private DownloadThread createDownloadSegThread(ConnectionPool pool, String group, String messageId, String downloadFilename) {
		return new DownloadThread(pool, group, messageId, downloadFilename );

	}

	private NZB getNzb(String path) {
		return NzbUtils.parseFile(path);
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		NzbClient client = new NzbClient(args);
	}
	
	public static void printUsage() {
		System.out.println("Usage: nzbclient [options] <NZB FILE>");
		System.out.println("Downloads the specified file.\n");
		System.out.println("  -reset       resets all config settings and runs interactive configuration tool.");
		System.out.println("  -config      modify config settings.");
		System.out.println("  -keepparts   keep downloaded parts after decoding.");
		System.out.println("  -justdecode  decode files in the cache dir,");
		System.out.println("               only useful if nzb has one file in it - better support to be added.");
	}

}
