package com.webs.graub.tinywebserver;

import java.net.URI;

public interface Library {

	/**
	 * Prepares content from the library by URI. This is called on GET and HEAD
	 * requests. The Library implementation should check the uri.relativePath()
	 * and return a matching Content class that matches the request. The Content
	 * will be used to provide the message body to client.
	 * 
	 * If null is returned, status "404 Not Found" is transmitted to client.
	 * The Library implementation may also throw any other HttpStatus code
	 * like an exception, in which case it will be transmitted to the client
	 * as a HTTP error code.
	 */
	Content getContent(URI uri) throws HttpStatus;
	
}
