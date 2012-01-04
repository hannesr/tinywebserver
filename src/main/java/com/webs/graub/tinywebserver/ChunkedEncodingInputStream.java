package com.webs.graub.tinywebserver;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ChunkedEncodingInputStream is an InputStream that reads HTTP stream
 * where data is known to be in chunked encoding (data transmitted in
 * chunks, and preceding each chunk, size of the chunk is given). Zero
 * chunk size marks end of data. <p>
 *
 * The class is used as FilterInputStream - Create ChunkedEncodingInputStream
 * by giving the source InputStream as a parameter. Now you are ready to
 * read the data. The stream will give data until the last chunk, after which
 * the stream will indicate EOF (even if there would still be data in the
 * underlying stream) <p>
 * 
 * Due to the nature of chunked encoding, the class provides a small buffering
 * feature (the size of the chunk) but if you want additional buffering, you
 * can use this class together with BufferedInputStream. <p>
 * 
 * Example:
 * <pre>
 * BufferedReader reader = new BuffredReader(
 *     new ChunkedEncodingInputStream(
 *         new BufferedInputStream(socket.getInputStream())));
 * while(true) {
 *     String line = reader.readLine();
 *     if (line==null) break;
 *     System.out.println(line);
 * }
 * </pre>
 *  
 * @author Hannes R.
 */
public class ChunkedEncodingInputStream extends FilterInputStream {

	// members
	byte[] mChunk;
	int mPointer;
	enum State {INITIAL,ACTIVE,EOF,EOF_WILL_THROW};
	//boolean mEof = false;
	State mState = State.INITIAL;

	protected ChunkedEncodingInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		if (mState==State.INITIAL) readChunk();
		if (mState==State.EOF) return eof();
		if (mState==State.EOF_WILL_THROW)
			throw new EOFException("No more chunks");

		if (mPointer>=mChunk.length) readChunk();
		if (mState==State.EOF) return eof();
		return b2i(mChunk[mPointer++]);
	}

	@Override
	public int read(byte b[], int off, int len) throws IOException {
		if (mState==State.INITIAL) readChunk();
		if (mState==State.EOF) return eof();
		if (mState==State.EOF_WILL_THROW)
			throw new EOFException("No more chunks");

		int b_pointer = 0;
		while(true) {
			if (mPointer>=mChunk.length) readChunk();
			if (mState != State.ACTIVE) break;
			int length = Math.min(len, mChunk.length - mPointer);
			if (length==0) break;
			System.arraycopy(mChunk, mPointer, b, off+b_pointer, length);
			mPointer += length;
			b_pointer += length;
		}
		return b_pointer;
	}

	@Override
	public int available() throws IOException {
		if (mState==State.ACTIVE) {
			if (mPointer<mChunk.length) return mChunk.length-mPointer;
			else return in.available();
		}
		else return 0;
	}

	private int b2i(byte b) {
		return 0xFF & (int)b;
	}

	private int eof() {
		mState = State.EOF_WILL_THROW;
		return -1;
	}

	private void readChunk() throws IOException {
		// read hex number
		byte[] buf = new byte[32];
		int size = 0;
		while (true) {
			byte b = (byte)in.read();
			if (!isalnum(b)) break;
			buf[size++] = b;
		}
		// ignore rest of the line until newline char
		while(true) {
			int b = in.read();
			if (b<0 || (byte)b=='\n') break;
		}

		// decode this as chunk size
		int chunk_size = Integer.parseInt(new String(buf, 0, size).trim(), 16);
		if (chunk_size>0) {
			mState = State.ACTIVE;
			// read chunk according to chunk size
			mChunk = new byte[chunk_size];
			in.read(mChunk);
			// ignore rest of the line until newline char
			while(true) {
				int b = in.read();
				if (b<0 || (byte)b=='\n') break;
			}
			// set read position at beginning of chunk
			mPointer = 0;
		} else {
			mState = State.EOF;
			mChunk = null;
			mPointer = 0;
		}
	}


	// true if byte represents an alphanumeric ascii character
	boolean isalnum(byte b) {
		return (b>='0'&&b<='9')||(b>='a'&&b<='f')||(b>='A'&&b<='F');
	}

}


