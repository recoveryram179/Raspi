package com.hawks.raspi.websocket;

import com.hawks.raspi.tcp.TcpServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MessageWebSocketHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    // @Lazy breaks the circular dependency:
    //   WebSocketHandler -> TcpServerService -> WebSocketHandler
    @Autowired
    @Lazy
    private TcpServerService tcpServerService;

    // -------------------------------------------------------------------------
    // Connection lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        System.out.println("[WS] Client connected: " + session.getId()
                + " | total sessions: " + sessions.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        System.out.println("[WS] Client disconnected: " + session.getId()
                + " | status: " + status
                + " | remaining sessions: " + sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("[WS] Transport error on session " + session.getId()
                + ": " + exception.getMessage());
        sessions.remove(session);
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException e) {
                // Ignore — we're already cleaning up
            }
        }
    }

    // -------------------------------------------------------------------------
    // Messaging
    // -------------------------------------------------------------------------

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload().trim();

        if (payload.isEmpty()) {
            return;
        }

        System.out.println("[WS] Received from " + session.getId() + ": " + payload);

        try {
            tcpServerService.sendMessage(payload);
        } catch (IOException e) {
            System.err.println("[WS] Failed to forward message to TCP client: " + e.getMessage());
            sendError(session, "TCP client not connected: " + e.getMessage());
        }
    }

    /**
     * Broadcast a message to every open WebSocket session.
     */
    public void broadcast(String message) {
        TextMessage textMessage = new TextMessage(message);
        sessions.forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    System.err.println("[WS] Failed to send to session "
                            + session.getId() + ": " + e.getMessage());
                    sessions.remove(session);
                }
            } else {
                sessions.remove(session);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void sendError(WebSocketSession session, String error) {
        if (!session.isOpen()) return;
        // Escape backslashes and quotes so the error text can't break the JSON structure
        String safe = error.replace("\\", "\\\\").replace("\"", "\\\"");
        try {
            session.sendMessage(new TextMessage("{\"error\":\"" + safe + "\"}"));
        } catch (IOException e) {
            // Best-effort
        }
    }

    public int getSessionCount() {
        return sessions.size();
    }
}