
# Simple Web Server

A basic web server written using only native Java libraries.

This was an much a learning project for me as something intended to be useful for others, but it can still serve as a reasonably straightforward and reasonably 'lightweight' (to the extend that Java can be) server to serve simple HTTP requests.

It is used in (and, really, written for) my [Chinese Character Practice Sheets](https://github.com/snasphysicist/ChineseCharacterPracticeSheets) tool.

## Capabilities/Limitations

These capabilities are currently built into the library. I have tried to make it easily extensible, see below for more information.

* HTTP 1.0 responses
* Each request is handled in its own thread
* Can easily serve static files
* Custom port easily selectable
* Logging through native Java library
* Map arbitrary URLs to arbitrary resources
* Routing in a similar fashion to most web server frameworks

It is not, and it is not intended to be, a fully compliant HTTP server. The idea is to have 'enough' functionality to achieve what's needed, rather than to be all things to all humans.

## Extending Functionality

Ultimately everything a HTTP server does is part of a request or a response. To add some extra of custom behaviour to this server the <strong>Request</strong> and <strong>Response</strong> classes can be extended.

### Request

Each request actually runs as its own thread. The class implements <em>runnable</em> and is spun off into a new thread & ``run()`` called when a connection is established to a web server.

The request class is responsible for reading the HTTP request from the input stream and

* Checking the request is a valid HTTP request
* Establish the method and protocol
* Checking the method is supported by the web server
* Parse the headers
* Grab the source IP address of the connection (for logging)

Finally after reading everything in and all if all checks are passed, the request passes itself to the <strong>Router</strong> where it is passed to the correct function to return an appropriate response.

The response object is then returned to the request (which still has a reference to the original connection) so that the response data can be sent.

Since the server currently handles only HTTP 1.0, the connection is immediately closed.

Usually it is not necessary to subclass Request unless you are making major changes to the way connections are handled or the protocols handled, for example, implementing HTTP 1.1 or HTTP 2.0.

### Router

It is worth taking a detour via the Router class, since this provides the connection between the Request and the Response.

The Router is basically a wrapper class for a HashTable that maps uris to handler functions, providing in addition some convenient methods for adding certain common routes (such as static files) and for providing a reference to the correct function when given an uri. In addition, some error handling is included here (particularly, 404 errors).

When the server is set up, usually before the port is opened, a series of handlers are associated with uris using the method ``addRoute( uri , handler )``.

The handler is an Object of type <strong>Function<Request,Response></strong> from the package java.util.function. All logic that occurs in response to the content of a request and processing in the construction of a response occurs in ``apply()`` method of that Function object, and the appropriate Response object should also be created and returned by the function.

A Request object will call the ``route( request )`` method in order to be handled, which is where the 404 error handling occurs.

### Response

It is more natural to subclass the Response class to allow your web server to respond in a custom manner.

This might be used to respond with a content type not yet implemented, with data from a particular stream or it can have any other content injected into it based on the processing done in the handler function.

When the Response object is returned to the Request object, the Request object will call the ``send( connection )`` method, passing the connection to the client it has stored  (<strong>Socket</strong> object from java.net.Socket).

This method calls ``generateContent()`` which can be overridden when subclassed to insert any content desired.

Here you can first add any custom headers with ``addHeader(Header header)``. You should then always call ``generateTopMatter()`` which adds the protocol, status code & title to the response

Finally you can write any other data (the <em>body</em>) to the response. For text responses, this can simply be added to the ``content`` string which is streamed down the client connection in ``send(Socket connection)``. For sending data streams (such as file streams) it is currently necessary to override the ``send(Socket connection)`` method also to handle the streaming.

That's all there is to it!

## WebServer class

Were this meant to be used as a standalone web server, this would be where the ``main`` method lives. However, the idea is that this class will be used as a library in other projects, where a web server can be run essentially just be creating a WebServer object.

Hence this class deals with the setup of the server (setting the port, routing), creating the <strong>ServerSocket</strong> object to accept connections, the request handling loop that spins Requests off into threads and major 'server killing' error handling.

This all happens in the ``handleRequests()`` method. You'll notice that the server implements <strong>Runnable</strong> since it is intended to run as a separate thread in the main program - the ``run()`` method really just wraps ``handleRequests()``, with a little logging if the handling loop completely dies. If all works as intended, this should only be possible if the thread is interrupted, since a try catch construct in ``handleRequests()`` attempts to recover from any catastrophic IO issues arising from the ServerSocket.

Note that, although multithreaded request handling is implemented, the server currently doesn't keep track of the threads. This is probably not _super_ essential for a HTTP 1.0 server, since connections are killed after a single response is send and hence it's unlikely that connections will be held open for a very long time & start to build up. This does make the server vulnerable to a slow loris style attack, so probably a maximum thread count will be implemented in the future.

## Key enums

There are a couple of key enums worth mentioning, particular if you are extending the functionality of the server.

First a word on formatting - where special characters (such as / or .) appear in important strings (such as ``HTTP 1.0``, or ``application/json``), the convention used when setting enums is just to drop the special characters and cram all the numbers and letters together, e.g. ``HTTP 1.0`` -> ``HTTP10``, ``application/json`` -> ``APPLICATIONJSON``. 

The <strong>Method</strong> enum lists all methods recognised by the server currently. Check it - you'll see that I definitely wasn't joking about this not being a full HTTP server. You'll need to add an entry here before implementing handlers for different method types, else the Request object will refuse the handle the request.

You'll have to add entries to the <strong>Protocol</strong> enum if you want to implement HTTP 1.1, HTTP 2.0 or some other weird and wonderful protocol, for the same reason as above.

To support new content types, either as requests or responses, you'll have to add an entry to the <strong>ContentType</strong> enum.
