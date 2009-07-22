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

public interface NZB {
	
	/**
	 * Get the list of files within the NZB
	 * 
	 * @return a list of File objects
	 */
	public List<File> getFiles();
	
	/**
	 * Add a new file to the NZB
	 * 
	 * @param file File to add
	 */
	public void addFile(File file);


}
