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
package com.cheesmo.nzb.model;

import java.util.List;

public interface File {
	
	/**
	 * 
	 * @return list of Segments making up the file
	 */
	public List<Segment> getSegments();
	
	/**
	 * @return List of Groups the file is located within
	 */
	public List<Group> getGroups();
	
	/**
	 * 
	 * @return name of poster
	 */
	public String getPoster();
	
	/**
	 * 
	 * @return subject
	 */
	public String getSubject();
	
	/**
	 * 
	 * @return date
	 */
	public int getDate();
	
	/**
	 * Insert a new Segment into the file
	 * @param segment to add
	 */
	public void addSegment(Segment segment);
	
	/**
	 * Insert a new Group into the file
	 * @param group to add
	 */
	public void addGroup(Group group);

}
