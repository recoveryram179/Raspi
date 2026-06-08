package com.hawks.raspi.tcp;

import com.hawks.raspi.websocket.MessageWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TcpServerService {

    private static final int TCP_PORT = 5001;

    private final AtomicReference<ClientConnection> activeConnection = new AtomicReference<>();
    private ServerSocket serverSocket;

    @Autowired
    private MessageWebSocketHandler webSocketHandler;

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @PostConstruct
    public void startServer() {
        Thread serverThread = new Thread(this::runAcceptLoop, "tcp-accept-thread");
        serverThread.setDaemon(true);
        serverThread.start();
    }

    @PreDestroy
    public void stopServer() {
        closeActiveConnection();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("[TCP] Error closing server socket: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Accept loop
    // -------------------------------------------------------------------------

    private void runAcceptLoop() {
        try {
            serverSocket = new ServerSocket(TCP_PORT);
            System.out.println("[TCP] Server started on port " + TCP_PORT);

            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("[TCP] Client connected: " + socket.getInetAddress());

                // Replace the old connection — only one client at a time
                closeActiveConnection();

                ClientConnection connection;
                try {
                    connection = new ClientConnection(socket);
                } catch (IOException e) {
                    System.err.println("[TCP] Failed to open streams for client: " + e.getMessage());
                    socket.close();
                    continue; // keep the accept loop alive
                }
                activeConnection.set(connection);

                Thread clientThread = new Thread(
                        () -> handleClient(connection),
                        "tcp-client-" + socket.getInetAddress()
                );
                clientThread.setDaemon(true);
                clientThread.start();
            }

        } catch (IOException e) {
            if (serverSocket == null || serverSocket.isClosed()) {
                System.out.println("[TCP] Server socket closed, shutting down.");
            } else {
                System.err.println("[TCP] Accept loop error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Client handler
    // -------------------------------------------------------------------------

    private void handleClient(ClientConnection connection) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.socket.getInputStream()))) {

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("[TCP] Received: " + message);
                webSocketHandler.broadcast(message);
            }

        } catch (IOException e) {
            System.out.println("[TCP] Client disconnected: " + e.getMessage());
        } finally {
            connection.close();
            activeConnection.compareAndSet(connection, null);
        }
    }

    // -------------------------------------------------------------------------
    // Send
    // -------------------------------------------------------------------------

    public void sendMessage(String message) throws IOException {
        ClientConnection connection = activeConnection.get();

        if (connection == null || !connection.isOpen()) {
            throw new IOException("No TCP client connected.");
        }

        connection.send(message);
        System.out.println("[TCP] Sent: " + message);
    }

    public boolean isClientConnected() {
        ClientConnection connection = activeConnection.get();
        return connection != null && connection.isOpen();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void closeActiveConnection() {
        ClientConnection old = activeConnection.getAndSet(null);
        if (old != null) {
            old.close();
        }
    }

    // -------------------------------------------------------------------------
    // Inner class — wraps a socket and its writer together
    // -------------------------------------------------------------------------

    private static class ClientConnection {

        private final Socket socket;
        private final PrintWriter writer;

        ClientConnection(Socket socket) throws IOException {
            this.socket = socket;
            // autoFlush = true so every println() flushes immediately
            this.writer = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                    true
            );
        }

        void send(String message) {
            writer.println(message);
        }

        boolean isOpen() {
            return !socket.isClosed() && socket.isConnected();
        }

        void close() {
            // writer.close() flushes and closes the underlying socket stream,
            // which also closes the socket itself — no need to close socket separately.
            writer.close();
        }
    }
}