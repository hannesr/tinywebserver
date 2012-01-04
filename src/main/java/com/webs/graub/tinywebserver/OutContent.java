package com.webs.graub.tinywebserver;
import java.io.IOException;
import java.io.OutputStream;

import com.webs.graub.tinywebserver.Content;


public interface OutContent extends Content {

	/**
	 * mimetype for this content
	 */
	public String getMimetype();

	/** 
	 * data size of this content
	 * return zero if you don't know (will use chunked encoding)
	 */
	public int getDataSize();

	/**
	 * requests the content to be written out the given stream.
	 * This method is called on GET and HEAD requests, but only if
	 * direction of this content is OUT or IN_OUT.
	 * NOTE: write the data AS IT IS. The underlying stream will take
	 * care of all necessary encodings.
	 */
	void out(OutputStream stream) throws IOException;

}
