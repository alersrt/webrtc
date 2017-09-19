package org.student.webrtc.server;

import com.pusher.java_websocket.WebSocket;
import com.pusher.java_websocket.handshake.ClientHandshake;
import com.pusher.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class SignallingServer extends WebSocketServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SignallingServer.class);

	private static Set<WebSocket> webSockets = new HashSet<>();

	public SignallingServer() {
		super(new InetSocketAddress(50000));
	}

	@Override
	public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
		LOGGER.info("New connection: " + webSocket.getRemoteSocketAddress().hashCode());
	}

	@Override
	public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
		LOGGER.info("Connection %s is closed by reason %s", webSocket.getRemoteSocketAddress().hashCode(), reason);
	}

	@Override
	public void onError(WebSocket webSocket, Exception e) {
		LOGGER.info("Error %s", e.getMessage());
	}

	@Override
	public void onMessage(WebSocket webSocket, String message) {
		webSockets.add(webSocket);
		sendToAll(webSocket, message);
	}

	private void sendToAll(WebSocket webSocket, String message) {
		webSockets
				.stream()
				.filter(c -> !webSocket.equals(c))
				.forEach(c -> c.send(message));
	}
}
