package com.webs.graub.tinywebserver;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

/**
 * ChunkedEncodingOutputStream is a stream that converts any data into
 * HTTP chunked encoding transmission stream on the fly. Chunked transmission
 * is a HTTP standard where data is transmitted in chunks, and preceding each
 * chunk, size of the chunk is transmitted. End of data is marked with a chunk
 * of zero data size. <p>
 * 
 * The stream is used as a FilterOutputStream - by connecting it to another
 * OutputStream (possibly a socket OutputStream) and then you are ready to
 * write any data. Once all data has been written, you must terminate the
 * stream using transmitEof() method, for otherwise the receiving end expects
 * more data. <p>
 * 
 * Due to the nature of the encoding, The class provides a small buffering
 * feature automatically (the size of the chunk) but if you want additional
 * buffering, you can use the class together with BufferedOutputStream.
 * A chunk is automatically completed and transmitted on every flush(),
 * or when a pre-defined chunk maximum size is reached on buffer. <p>
 * 
 * Example:
 * <pre>
 * ChunkedEncodingOutputStream out = new ChunkedEncodingOutputStream(
 *    new BufferedOutputStream(socket.getOutputStream()));
 * for(byte b: data) {
 *     out.write(b);
 * }
 * out.transmitEof();
 * </pre>
 *
 * @author Hannes R.
 */
public class ChunkedEncodingOutputStream extends FilterOutputStream {

	// constants
	static final int CHUNK_SIZE = 512;

	// members
	byte[] mChunk;
	int mChunkDataSize;

	ChunkedEncodingOutputStream(OutputStream out) {
		super(out);
		mChunk = new byte[CHUNK_SIZE];
		mChunkDataSize = 0;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int offset = off;
		while(true) {
			mChunkDataSize = Math.min(CHUNK_SIZE, off+len-offset);
			if (mChunkDataSize==0) break;
			System.arraycopy(b, offset, mChunk, 0, mChunkDataSize);
			offset += mChunkDataSize;
			flush();
		}
	}

	@Override
	public void write(int b) throws IOException {
		mChunk[mChunkDataSize++] = (byte)b;
		if (mChunkDataSize>=CHUNK_SIZE) {
			flush();
		}
	}

	@Override
	public void flush() throws IOException {
		if (mChunkDataSize > 0) {
			String chunk_size_row = Integer.toHexString(mChunkDataSize);
			out.write(chunk_size_row.getBytes());
			out.write(Http.CRLF);
			out.write(mChunk, 0, mChunkDataSize);
			out.write(Http.CRLF);
			mChunkDataSize = 0;
			out.flush();
		}
	}

	/**
	 * This must be called when all write() methods have been called,
	 * so that the recipient understands that the last chunk has been
	 * received. This is a separate method (and not embedded in close)
	 * because user may want to send some footers or other data AFTER
	 * the data chunks, using the same underlying stream.
	 */
	public void complete() throws IOException {
		String chunk_size_row = Integer.toHexString(0);
		out.write(chunk_size_row.getBytes());
		out.write(Http.CRLF);
		out.write(Http.CRLF);
	}

}
