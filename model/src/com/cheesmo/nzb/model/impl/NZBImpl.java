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
import com.cheesmo.nzb.model.NZB;

public class NZBImpl implements NZB {
	
	
	private List files = null;
	
	public NZBImpl() {
		files = new ArrayList();
	}
	
	public List getFiles() {
		return files;
	}
	
	private static String getInQuotes(String subj) {
		int start, end;
		String part;
		//start = subj.indexOf('"');
		end = subj.lastIndexOf('"');
		if (end == -1) {
			return subj;
		}
		subj = subj.substring(0, end);
		start = subj.lastIndexOf('"');
		part = subj.substring(start + 1);
		return part;
	}

	/* Really simple insertion sort */
	public void addFile(File file) {
		/* Sort by the part numbers */
		String subj = file.getSubject();
		String part;
		String existing;
		part = getInQuotes(subj);
		System.out.println(part);
		if (files.size() > 0) {
			subj = ((File)files.get(0)).getSubject();
			existing = getInQuotes(subj);
			if (part.compareTo(existing) < 0) {
				files.add(0, file);
				return;
			}
			subj = ((File)files.get(files.size() - 1)).getSubject();
			existing = getInQuotes(subj);
			if (part.compareTo(existing) > 0) {
				files.add(file);
				return;
			}
			for (int i = 0; i < files.size(); i++) {
				subj = ((File)files.get(i)).getSubject();
				existing = getInQuotes(subj);
				if (part.compareTo(existing) < 0) {
					files.add(i, file);
					return;
				}
			}
		}
		files.add(file);
	}

	public String toString() {
		String toReturn = "[NZB]";
		for (int i = 0; i < files.size(); i++) {
			toReturn += "\n\t" + files.get(i);
		}
		return toReturn;
	}

}
