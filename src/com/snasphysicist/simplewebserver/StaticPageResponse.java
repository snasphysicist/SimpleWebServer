package com.snasphysicist.simplewebserver;

import java.io.InputStreamReader ;
import java.net.URL ;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticPageResponse extends Response {

	private final static Logger LOG = Logger.getLogger( Logger.class.getName() ) ;
	
	String filePath ;
	
	public StaticPageResponse( Protocol protocol , int status , String title , String filePath ) {
		super(protocol , status , title ) ;
		this.filePath = filePath ;
	}
	
	private void loadBody() {
		
		try {
			String body = "" ;
			URL url = WebServer.class.getResource( filePath ) ;
			InputStreamReader fileStream = new InputStreamReader( url.openStream() ) ;
			while( fileStream.ready() ) {
				body += fileStream.read() ;
			}
			content += body ;
		}
		catch( Exception e ) {
			LOG.log( Level.WARNING , String.format( "Could not load file %s" , filePath ) ) ;
			content += "500: Experienced an Internal Server Error" ;
		}
	}
	
	protected void generateContent() {
		generateTopMatter() ;
		loadBody() ;
	}

}
