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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.cheesmo.nzb.codec.DecodingException;
import com.cheesmo.nzb.codec.SplitFileInputStream;
import com.cheesmo.nzb.codec.YEncDecoder;
import com.cheesmo.nzb.model.File;
import com.cheesmo.nzb.model.NZB;
import com.cheesmo.nzb.model.Segment;
import com.cheesmo.nzb.util.NzbUtils;

public final class NzbClient {

	private ClientConfig config;
	private Options options;
	private ConnectionPool pool;

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
			launchDecode(fileList);
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

	public void start(String nzbPath) {

		NZB nzb = getNzb(nzbPath);
		List<File> files = nzb.getFiles();
		int fileCount = 1;
		for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
			File file = i.next();
			System.out.println("File " + fileCount + "/" + files.size() + " " + file.getSubject());
			List<String> segmentNames = new ArrayList<String>();
			List<DownloadThread> downloadThreads = new ArrayList<DownloadThread>();

			for (Iterator<Segment> j = file.getSegments().iterator(); j.hasNext(); ) {
				Segment seg = j.next();
				String downloadName = file.getSubject().hashCode() + "_" + seg.getNumber() + ".yenc";

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
			int segCount = 1;
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
				launchDecode(segmentNames);
			} else {
				System.err.println("Couldn't download all segments.");
				launchDecode(segmentNames);
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
		
	}
	
	private void launchDecode(List<String> segmentNames) {

		try {

			//Make sure we decode in correct order
			Collections.sort(segmentNames, new Comparator<String> () {

				public int compare(String id1, String id2) {
					Integer segmentNumber1 = Integer.parseInt(id1.substring(id1.indexOf("_") + 1, id1.indexOf(".")));
					Integer segmentNumber2 = Integer.parseInt(id2.substring(id2.indexOf("_") + 1, id2.indexOf(".")));
					return segmentNumber1.compareTo(segmentNumber2);
				}});

			SplitFileInputStream sfis = new SplitFileInputStream(config.getCacheDir(), (String[])segmentNames.toArray(new String[segmentNames.size()]), !options.isKeepParts());
			System.out.println("Decoding . . .");
			YEncDecoder decoder = new YEncDecoder(sfis, config.getDownloadDir());
			String fileDecoded = decoder.decode();
			
			if (fileDecoded != null) {
				System.out.println("Decoding " + fileDecoded + " complete.");
			} else {
				System.out.println("Couldn't decode.");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DecodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
