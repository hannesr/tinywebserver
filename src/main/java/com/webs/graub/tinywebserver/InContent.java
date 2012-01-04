package com.webs.graub.tinywebserver;

import java.io.IOException;
import java.io.InputStream;

public interface InContent extends Content{

	/**
	 * Requests the content to be read in from given stream.
	 * This method is called on POST and PUT requests, but only if
	 * direction of this content is IN or IN_OUT.
	 * NOTE: read the data AS IT IS, until EOF. The underlying stream
	 * will take care of all necessary decodings.
	 */
	void in(InputStream in) throws IOException;

}
