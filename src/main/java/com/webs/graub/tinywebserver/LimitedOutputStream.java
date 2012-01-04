package com.webs.graub.tinywebserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class LimitedOutputStream extends FilterOutputStream {

	int mLimit;

	public LimitedOutputStream(OutputStream out, int limit) {
		super(out);
		this.mLimit = limit;
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		int bytesToWrite = Math.min(len, Math.max(mLimit,0));
		out.write(b,off,bytesToWrite);
		mLimit -= len;
		if (mLimit < 0) {
			System.out.println("WARNING: LimitedOutputStream is discarding "+
					(len-bytesToWrite)+" extra bytes");
		}
	}

	@Override
	public void write(int b) throws IOException {
		if (mLimit>0) out.write(b);
		mLimit -= 1;
		if (mLimit < 0) {
			System.out.println("WARNING: LimitedOutputStream is discarding "+
					1+" extra bytes");
		}
	}

	/**
	 * This method can be called
	 */
	public void complete() throws IOException {
		if (mLimit > 0) {
			System.out.println("WARNING: LimitedOutputStream is writing "+
					mLimit+" dummy bytes");
			byte[] dummy = new byte[mLimit];
			Arrays.fill(dummy, (byte)0);
			write(dummy,0,dummy.length);
			assert(mLimit==0);
		}
	}


}
