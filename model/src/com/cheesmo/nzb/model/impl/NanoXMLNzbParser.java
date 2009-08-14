package com.cheesmo.nzb.model.impl;

import java.io.Reader;

import com.cheesmo.nzb.model.File;
import com.cheesmo.nzb.model.Group;
import com.cheesmo.nzb.model.NZB;
import com.cheesmo.nzb.model.Segment;

import net.n3.nanoxml.IXMLBuilder;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

public class NanoXMLNzbParser implements IXMLBuilder {

	NZB nzb;
	File currFile;
	Group currGroup;
	Segment currSegment;
	String localName;
	Object retObj;
	
	@Override
	public void addAttribute(String key, String arg1, String arg2,
			String value, String type) throws Exception {
		// TODO Auto-generated method stub
		if (localName.equals("file")) {
			if (key.equals("poster")) {
				((FileImpl)currFile).poster = value;
			}
			if (key.equals("subject")) {
				((FileImpl)currFile).subject = value;
			}
			if (key.equals("date")) {
				int date = 0;
				try {
					date = Integer.parseInt(value);
				} catch (Exception e) {
					
				}
				((FileImpl)currFile).date = date;
			}
		}
		if (localName.equals("segment")) {
			if (key.equals("bytes")) {
				((SegmentImpl)currSegment).bytes = Integer.parseInt(value);
			}
			if (key.equals("number")) {
				((SegmentImpl)currSegment).number = Integer.parseInt(value);
			}
		}
	}

	@Override
	public void addPCData(Reader rdr, String file, int lineNumber) throws Exception {
		char[] buf = new char[4096];
		int sz;
		sz = rdr.read(buf);
		if (localName.equals("group")) {
			currGroup = new GroupImpl(new String(buf, 0, sz));
		}
		if (localName.equals("segment")) {
			((SegmentImpl)currSegment).string = new String(buf, 0, sz);
		}
	}

	@Override
	public void elementAttributesProcessed(String name, String nsPrefix, String nsSystemID)
			throws Exception {
	}

	@Override
	public void endElement(String name, String nsPrefix, String nsSystemID)
			throws Exception {
		if (name.equals("group")) {
			currFile.addGroup(currGroup);
		}
		if (name.equals("segment")) {
			currFile.addSegment(currSegment);
		}
		if (name.equals("file")) {
			nzb.addFile(currFile);
		}
		if (name.equals("nzb")) {
			retObj = nzb;
		}
	}

	@Override
	public Object getResult() throws Exception {
		return retObj;
	}

	@Override
	public void newProcessingInstruction(String arg0, Reader arg1)
			throws Exception {
	}

	@Override
	public void startBuilding(String arg0, int arg1) throws Exception {
		nzb = new NZBImpl();
	}

	@Override
	public void startElement(String name, String nsPrefix, String nsSystemID,
			String systemID, int lineNr) throws Exception {
		localName = name;
		if (localName.equals("file")) {
			currFile = new FileImpl();
		}
		if (localName.equals("group")) {
			
		}
		if (localName.equals("segment")) {
			currSegment = new SegmentImpl();
		}
	}
	
	public static void main(String[] args) throws Exception {
		IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
		IXMLReader reader = StdXMLReader.fileReader("/home/ben/sample.nzb");
		IXMLBuilder builder;
		builder = new NanoXMLNzbParser();
		
		parser.setReader(reader);
		parser.setBuilder(builder);
		Object f = parser.parse();
		System.out.println(f.toString());
	}
	
}


