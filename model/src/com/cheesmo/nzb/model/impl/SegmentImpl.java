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
package com.cheesmo.nzb.model.impl;

import com.cheesmo.nzb.model.Segment;

public class SegmentImpl implements Segment {
	
	int bytes = 0;
	int number = 0;
	String string = "";
	
	public SegmentImpl(int bytes, int number, String string) {
		this.bytes = bytes;
		this.number = number;
		this.string = string;
	}
	
	public SegmentImpl() {
		
	}

	public int getBytes() {
		return bytes;
	}

	public int getNumber() {
		return number;
	}

	public String getString() {
		return string;
	}
	
	public String toString() {
		return "\t\t\t" + bytes +":" + number +":" + string;
	}


}
