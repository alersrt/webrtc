package org.student.webrtc.server;

import com.pusher.java_websocket.WebSocket;
import com.pusher.java_websocket.handshake.ClientHandshake;
import com.pusher.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;

public class SignallingServer extends WebSocketServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(SignallingServer.class);

	private static Map<Long, Set<WebSocket>> rooms = new HashMap<>();

	private long myroom;

	public SignallingServer() {
		super(new InetSocketAddress(30001));
	}

	@Override
	public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
		LOGGER.info("New connection: %s hash %s", webSocket.getRemoteSocketAddress(), webSocket.getRemoteSocketAddress().hashCode());
	}

	@Override
	public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
		LOGGER.info("%s is closed: by reason %s", webSocket.getRemoteSocketAddress(), reason);
	}

	@Override
	public void onError(WebSocket webSocket, Exception e) {
		LOGGER.info("Error: %s", e.getMessage());
	}

	@Override
	public void onMessage(WebSocket webSocket, String message) {
		Set<WebSocket> s;
		try {
			JSONObject obj = new JSONObject(message);
			String msgtype = obj.getString("type");
			switch (msgtype) {
				case "GETROOM":
					myroom = generateRoomNumber();
					s = new HashSet<>();
					s.add(webSocket);
					rooms.put(myroom, s);
					webSocket.send("{\"type\":\"GETROOM\",\"value\":" + myroom +
							"}");

					LOGGER.info("Generated new room: %s", myroom);
					break;
				case "ENTERROOM":
					myroom = obj.getInt("value");
					s = rooms.get(myroom);
					s.add(webSocket);
					rooms.put(myroom, s);

					LOGGER.info("New client entered room %s", myroom);
					break;
				default:
					sendToAll(webSocket, message);
					break;
			}
		} catch (JSONException e) {
			sendToAll(webSocket, message);
		};
	}

	private long generateRoomNumber() {
		return new Random(System.currentTimeMillis()).nextLong();
	}

	private void sendToAll(WebSocket webSocket, String message) {
		rooms.get(myroom)
				.stream()
				.filter(c -> !webSocket.equals(c))
				.forEach(c -> c.send(message));
	}
}
