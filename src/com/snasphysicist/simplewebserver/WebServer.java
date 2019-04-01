
package com.snasphysicist.simplewebserver ;

import java.net.ServerSocket ;
import java.net.Socket ;
import java.io.IOException ;
import java.util.logging.Logger ;
import java.util.logging.Level ; 
import java.util.function.Function ;

//http://cs.au.dk/~amoeller/WWW/examples/FileServer.java

public class WebServer implements Runnable {
	
	private final static Logger LOG = Logger.getLogger( Logger.class.getName() ) ;
	
	private int port ;
	private ServerSocket socket ;
	private Router router ;
		
	public WebServer( int port , Router router ) {
		this.port = port ;
		this.router = router ;
		LOG.log( Level.INFO, String.format( "Server initialized on port %d" , port ) ) ;
	}
	
	public void addRoute( String uri, Function<Request,Response> handler ) {
		router.addRoute( uri, handler ) ;
	}
	
	/*
	 * Setup uncaught exception handler
	 */
	public Thread.UncaughtExceptionHandler getexceptionHandler() {
		return new Thread.UncaughtExceptionHandler() {
					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						//Log all the things
						LOG.log( Level.SEVERE, "Request handling thread died with " + e.toString() + "\n" ) ;
						for( StackTraceElement line : e.getStackTrace() ) {
							LOG.log( Level.SEVERE, line.toString() ) ;
						}
					}
					
				} ;
	}
	
	/*
	 * Attempt to open the socket
	 */
	public boolean openSocket() {
		try {
			socket = new ServerSocket( port ) ;
			LOG.log( Level.INFO, String.format( "Server opened socket on port %d" , port ) ) ;
			return true ;
		} catch( IOException e ) {
			LOG.log( Level.SEVERE, String.format( "Server could not open socket on port %d" , port ) ) ;
			return false ;
		}
	}
	
	/*
	 * Attempt to close the socket
	 */
	public boolean closeSocket() {
		try {
			socket.close() ;
			LOG.log( Level.INFO, String.format( "closed socket on port %d", port ) ) ;
			return true ;
		} catch( IOException e ) {
			LOG.log( Level.SEVERE, String.format( "unable to close socket on port %d", port ) ) ;
			return false ;
		}
	}

	/*
	 * Returns a JSON formatted error message
	 */
//	private String errorResponseJSON( String errorMessage ) {
//		return String.format( "{\"error\":\"%s\"}" , errorMessage ) ;
//	}

	/*
	 * Start the response handling loop
	 */
	public void handleRequests() 
			throws InterruptedException {
		
		Socket connection = null ;
		String ip ;
		Request request ;
		Response response ;
		
		while( true ) {
			
			//Close the connection, if it is open
			try {
				connection.close() ;
			} catch( IOException | NullPointerException e ) {
				//Don't really mind if this fails, just keep going
			}
			
			//Exit the loop if the thread is interrupted
			if( Thread.interrupted() ) {
				throw new InterruptedException() ;
			}
			
			try {
				
				//Wait for a connection
				connection = socket.accept() ;
				//Grab ip address first for logging 
				ip = connection.getInetAddress().getHostAddress() ;
				//Create a new request
				request = new Request() ;
				if( !request.fromSocket( connection ) ) {
					//Fundamentally bad request
					LOG.log( Level.INFO, String.format( "%s : Received bad request", ip ) ) ;
					//sendBadRequestError( connection, "{\"error\":\"bad request\"}" ) ;
					continue ;
				}

			} catch (IOException e) {
				//Try again
				continue ;
			}
			
			LOG.log( Level.FINE, String.format( "%s : Received request", ip ) ) ;
			
			response = router.route( request ) ;
			if( response.send( connection ) ) {
				LOG.log( Level.FINE, String.format( "%s : Successfully responded", ip ) ) ;
				continue ;
			}

		} //End of main while loop
		
	}
	
	//Main run method
	@Override
	public void run() {
		LOG.log( Level.INFO , "Request handling loop started" ) ;
		try {
			handleRequests() ;
		} catch( InterruptedException e ) {
			LOG.log( Level.INFO , "Server thread terminated" ) ;
			//pass - we don't have to do anything, just stop the thread
		}
	}

}
