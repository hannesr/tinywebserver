package com.webs.graub.tinywebserver;

/**
 * Runs server
 * @author Hannes R.
 *
 */
public class RunServerSilently {

	public static void main(String[] args) {
		Server s;
		try {
			s = new Server(new MyLibrary(), GetTest.PORT);
			s.start();
			Thread.sleep(200);
			s.join(); // run infinitely!
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
