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


public class DownloadThread extends Thread {
	String group;
	String messageId;
	String downloadFileName;
	ConnectionPool pool;
	boolean result = false;
	
	public DownloadThread(ConnectionPool pool, String group, String messageId, String downloadFilename) {
		this.group = group;
		this.messageId = messageId;
		this.downloadFileName = downloadFilename;
		this.pool = pool;
	}

	public void run() {
		NNTPConnection connection = null;
		try {
			connection = pool.getConnection();
			this.result = connection.downloadSegment(group, messageId, downloadFileName);
		} finally {
			if (connection != null)
				pool.releaseConnection(connection);
		}
	}
	
	public boolean getResult() {
		return result;
	}

}
