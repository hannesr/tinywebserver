package com.webs.graub.tinywebserver;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

import org.junit.*;

public class BadRequestsTest {


	public static final int PORT = 27888;
	static Library mDummyLibrary = new MyLibrary();
	static Server mServer;


	/**
	 * Starts the webserver - prerequisite for the tests
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
	 * Terminate the webserver after all tests run
	 * @throws InterruptedException 
	 */
	@AfterClass
	public static void stopServer() throws InterruptedException {
		mServer.stopServer();
		Thread.sleep(1000);
		assertFalse(mServer.isAlive());
	}

	/**
	 * Test a page that does not exist
	 * @throws IOException 
	 */
	@Test
	public void testGetNonExistentPage() throws IOException {
		String req =
			"GET /nonexistent HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("404 Not Found"));
	}

	/**
	 * HTTP 1.1 Request that does not contain the Host header should return "400 bad Request"
	 * @throws IOException 
	 */
	@Test
	public void testRequestWithoutHostHeader() throws IOException {
		String req =
			"GET /normal HTTP/1.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("400 Bad Request"));
	}

	/**
	 * Request that with unknown command"
	 * @throws IOException 
	 */
	@Test
	public void testUnknownHttpCommand() throws IOException {
		String req =
			"XYZ /normal HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("400 Bad Request"));
	}

	/**
	 * Bad request line (missing params)"
	 * @throws IOException 
	 */
	@Test
	public void testBadRequestLine() throws IOException {
		String req =
			"GET\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("400 Bad Request"));
	}

	/**
	 * Version that does not exist
	 * @throws IOException 
	 */
	@Test
	public void testUnknownHttpVersion() throws IOException {
		String req =
			"GET /normal HTTP/0.3\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("400 Bad Request"));
	}

	/**
	 * BadHeadersTest
	 * @throws IOException 
	 */
	@Test
	public void testBadHeaders1() throws IOException {
		String req =
			"GET /normal HTTP/0.3\nHost: 127.0.0.1\nBadHeader\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("400 Bad Request"));
	}

	/**
	 * BadHeadersTest
	 * @throws IOException 
	 */
	@Test
	public void testBadHeaders2() throws IOException {
		String req =
			"GET /normal HTTP/0.3\nHost: 127.0.0.1\nkey:\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("400 Bad Request"));
	}

	/**
	 * BadHeadersTest
	 * @throws IOException 
	 */
	@Test
	public void testBadHeaders3() throws IOException {
		String req =
			"GET /normal HTTP/0.3\nHost: 127.0.0.1\n:value\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("400 Bad Request"));
	}

	/**
	 * Request that with unsupported command"
	 * @throws IOException 
	 */
	@Test
	public void testUnsupportedHttpCommand() throws IOException {
		String req =
			"DELETE /normal HTTP/1.1\nHost: 127.0.0.1\nConnection: close\n\n";
		Socket s = new Socket(InetAddress.getLocalHost(), PORT);
		OutputStreamWriter writer = new OutputStreamWriter(s.getOutputStream());
		writer.write(req);
		writer.flush();
		BufferedInputStream in = new BufferedInputStream(s.getInputStream());
		String resp = Util.readLine(in);
		HashMap<String,String> headers = new HashMap<String,String>();
		Util.readHeaders(in, headers);
		assertTrue(resp.contains("501 Not Implemented"));
	}


}
