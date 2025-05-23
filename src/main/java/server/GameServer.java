package server;

import common.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

/**
 * GameServer listens for client connections, pairs them,
 * and creates GameSession instances to handle gameplay between pairs.
 */
public class GameServer {

    private static final int PORT = 12345; // Server port for incoming connections

    /**
     * Entry point: starts the server.
     */
    public static void main(String[] args) {
        new GameServer().start();
    }

    /**
     * Starts the server, listens for connections, and manages matchmaking.
     */
    public void start() {
        Queue<Socket> lobby = new LinkedList<>(); // Waiting lobby for unmatched clients

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket sock = server.accept();
                System.out.println("Client connected: " + sock.getRemoteSocketAddress());

                lobby.add(sock); // Add to waiting lobby

                if (lobby.size() >= 2) {
                    // Take two clients and start a game session
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

    /**
     * Starts a new thread for a pair of matched players.
     * Inside the thread, a GameSession is started and managed.
     *
     * @param p0 socket for player 0
     * @param p1 socket for player 1
     */
    private void handleMatch(Socket p0, Socket p1) {
        new Thread(() -> {
            try {
                GameSession session = new GameSession(p0, p1);
                boolean rematch;

                do {
                    rematch = session.playOnceAndCheckRematch(); // play game and check if both players want rematch
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
        }, "GameSession-" + p0.getRemoteSocketAddress() + "-vs-" + p1.getRemoteSocketAddress()).start();
    }

    /**
     * Closes the socket quietly without throwing exceptions.
     * @param socket the socket to close
     */
    private void closeQuietly(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}
