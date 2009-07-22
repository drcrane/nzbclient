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

import java.util.ArrayList;
import java.util.List;

import com.cheesmo.nzb.model.File;
import com.cheesmo.nzb.model.Group;
import com.cheesmo.nzb.model.Segment;

public class FileImpl implements File {
	
	List groups = null;
	List segments = null;
	int date;
	String poster;
	String subject;
	
	public FileImpl(String poster, String subject, int date) {
		groups = new ArrayList();
		segments = new ArrayList();
		this.date = date;
		this.poster = poster;
		this.subject = subject;
	}

	public List getSegments() {
		return segments;
	}

	public List getGroups() {
		return groups;
	}

	public void addSegment(Segment segment) {
		segments.add(segment);
	}

	public void addGroup(Group group) {
		groups.add(group);
	}

	public String getPoster() {
		return poster;
	}

	public String getSubject() {
		return subject;
	}

	public int getDate() {
		return date;
	}
	
	public String toString() {
		String toReturn = "";
		toReturn += "\n[File]";
		toReturn += "\n\t\tdate=" + date;
		toReturn += "\n\t\tposter=" + poster;
		toReturn += "\n\t\tsubject=" + subject;
		for (int i = 0; i < groups.size(); i++) {
			toReturn += groups.get(i).toString();
		}
		for (int i = 0; i < segments.size(); i++) {
			toReturn += segments.get(i).toString();
		}
		return toReturn;
	}

}
