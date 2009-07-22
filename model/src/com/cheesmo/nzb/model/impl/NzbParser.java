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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.cheesmo.nzb.model.File;
import com.cheesmo.nzb.model.NZB;

/**
 * This is a SAX parser for parsing an NZB file.
 * 
 * Typical usage of this parser would be:<p>
 * <code>
 * NzbParser parser = new NzbParser();<br>
 * NZB nzb = parser.parseFile(<i>path to file</i>);
 * </code>
 */
public class NzbParser extends DefaultHandler {
	
	NZB nzb = new NZBImpl();
	File currentFile;
	int currentSegmentBytes = 0;
	int currentSegmentNumber = 0;
	String currentSegmentString = "";
	String currentGroup = "";
	String curElement = "";
	
	
	public NZB parse(String path) throws SAXException, FileNotFoundException, IOException {
		return parse(new InputSource(path));
	}
	
	public NZB parse(InputSource source) throws SAXException, IOException {
		NzbParser parser = new NzbParser();
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(parser);
		reader.parse(source);
		return parser.nzb;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		if (curElement.equals("group")) {
			currentGroup += new String(ch, start, length);
		} else if (curElement.equals("segment")) {
			currentSegmentString += new String(ch, start, length);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (localName.equals("file")) {
			nzb.addFile(currentFile);
		} else if (localName.equals("group")) {
			currentFile.addGroup(new GroupImpl(currentGroup));
			currentGroup = "";
		} else if (localName.equals("segment")) {
			currentFile.addSegment(new SegmentImpl(currentSegmentBytes, currentSegmentNumber, currentSegmentString));
			currentSegmentString = "";
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		curElement = localName;
		if (localName.equals("file")) {
			
			String subject = attributes.getValue("subject");
			String poster = attributes.getValue("poster");
			String dateS = attributes.getValue("date");
			int date = 0;
			try {
				date = Integer.parseInt(dateS);
			} catch (Exception e) {
				
			}
			currentFile = new FileImpl(poster, subject, date);
		} else if (localName.equals("group")) {
			currentGroup = "";
		} else if (localName.equals("segment")) {
			try {
				currentSegmentBytes = Integer.parseInt(attributes.getValue("bytes"));
				currentSegmentNumber= Integer.parseInt(attributes.getValue("number"));
			} catch (Exception e) {
				
			}
		}
	}


}
