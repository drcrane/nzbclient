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
package com.cheesmo.nzb.model.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cheesmo.nzb.model.Group;
import com.cheesmo.nzb.model.NZB;
import com.cheesmo.nzb.model.Segment;
import com.cheesmo.nzb.model.impl.FileImpl;
import com.cheesmo.nzb.model.impl.GroupImpl;
import com.cheesmo.nzb.model.impl.NzbParser;
import com.cheesmo.nzb.model.impl.SegmentImpl;
import com.cheesmo.nzb.util.NzbUtils;

public class ParserTest extends TestCase {
	
	public void testParseSimpleFile() {
		URL url = ClassLoader.getSystemClassLoader().getResource("com/cheesmo/nzb/model/test/test1.nzb");
		File file = new File(url.getFile());
		assertTrue(file.canRead());
		NzbParser parser = new NzbParser();
		try {
			NZB nzb = parser.parse(file.getAbsolutePath());
			assertNotNull(nzb);
			List files = nzb.getFiles();
			assertNotNull(files);
			assertEquals(files.size(), 47);
			
			//Check all the files
			for (Iterator i = files.iterator(); i.hasNext(); ) {
				com.cheesmo.nzb.model.File nzbFile = (com.cheesmo.nzb.model.File) i.next();
				assertNotNull(nzbFile);
				assertTrue(nzbFile.getDate() > 0);
				assertNotNull(nzbFile.getPoster());
				assertNotNull(nzbFile.getSubject());
				//check all the groups
				List groups = nzbFile.getGroups();
				for (Iterator j = groups.iterator(); j.hasNext(); ) {
					Group group = (Group) j.next();
					assertNotNull(group);
					assertNotNull(group.getName());
				}
				
				//test segments
				for (Iterator j = nzbFile.getSegments().iterator(); j.hasNext(); ) {
					Segment segment = (Segment) j.next();
					assertTrue(segment.getBytes() > -1);
					assertNotNull(segment.getString());
					assertTrue(segment.getNumber() > 0);
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testInputSource() {
	
		URL url = ClassLoader.getSystemClassLoader().getResource("com/cheesmo/nzb/model/test/test1.nzb");
		try {
			NzbParser parser = new NzbParser();
			//multiple parsing
			assertNotNull(parser.parse(new InputSource(url.openStream())));
			assertNotNull(parser.parse(new InputSource(url.openStream())));
			assertNotNull(parser.parse(new InputSource(url.openStream())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void testNzbUtil() {
		URL url = ClassLoader.getSystemClassLoader().getResource("com/cheesmo/nzb/model/test/test1.nzb");
		File file = new File(url.getFile());
		assertNotNull(NzbUtils.parseFile(file.getAbsolutePath()));
	}
	
	public void testGroup() {
		Group group = new GroupImpl("GroupName");
		assertEquals(group.getName(), "GroupName");
	}
	
	public void testSegment() {
		int bytes = 100;
		int number = 200;
		String string = "<sjflkjslfjasljfldsfas>";
		Segment segment = new SegmentImpl(bytes, number, string);
		assertEquals(bytes, segment.getBytes());
		assertEquals(number, segment.getNumber());
		assertEquals(string, segment.getString());
	}
	
	public void testFile() {
		String poster = "posterName";
		String subject = "subject";
		int date = 99999;
		com.cheesmo.nzb.model.File file = new FileImpl(poster, subject, date);
		assertEquals(poster, file.getPoster());
		assertEquals(subject, file.getSubject());
		assertEquals(date, file.getDate());
		
		Group group1 = new GroupImpl("group1");
		Group group2 = new GroupImpl("group2");
		
		file.addGroup(group1);
		file.addGroup(group2);
		assertEquals(file.getGroups().size(), 2);
		
		Segment segment1 = new SegmentImpl(100, 1, "seg1");
		Segment segment2 = new SegmentImpl(100, 2, "seg2");
		file.addSegment(segment1);
		file.addSegment(segment2);
		assertEquals(file.getSegments().size(), 2);
	}
}
