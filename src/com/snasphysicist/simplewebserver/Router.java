
package com.snasphysicist.simplewebserver;

import java.util.Hashtable;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;

public class Router {
	
	private final static Logger LOG = Logger.getLogger(
		Logger.class.getName()
	);
	
	Hashtable<String,Function<Request, Response>> routes;
	
	public Router() {
		routes = new Hashtable<String, Function<Request,Response>>();
	}

	public void addRoute(
		String uri, 
		Function<Request, Response> handler
	) {
		routes.put(
			uri, 
			handler
		);
	}
	
	public void addStaticAsset(
		String uri,
		String title, 
		URL filePath
	) {	
		Function<Request, Response> staticAssetHandler = new Function<Request, Response>() {
			@Override
			public Response apply(
				Request rawRequest
			) {
				Response response;
				switch(
					StaticTextResponse.guessContentType(
						filePath
					)
				) {
					case APPLICATIONJAVASCRIPT:
					case TEXTCSS:
					case TEXTHTML:
					case APPLICATIONJSON: {
						response = new StaticTextResponse( 
							Protocol.HTTP10, 
							200, 
							title, 
							filePath 
						);
						break;
					}
					case IMAGEBMP:
					case IMAGEJPEG:
					case IMAGEPNG:
					case APPLICATIONOCTETSTREAM: 
					default: {
						response = new StaticFileResponse(
							Protocol.HTTP10,
							200, 
							title, 
							filePath 
						);
					}
				}
				return response;
			}
		};
				
		this.addRoute(
			uri, 
			staticAssetHandler
		);		
	}
	
	public void setErrorHandler(
		int statusCode, 
		Function<Request, Response> handler
	) {
		addRoute(
			String.format(
				"/%d.html", 
				statusCode
			),
			handler
		);
	}
	
	private Response defaultResponse() {
		return new TextResponse(
			Protocol.HTTP10, 
			500, 					 
			"Internal Server Error",
			"Error 500: Internal Server Error"
		);
	}
	
	/*
	 * Main function
	 * Takes a request, reads the uri,
	 * applies a function to map the request
	 * to a response and returns the response object
	 */
	public Response route(
		Request request
	) {
		// Get the uri, try to find a handler based on this
		Function<Request, Response> handler = routes.get(
			request.getUri()
		);
		if(handler == null) {
			// Attempt to get response generator for 404 page 
			handler = routes.get(
				"/404.html"
			);
			if(handler == null) {
				// No route defined for uri or for 404 page, return default
				LOG.log(
					Level.WARNING, 
					"No 404 route defined, returning default response"
				);
				return defaultResponse();
			} else {
				// No route defined for uri, return 404
				LOG.log(
					Level.INFO,
					String.format(
						"No route found for %s, returning 404 response",
						request.getUri()
					)
				);
				return handler.apply(
					request
				);
			}
		} else {
			// Route defined for uri, generate & return appropriate response
			LOG.log(
				Level.INFO, 
				String.format(
					"Route found for %s",
					request.getUri()
				)
			);
			return handler.apply(
				request
			);
		}
	}
	
}
