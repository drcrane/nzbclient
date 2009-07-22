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

public class Options {
	
	private boolean isModifyPrefs = false;
	private boolean isResetPrefs = false;
	private boolean isKeepParts = false;
	private boolean isJustDecode = false;
	
	public static final String RESET_SWITCH = "-reset";
	public static final String MODIFY_CONFIG_SWITCH = "-config";
	public static final String KEEP_PARTS_SWITCH = "-keepparts";
	public static final String JUST_DECODE_SWITCH = "-justdecode";
	
	public Options(String [] args) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(RESET_SWITCH)) {
				isResetPrefs = true;
			} else if (args[i].equals(MODIFY_CONFIG_SWITCH)) {
				isModifyPrefs = true;
			} else if (args[i].equals(KEEP_PARTS_SWITCH)) {
				isKeepParts = true;
			} else if (args[i].equals(JUST_DECODE_SWITCH)) {
				isJustDecode = true;
			}
		}
	}
	
	public boolean isModifyPrefs() {
		return this.isModifyPrefs;
	}
	
	public boolean isResetPrefs() {
		return this.isResetPrefs;
	}
	
	public boolean isKeepParts() {
		return this.isKeepParts;
	}

	public boolean isJustDecode() {
		return this.isJustDecode;
	}
}
