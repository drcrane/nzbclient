package com.cheesmo.nzb.client;

public class DownloadSegment {
	public String group;
	public String messageId;
	public String downloadFileName;
	
	public DownloadSegment(String group, String messageId, String downloadFilename) {
		this.group = group;
		this.messageId = messageId;
		this.downloadFileName = downloadFilename;
	}
}
