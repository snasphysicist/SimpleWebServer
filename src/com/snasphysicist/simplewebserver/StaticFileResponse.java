
package com.snasphysicist.simplewebserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Logger ;
import java.util.logging.Level;

public class StaticFileResponse extends Response {

	private final static Logger LOG = Logger.getLogger( Logger.class.getName() ) ;
	
	URL filePath ;
	
	public StaticFileResponse( Protocol protocol , int status , String title , URL filePath ) {
		super( protocol , status , title ) ;
		this.filePath = filePath ;
	}
	
	private Boolean loadBody( BufferedOutputStream dataOut ) {
		
		try {
			InputStream fileStream = filePath.openStream() ;
			int nextByte = fileStream.read() ;
			while( nextByte != -1 ) {
				dataOut.write( nextByte ) ;
				nextByte = fileStream.read() ;
			}
			fileStream.close() ;
			LOG.log( Level.FINE , String.format( "Loaded file %s" , filePath ) ) ;
			return true ;
		}
		catch( Exception e ) {
			LOG.log( Level.WARNING , String.format( "Could not load file %s" , filePath ) ) ;
			return false ;
		}
		
	}
	
	public boolean send( Socket connection ) {
		addHeader( StaticTextResponse.generateContentTypeHeader( StaticTextResponse.guessContentType( filePath ) ) ) ;
		generateContent() ;
		try {
			BufferedOutputStream byteDataOut = new BufferedOutputStream( connection.getOutputStream() ) ;
			PrintStream dataOut = new PrintStream( byteDataOut ) ;
			//Write status line & headers
			dataOut.print( content + "\r\n" ) ;
			//Write file contents
			if ( !loadBody( byteDataOut ) ) {
				content += "<html><body><p>500: Experienced an Internal Server Error</p></body></html>" ;
			}
			dataOut.flush() ;
			byteDataOut.flush() ;
			dataOut.close() ;
			byteDataOut.close() ;
			return true ;
		} catch( IOException e ) {
			return false ;
		}
	}

}
