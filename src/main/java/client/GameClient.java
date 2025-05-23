package client;

import common.*;
import model.Position;
import model.Ship;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * GameClient manages the client-side communication with the server.
 * Responsibilities:
 *  - Handles the initial handshake and match-making.
 *  - Processes game flow: ship placement, turns, fire, and game over.
 *  - Sends requests such as place ship, ready, fire, and rematch.
 */
public class GameClient {

    private final Socket socket;               // TCP socket for communication
    private final ObjectOutputStream out;      // Stream to send objects to the server
    private final ObjectInputStream in;        // Stream to receive objects from the server
    private int myPlayer;                      // This client's player ID
    private final LobbyUI lobbyUi;             // UI shown in the lobby phase
    private Object ui;                         // Active UI (GameUI or BattleUI)

    /**
     * Constructor for GameClient.
     * Initializes network connection and input/output streams.
     */
    public GameClient(String host, int port, LobbyUI lobbyUi) throws IOException {
        this.socket = new Socket(host, port);
        this.out    = new ObjectOutputStream(socket.getOutputStream());
        this.in     = new ObjectInputStream(socket.getInputStream());
        this.lobbyUi = lobbyUi;
        this.ui      = lobbyUi;
    }

    /**
     * Sets the current active UI (either GameUI or BattleUI).
     */
    public void setUI(Object ui) {
        this.ui = ui;
    }

    /**
     * Main entry point for the client logic.
     * Handles handshake, game loop, and rematch cycles.
     */
    public void start() {
        try {
            boolean rematch;
            do {
                // === A) Initial Handshake and Match Found Phase ===
                Object msg;
                while ((msg = in.readObject()) != null) {
                    if (msg instanceof String str && "WELCOME".equals(str)) {
                        System.out.println("Handshake: " + str);
                    } else if (msg instanceof MatchFoundMessage mfm) {
                        myPlayer = mfm.getPlayerId();
                        System.out.println("MatchFound: player=" + myPlayer);

                        // Update UI on EDT
                        SwingUtilities.invokeLater(() -> {
                            if (ui instanceof JFrame) {
                                ((JFrame) ui).dispose();  // Close previous UI if any
                            }
                            lobbyUi.onMatchFound(myPlayer, this);
                        });
                        break; // Proceed to game loop
                    } else {
                        System.err.println("Unexpected handshake message: " + msg);
                    }
                }

                // === B) Game Phase: Placement, Turns, and Game Over ===
                while ((msg = in.readObject()) != null) {
                    if (msg instanceof PlaceShipResponse psr) {
                        ((GameUI) ui).handlePlaceShipResponse(psr);
                    } else if (msg instanceof TurnMessage tm) {
                        if (ui instanceof GameUI) {
                            ((GameUI) ui).handleTurnMessage(tm);
                        } else {
                            ((BattleUI) ui).handleTurnMessage(tm);
                        }
                    } else if (msg instanceof FireResponse fr) {
                        ((BattleUI) ui).handleFireResponse(fr);
                    } else if (msg instanceof GameOverMessage gom) {
                        ((BattleUI) ui).handleGameOverMessage(gom);
                        break; // Game over, break out to check for rematch
                    } else {
                        System.err.println("Unexpected game loop message: " + msg);
                    }
                }

                // === C) Rematch Cycle Detection ===
                msg = in.readObject();
                if (msg instanceof String str2 && "WELCOME".equals(str2)) {
                    System.out.println("Rematch starting...");
                    rematch = true;
                } else {
                    rematch = false;
                }

            } while (rematch);  // Loop for rematches

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();  // Ensure socket is closed
            } catch (IOException ignored) {}
        }
    }

    // ====================
    // === Send Methods ===
    // ====================

    /**
     * Sends a ship placement request to the server.
     */
    public void sendPlaceShip(Ship s) {
        try {
            out.writeObject(new PlaceShipRequest(myPlayer, s));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a "ready to start battle" message to the server.
     */
    public void sendReady() {
        try {
            out.writeObject(new ReadyRequest(myPlayer));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a fire request (shot at a position) to the server.
     */
    public void sendFire(Position p) {
        try {
            out.writeObject(new FireRequest(p));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a rematch request to the server after the game ends.
     */
    public void sendRematchRequest() {
        try {
            out.writeObject(new RematchRequest(myPlayer));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
