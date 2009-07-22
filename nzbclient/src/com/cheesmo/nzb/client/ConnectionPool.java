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

import java.util.concurrent.Semaphore;

public final class ConnectionPool {
	
	private NNTPConnection [] connections = null;
	private Semaphore sem = null; 
	
	public ConnectionPool(ClientConfig cfg) {
		final int connCount = cfg.getMaxConnections();
		connections = new NNTPConnection[connCount];
		
		for (int i = 0; i < connCount; i++) {
			connections[i] = new NNTPConnection(cfg);
		}
		sem = new Semaphore(connCount);
		
		//Make sure we always disconnect
		Runtime.getRuntime().addShutdownHook(new Thread() {

			public void run() {
				for (int i = 0; i < connCount; i++) {
					connections[i].tryDisconnect();
				}
			}			
		});
	}
	
	
	public NNTPConnection getConnection() {
		try {
			sem.acquire();
			for (int i = 0; i < connections.length; i++) {
				synchronized (connections[i]) {
					if (!connections[i].isInUse()) {
						connections[i].setInUse(true);
						return connections[i];
					}
				}
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void releaseConnection(NNTPConnection connection) {
		connection.setInUse(false);
		sem.release();
	}

}
