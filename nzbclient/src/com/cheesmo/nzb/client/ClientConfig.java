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
package com.cheesmo.nzb.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class ClientConfig {

	Preferences prefs = null;
	Properties cfg = null;


	public ClientConfig() {
		cfg = new Properties();
		try {
			cfg.load(ClassLoader.getSystemResourceAsStream("com/cheesmo/nzb/client/preferences.cfg"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		prefs = Preferences.userNodeForPackage(this.getClass());

		if (!prefs.getBoolean("init", false)) {
			try {
				initPrefs();
				display();
				prefs.putBoolean("init", true);
			} catch (Exception e) {
				System.err.println("Error setting preferences.");
				e.printStackTrace();
			}
		}
	}
	
	public void editPrefs() {
		display();
		System.out.println("Select the option to modify [1-" + (getNumOfPrefs() + 1) + "]\n");
		int chosen = readOption();
		while (chosen != (getNumOfPrefs() + 1)) {

			if (chosen < 1 || chosen > getNumOfPrefs()) { 
				System.out.println("Select the option to modify [1-" + (getNumOfPrefs() + 1) + "]\n");
				chosen = readOption();
			} else { 

				String id = cfg.getProperty("pref.id." + chosen);
				String display = cfg.getProperty("pref.display." + chosen);
				String type = cfg.getProperty("pref.type." + chosen);
				String def = cfg.getProperty("pref.default." + chosen);
				initPref(id, display, type, def);
				display();
				chosen = readOption();
			}
		}
	}

	private int readOption() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String value;
		int intValue = -1;
		try {
			value = reader.readLine();
			intValue = Integer.parseInt(value);
		} catch (Throwable ex) {
			//ex.printStackTrace();

		}

		return intValue;
	}



	private void initPrefs() {
		System.out.println("This is your first time running nzb client.");
		for (int prefNum = 1; ; prefNum++) {
			String id = cfg.getProperty("pref.id." + prefNum);
			String display = cfg.getProperty("pref.display." + prefNum);
			String type = cfg.getProperty("pref.type." + prefNum);
			String def = cfg.getProperty("pref.default." + prefNum);
			if (id == null)
				break;
			else
				initPref(id, display, type, def);
		}

	}

	private void initPref(String id, String display, String type, String def){

		if (type.equals("string")) {
			getStringPref(display, id, prefs.get(id, def), false);
		} else if (type.equals("int")) {
			getIntegerPref(display, id, prefs.get(id, def));
		} else if (type.equals("bool")) {
			getBooleanPref(display, id, prefs.get(id, def));
		} else if (type.equals("password")) {
			getStringPref(display, id, null, true);
		}
	}

	private String getStringPref(String string, String prefId, String def, boolean isPassword) {
		System.out.println(string + (def != null ? " [" + def + "]:" : ":\n"));
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		String value = "";
		try {
			if (isPassword && isPasswordSupported()) {
				
				//Use java6 echoless support
				value = new String(System.console().readPassword());

			} else {

				value = reader.readLine();
			}
			if ("".equals(value.trim()) && def != null)
				value = def;
			prefs.put(prefId, value);
			return value;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}

	private boolean isPasswordSupported() {

		try {
			Class.forName("java.io.Console");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}

	}

	private int getIntegerPref(String string, String prefId, String def) {
		String value = getStringPref(string, prefId, def, false);
		while (!isValidInt(value)) {
			prefs.remove(prefId);
			System.out.println("Valid integer required.");
			value = getStringPref(string, prefId, def, false);
		}
		prefs.putInt(prefId, Integer.parseInt(value));
		return Integer.parseInt(value);

	}

	private boolean getBooleanPref(String string, String prefId, String def) {
		String value = getStringPref(string, prefId, def, false);
		while (!isValidBool(value)) {
			prefs.remove(prefId);
			System.out.println("Valid boolean required.");
			value = getStringPref(string, prefId, def, false);
		}
		prefs.putBoolean(prefId, Boolean.parseBoolean(value));
		return Boolean.parseBoolean(value);

	}


	private boolean isValidInt(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	private boolean isValidBool(String value) {
		try {
			Boolean.parseBoolean(value);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}

	public String getServer() {
		return prefs.get("server.name", null);
	}

	public String getUsername() {
		return prefs.get("server.username", null);
	}

	public String getPassword() {

		return prefs.get("server.password", null);
	}

	public int getPort() {
		return prefs.getInt("server.port", 23);
	}

	public boolean requiresAuthentication() {
		return prefs.getBoolean("server.auth", true);
	}

	public int getMaxConnections() {
		return prefs.getInt("server.connections", 2);
	}

	public String getDownloadDir() {
		return getPropertyBasedPref("dir.download");
	}

	public String getCacheDir() {
		return getPropertyBasedPref("dir.cache");
	}

	/**
	 * Gets a preferences with expandable system properties in the value, like ${user.dir}
	 * @param key
	 * @return
	 */
	private String getPropertyBasedPref(String key) {
		String def = null;
		for (int i = 1; i < getNumOfPrefs() + 1; i++) {
			if (key.equals(cfg.getProperty("pref.id." + i))) {
				def = cfg.getProperty("pref.default." + i);
			}
		}
		String pref = prefs.get(key, def);

		if (pref.indexOf("$") == -1)
			return pref;

		for (Iterator<Object> i = System.getProperties().keySet().iterator(); i.hasNext(); ) {
			String propName  = (String) i.next();
			if (pref.indexOf("${" + propName + "}") > -1) {
				pref = pref.replace( "${" + propName + "}", System.getProperty(propName));
			}
		}
		return pref;
	}

	public void display() {
		System.out.println("[Preferences]");
		int displayLength = 16;
		for (int prefNum = 1; ; prefNum++) {
			String id = cfg.getProperty("pref.id." + prefNum);
			String display = cfg.getProperty("pref.display." + prefNum);
			String type = cfg.getProperty("pref.type." + prefNum);
			String def = cfg.getProperty("pref.default." + prefNum);

			if (id == null) {
				break; //that's all there is
			}

			if (type.equals("password")) {
				System.out.println("[" + prefNum + "] " + pad(display, displayLength) + ": " + mask(prefs.get(id, def)));
			} else {
				System.out.println("[" + prefNum + "] " + pad(display, displayLength) + ": " + prefs.get(id, def));
			}

		}

		System.out.println("[" + (getNumOfPrefs() + 1) + "] Quit");

	}

	private int getNumOfPrefs() {
		int prefNum = 1;
		while (true) {

			String id = cfg.getProperty("pref.id." + prefNum);
			if (id == null) {
				break; //that's all there is
			}
			prefNum++;
		}
		return prefNum - 1;
	}

	/**
	 * Pads the front of a String with spaces until it's the correct length
	 * @param display
	 * @param displayLength
	 * @return
	 */
	private String pad(String display, int displayLength) {
		while (display.length() < displayLength) {
			display = " " + display;
		}
		return display;
	}
	
	private String mask(String value) {
		StringBuffer toReturn = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			toReturn.append('*');
		}
		return toReturn.toString();
	}

	public void reset() {
		try {
			prefs.removeNode();
			System.out.println("Preferences have been reset.");
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
