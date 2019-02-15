
package com.snasphysicist.chinesecharacterpracticesheets;

import java.util.LinkedList ;
import java.io.UnsupportedEncodingException;
import java.io.BufferedReader ;
import java.io.InputStreamReader ;
import java.io.IOException ;
import java.net.Socket ;
import java.net.URLDecoder;
import java.util.logging.Logger ;
import java.util.logging.Level ;

public class Request {
	
	private final static Logger LOG = Logger.getLogger( Logger.class.getName() ) ;
	
	private final static int LINE_LATENCY = 10 ; //ms
	private final static int CHAR_LATENCY = 1 ; //ms
	
	private String sourceIp ;
	protected Method method ;
	private String uri ;
	private Protocol protocol ;
	private LinkedList<Header> headers ;
	protected String body ;
	
	public Request() {
		/*
		 * Do nothing
		 * fromSocket will do the processing
		 */
	}
	
	/*
	 * Construct for use with child classes
	 * Basically copies an existing request
	 */
	public Request( Request request ) {
		this.protocol = request.getProtocol() ;
		this.method = request.getMethod() ;
		this.sourceIp = request.getSourceIp() ;
		this.uri = request.getUri() ;
		this.headers = request.headers ;
		this.body = request.getBody() ;
	}
	
	public String getSourceIp() {
		return sourceIp ;
	}
	
	public Method getMethod() {
		return method ;
	}
	
	public String getUri() {
		return uri ;
	}
	
	public Protocol getProtocol() {
		return protocol ;
	}
	
	public Header getNextHeader() {
		if( headers.size() > 0 ) {
			return headers.pop();
		} else {
			return null ;
		}
	}
	
	public String getBody() {
		return body ;
	}
	
	/*
	 * Wait a certain number of milliseconds
	 */
	public void wait( int ms ) {
		try {
			Thread.sleep( ms ) ;
		} catch( InterruptedException e ) {
			//do nothing
		}
	}
	
	public boolean fromSocket( Socket connection ) {
		
		int i ;
		Header nextHeader ;
		BufferedReader dataIn ;
		String nextLine ;
		String request = "" ;
		
		//Attempt to read in the data from the socket
		try {
			dataIn = new BufferedReader( new InputStreamReader( connection.getInputStream() ) ) ;
			nextLine = dataIn.readLine() ;
			/*
			 * Everything up to the first line break
			 * i.e. all of the headers
			 */
			while( nextLine != null && !nextLine.equals( "" ) ) {
				request += nextLine + "\r\n" ;
				nextLine = dataIn.readLine() ;
				wait( LINE_LATENCY ) ;
			}
			//Since the empty line was skipped
			request += "\r\n" ;
			//Anything left after this (the body)
			while( dataIn.ready() ) {
				request += (char) dataIn.read() ;
				wait( CHAR_LATENCY ) ;
			}
			LOG.log( Level.FINE, String.format( "read request %s", request ) ) ;
		} catch( IOException e ) {
			//Return false if there is an IO error
			LOG.log( Level.WARNING, "could not read incoming request, generic io error" ) ; 
			return false ;
		}
		
		//Try to decode the request
		try {
			request = URLDecoder.decode( request , "UTF-8" ) ;
		} catch( UnsupportedEncodingException e ) {
			//Fail if the encoding is not supported
			LOG.log( Level.WARNING, "Unsupported encoding in request" ) ;
			return false ;
		}
		
		/*
		 * -1 used to ensure that we always get some output from this operation
		 * even if the string is just \r\n
		 */
		String[] lines = request.split( "\\r\\n", -1 ) ; 
		
		//A well formed request should have two spaces in the first line
		if( lines[0].split( " " ).length != 3 ) {
			//Otherwise it's no good
			LOG.log( Level.WARNING , String.format( "Malformed first line of request %s", lines[0] ) ) ;
			return false ;
		}
		
		try {
			//Try to get a method first
			method = Method.valueOf( lines[0].split( " " )[0] ) ;
		} catch( IllegalArgumentException e ) {
			LOG.log( Level.WARNING, String.format( "Unsupported request method %s", lines[0] ) ) ;
			System.out.println( String.format("REQUEST %s", request ) ) ;
			return false ;
		}
		
		uri = lines[0].split( " " )[1] ;
		
		/*
		 * Try to get the protocol second
		 * Note that the . & / in HTTP/1.0, etc must be
		 * removed for compatibility with the enum
		 */
		try {
			protocol = Protocol.valueOf( 
					lines[0].split( " " )[2].replaceAll( "\\.|/" , "" ) 
					) ;
		} catch( IllegalArgumentException e ) {
			LOG.log( Level.WARNING, String.format( "Unsupported protocol %s", lines[0] ) ) ;
			return false ;
		}
		
		//Go through line by line, headers first
		headers = new LinkedList<Header>() ;
		i = 1 ;
		while( ( i < lines.length ) && ( !lines[i].equals( "" ) ) ) {
			nextHeader = new Header() ;
			if( nextHeader.fromString( lines[i] ) ) {
				headers.add( nextHeader ) ;
			} else {
				//If the header is invalid, return false for failure
				LOG.log( Level.WARNING, String.format( "invalid header line %s", lines[1] ) ) ;
				return false ;
			}
			i++ ;
		}
		
		//Ignore the empty line
		i++ ;
		
		//Next, anything left goes into the body
		body = "" ;
		while( i < lines.length ) {
			body += String.format( "%s\r\n", lines[i] ) ;
			i++ ;
		}
		
		//Finally, set the connecting ip
		sourceIp = connection.getInetAddress().getHostName() ;
	
		//Return true to indicate success
		return true ;
		
	}

	/*
	 * Helper functions to check type of request
	 */
	public boolean isGet() {
		return method == Method.GET;
	}
	
	public boolean isPost() {
		return method == Method.POST ;
	}
	
	/*
	 * Checks if the content type is consistent with form data
	 */
	public boolean isFormData() {
		for( Header header : headers ) {
			if( header.getName().matches( "content-type" )
				&& ( header.getValue().indexOf( "application/x-www-form-urlencoded" ) >= 0 ) ) {
				// Any header is found with correct name & value, return true
				return true ;
			}
		}
		//Return false if no match is found
		return false ;
	}
	
}
