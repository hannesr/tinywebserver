package com.webs.graub.tinywebserver;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.junit.*;

public class GetTest {


	public static final int PORT = 27888;
	static Library mDummyLibrary = new MyLibrary();
	static Server mServer;


	/**
	 * Starts the server - prerequisite for the tests
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@BeforeClass
	public static void startServer() throws IOException, InterruptedException {
		mServer = new Server(mDummyLibrary, PORT);
		mServer.start();
		Thread.sleep(1000);
		assertTrue(mServer.isAlive());
	}

	/**
	 * Terminate the server after all tests run
	 * @throws InterruptedException 
	 */
	@AfterClass
	public static void stopServer() throws InterruptedException {
		mServer.stopServer();
		Thread.sleep(1000);
		assertFalse(mServer.isAlive());
	}

	/**
	 * Test traditional GET request
	 * @throws IOException 
	 */
	@Test
	public void testGetNormalPage() throws IOException {
		String req = "GET /normal HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		String content = new String(Util.readContent(in, headers));
		assertTrue(resp.contains("200 OK"));
		assertTrue(resp.contains("HTTP/1.1"));
		assertTrue(headers.containsKey("Server")); // <-- server has a name
		assertTrue(headers.containsKey("Content-Length")); // Content length is given
		assertTrue(headers.get("Date").contains("GMT")); // <-- server time is in GMT
		assertTrue(content.contains(MyLibrary.TEXT_DATA));
	}

	/**
	 * Test GET request, where chunked encoding is used for the content
	 * @throws IOException 
	 */
	@Test
	public void testGetChunkedPage() throws IOException {
		String req = "GET /chunked HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		String content = new String(Util.readContent(in, headers));
		assertTrue(resp.contains("200 OK"));
		assertFalse(headers.containsKey("Content-Length")); // Content length is NOT given
		assertTrue(content.contains(MyLibrary.TEXT_DATA));
	}

	/**
	 * Test GET request, where chunked encoding is used for the content
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Test
	public void testGetBigChunkedBinaryPage() throws IOException, ClassNotFoundException {
		String req = "GET /object HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		byte[] content = Util.readContent(in, headers);
		assertTrue(resp.contains("200 OK"));
		assertFalse(headers.containsKey("Content-Length")); // Content length is NOT given
		assertEquals(content.length, MyLibrary.BYTE_DATA.length);
		for(int i=0;i<Math.min(content.length, MyLibrary.BYTE_DATA.length);++i) {
			if (content[i] != MyLibrary.BYTE_DATA[i]) {
				fail("Data["+i+"] is different: original="+MyLibrary.BYTE_DATA[i]+ " received="+content[i]);
			}
		}
	}

	/**
	 * The webserver only supports HTTP 1.1, HTTP 1.0 requests bounce
	 * @throws IOException 
	 */
	@Test
	public void testHttp10Request() throws IOException {
		String req = "GET /normal HTTP/1.0\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		String content = new String(Util.readContent(in, headers));
		assertTrue(resp.contains("200 OK"));
		assertTrue(resp.contains("HTTP/1.0")); // <-- test that response is also in 1.0 mode
		assertTrue(content.contains(MyLibrary.TEXT_DATA));
	}

	/**
	 * Test that absolute URL is interprerted similarly than relative URL
	 * (actually this tests more the Java URL class)
	 * @throws IOException 
	 */
	@Test
	public void testAbsoluteUrl() throws IOException {
		String req = "GET http://127.0.0.1:27888/normal HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		new String(Util.readContent(in, headers));
		assertTrue(resp.contains("200 OK"));
	}


	/**
	 * Test a HTTP conversation with many requests using the same socket.
	 * The socket should not be disconnected, as there is no "Connection: close"
	 * header.
	 * @throws IOException 
	 */
	@Test
	public void testMultipleGetsOnSameConnection() throws UnknownHostException, IOException {
		String req = "GET / HTTP/1.1\nHost: 127.0.0.1\n\n";
		String req2 = "GET / HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		for(int i=0;i<5;++i) {
			writer.write(i<4 ? req : req2);
			writer.flush();
			String resp = Util.readLine(in);
			HashMap<String,String> headers = new HashMap<String,String>();
			Util.readHeaders(in, headers);
			new String(Util.readContent(in, headers));
			assertTrue(resp.contains("200 OK"));
		}
	}



}
