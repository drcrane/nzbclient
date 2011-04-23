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
package com.cheesmo.nzb.codec;

public interface YEncConstants {
	
	public static String YMARKER_BEGIN = "=ybegin";
	public static String YMARKER_PART = "=ypart";
	public static String YMARKER_END = "=yend";

	public static String YTAG_PART = "part=";
	public static String YTAG_TOTAL = "total=";
	public static String YTAG_LINE = "line=";
	public static String YTAG_SIZE = "size=";
	public static String YTAG_NAME = "name=";
	public static String YTAG_BEGIN = "begin=";
	public static String YTAG_END = "end=";
	public static String YTAG_PCRC32 = "pcrc32=";
	public static String YTAG_CRC32 = "crc32=";
	
	public static int ESCAPE_MODIFIER = 0x3D;
	public static int CR = 0x0D;
	public static int LF = 0x0A;
	public static int NULL = 0x00;
}
