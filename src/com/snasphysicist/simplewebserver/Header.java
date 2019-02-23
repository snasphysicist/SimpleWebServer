package com.snasphysicist.simplewebserver ;

public class Header {

	private String name ;
	private String value ;
	
	/*
	 * Constructor from explicit name and value
	 */
	public Header( String name, String value ) {
		//Lower case and strip whitespace
		this.name = name.toLowerCase().replaceAll( "\\s" , "" ) ;
		this.value = value ;
	}
	
	/*
	 * Empty constructor for use with fromString
	 */
	public Header() {
		this.name = "" ;
		this.value = "" ;
	}
	
	public String getName() {
		return name ;
	}
	
	public String getValue() {
		return value ;
	}
	
	/*
	 * Attempt to parse a header from a single string
	 * Returns true for success, false for failure
	 */
	public boolean fromString( String headerLine ) {
		
		if( ( headerLine.indexOf( ":" ) > 0 ) 
			&& ( headerLine.indexOf( ":" ) != headerLine.length() - 1 ) ) {
			this.value = headerLine.substring( headerLine.indexOf( ":" ) + 1 ) ;
			//Remember to lower case and strip whitespace
			this.name = headerLine.replace( ":" + this.value , "" ).toLowerCase().replaceAll( "\\s" , "" ) ;
			return true ;
		} else {
			return false ;
		}
		
	}
	
	public String toString() {
		return String.format( "%s:%s", name, value ) ;
	}

}
