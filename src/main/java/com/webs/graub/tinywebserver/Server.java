package com.webs.graub.tinywebserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Tiny web server.
 * 
 * @author Hannes R.
 */
public class Server extends Thread {

	// constants
	public static final int DEFAULT_PORT = 8080;

	// members
	ServerSocket mListenerSocket;
	Library mLibrary;
	boolean running = true;

	public Server(Library library) throws IOException {
		this(library, DEFAULT_PORT);
	}

	public Server(Library library, int port) throws IOException {
		this.mLibrary = library;
		this.mListenerSocket = new ServerSocket(port);
	}

	public void stopServer() {
		running = false;
		try {
			mListenerSocket.close();
		} catch (IOException e) {
			// do nothing
		}
	}

	@Override
	public void run() {
		while (running) {
			try {
				Socket s = mListenerSocket.accept();
				new Session(mLibrary, s).start();
			} catch (IOException ex) {
				if (running) {
					ex.printStackTrace();
					running = false;
				}
			}
		}
	}

}
