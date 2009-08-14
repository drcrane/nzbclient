package com.cheesmo.nzb.client;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SegmentQueue {
	Object mutex;
	List<DownloadSegment> segmentList;
	
	public SegmentQueue() {
		segmentList = new ArrayList<DownloadSegment>();
		mutex = segmentList;
	}
	
	public void addSegment(String group, String messageId, String downloadFilename) throws InterruptedException {
		synchronized (mutex) {
			DownloadSegment seg = new DownloadSegment(group, messageId, downloadFilename);
			segmentList.add(seg);
		}
	}
	
	public DownloadSegment getSegment(String group, String messageId, String downloadFilename) throws InterruptedException {
		DownloadSegment seg = null;
		synchronized (mutex) {
			if (segmentList.size() == 0) {
				return null;
			}
		}
		return seg;
	}
}
