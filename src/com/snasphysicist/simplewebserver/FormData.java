
package com.snasphysicist.simplewebserver;

import java.util.Hashtable;

public class FormData {

	Hashtable<String,String> data;
	
	public FormData() {
		// Initialise data dictionary
		data = new Hashtable<String,String>();
	}
	
	/*
	 * Populates the hashtable based on
	 * form data provided as a string
	 * Returns true/false for success/failure
	 */
	public boolean fromString(
		String formData
	) {	
		// May have some residual new lines from the page
		formData = formData.replaceAll(
			"\\r|\\n",
			""
		);
		
		// & separates key value pairs
		String[] lines = formData.split("&");
		for(String line : lines) {
			// = separates the key from the value
			if(line.indexOf( "=" ) > 0) {
				data.put( 
					line.substring(
						0, 
						line.indexOf("=")
					),
					line.substring(
						line.indexOf("=") + 1
					)
				);
			} else {
				// Fail if format invalid
				return false;
			}
		}
		// Return true if all key/value pairs added
		return true;
	}
	
	/*
	 * Helper methods to check if certain keys are set
	 */
	public boolean hasKey(
		String key
	) {
		return data.containsKey(key);
	}
	
	/*
	 * Note: returns true for empty input array
	 */
	public boolean hasKeys(
		String[] keys
	) {
		boolean has = true;
		for(String key : keys) {
			has = has && hasKey(key);
		}
		return has;
	}
	
	public String getValue(
		String key
	) {
		return data.get(key);
	}

}
