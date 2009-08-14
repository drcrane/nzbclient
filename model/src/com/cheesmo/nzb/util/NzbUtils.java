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
package com.cheesmo.nzb.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.n3.nanoxml.IXMLBuilder;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

import org.xml.sax.SAXException;

import com.cheesmo.nzb.model.NZB;
import com.cheesmo.nzb.model.impl.NanoXMLNzbParser;
import com.cheesmo.nzb.model.impl.NzbParser;

public class NzbUtils {
	
	/**
	 * Parse an NZB file at the specified path.
	 * 
	 * @param pathToFile file to parse
	 * @return an NZB if the file was parsed successfully, otherwise null is returned.
	 */
	public static NZB parseFile(String pathToFile) {
		
		try {
			IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
			IXMLReader reader = StdXMLReader.fileReader(pathToFile);
			IXMLBuilder builder;
			builder = new NanoXMLNzbParser();
			parser.setReader(reader);
			parser.setBuilder(builder);
			return (NZB)parser.parse();
		} catch (Exception e) {
			System.err.println("Could not parse " + pathToFile + ":  " + e.getMessage());
		}
		
		/*
		NzbParser parser = new NzbParser();
		try {
			return parser.parse(pathToFile);
		} catch (FileNotFoundException e) {
			System.err.println("Could not parse " + pathToFile + ":  " + e.getMessage());
		} catch (SAXException e) {
			System.err.println("Could not parse " + pathToFile + ":  " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Could not parse " + pathToFile + ":  " + e.getMessage());
		}
		*/
		return null;
	}

}
