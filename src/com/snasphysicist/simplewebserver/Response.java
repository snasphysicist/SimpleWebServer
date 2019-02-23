
package com.snasphysicist.simplewebserver ;

import java.io.BufferedOutputStream;
import java.io.PrintStream ;
import java.util.LinkedList ;
import java.io.IOException ;
import java.net.Socket ;

public class Response {

	private Protocol protocol ;
	private int status ;
	private String title ;
	private LinkedList<Header> headers ;
	protected String content ;
	
	public Response( Protocol protocol, int status, String title ) {
		
		this.protocol = protocol ;
		this.status = status ;
		this.title = title ;
		this.headers = new LinkedList<Header>() ;
		this.content = "" ;
		
		/*
		 * If the protocol is HTTP/1.0
		 * the connection should be closed
		 * For HTTP/1.1, it can be kept alive
		 */
		if( protocol.equals( Protocol.HTTP10 ) ) {
			addHeader( new Header( "connection", "close" ) ) ;
		} else if ( protocol.equals( Protocol.HTTP11 ) ) {
			addHeader( new Header( "connection", "keep-alive" ) ) ;
		}
		
	}
	
	public void addHeader( Header header ) {
		headers.add( header ) ;
	}
	
	protected void generateTopMatter() {
		//First line of response format {protocol} {status code} {title} newline
		content += String.format( "%s/%s.%s %d %s\r\n",
				   protocol.name().substring( 0, 4 ), protocol.name().charAt( 4 ), 
				   protocol.name().charAt( 5 ), status, title ) ;
		//Headers each on a new line
		for( Header header : headers ) {
			content += header.toString() + "\r\n" ;
		}
	}
	
	protected void generateContent() {
		generateTopMatter() ;
	}
	
	public boolean send( Socket connection ) {
		generateContent() ;
		try {
			PrintStream dataOut = new PrintStream( new BufferedOutputStream( connection.getOutputStream() ) ) ;
			dataOut.print( content ) ;
			dataOut.flush() ;
			dataOut.close() ;
			return true ;
		} catch( IOException e ) {
			return false ;
		}
	}

}
