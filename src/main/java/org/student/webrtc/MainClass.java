package org.student.webrtc;

import org.student.webrtc.server.SignallingServer;

public class MainClass {
	public static void main(String[] args) {
		SignallingServer server = new SignallingServer();
		server.start();
	}
}
