package server;

import common.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Lobby'de bekleyen iki client'i eşleştirip, aynı GameSession ile replay oynatan server.
 */
public class GameServer {

    private static final int PORT = 12345;

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        Queue<Socket> lobby = new LinkedList<>();

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket sock = server.accept();
                System.out.println("Client connected: " + sock.getRemoteSocketAddress());
                lobby.add(sock);

                if (lobby.size() >= 2) {
                    Socket p0 = lobby.poll();
                    Socket p1 = lobby.poll();
                    handleMatch(p0, p1);
                } else {
                    System.out.println("Waiting for another player... (" + lobby.size() + "/2)");
                }
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleMatch(Socket p0, Socket p1) {
        new Thread(() -> {
            try {
                GameSession session = new GameSession(p0, p1);
                boolean rematch;
                do {
                    rematch = session.playOnceAndCheckRematch();
                    if (rematch) {
                        System.out.println("GameServer: Both players requested rematch — restarting session.");
                    }
                } while (rematch);
            } catch (IOException ioe) {
                System.err.println("Failed to create or run GameSession: " + ioe.getMessage());
                ioe.printStackTrace();
            } finally {
                closeQuietly(p0);
                closeQuietly(p1);
                System.out.println("GameServer: Session ended, sockets closed.");
            }
        }, "GameSession-" + p0.getRemoteSocketAddress() + "-vs-" + p1.getRemoteSocketAddress())
        .start();
    }

    private void closeQuietly(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }
}
