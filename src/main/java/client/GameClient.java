package client;

import common.*;
import model.Position;
import model.Ship;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * GameClient handles handshake, game loop, and rematch cycles.
 */
public class GameClient {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private int myPlayer;      // assigned after MatchFound
    private final LobbyUI lobbyUi;
    private Object ui;         // GameUI or BattleUI reference

    public GameClient(String host, int port, LobbyUI lobbyUi) throws IOException {
        this.socket = new Socket(host, port);
        this.out    = new ObjectOutputStream(socket.getOutputStream());
        this.in     = new ObjectInputStream(socket.getInputStream());
        this.lobbyUi = lobbyUi;
        this.ui      = lobbyUi;
    }

    public void setUI(Object ui) {
        this.ui = ui;
    }

    public void start() {
        try {
            boolean rematch;
            do {
                // A) Handshake & MatchFound
                Object msg;
                while ((msg = in.readObject()) != null) {
                    if (msg instanceof String str && "WELCOME".equals(str)) {
                        System.out.println("Handshake: " + str);
                    } else if (msg instanceof MatchFoundMessage mfm) {
                        myPlayer = mfm.getPlayerId();
                        System.out.println("MatchFound: player=" + myPlayer);
                        SwingUtilities.invokeLater(() -> {
                            // Dispose previous UI before starting rematch
                            if (ui instanceof JFrame) {
                                ((JFrame) ui).dispose();
                            }
                            lobbyUi.onMatchFound(myPlayer, this);
                        });
                        break;
                    } else {
                        System.err.println("Unexpected handshake message: " + msg);
                    }
                }

                // B) Game loop: Placement, Battle, GameOver
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
                        break; // end of this game cycle
                    } else {
                        System.err.println("Unexpected game loop message: " + msg);
                    }
                }

                // C) Detect new WELCOME for rematch
                msg = in.readObject();
                if (msg instanceof String str2 && "WELCOME".equals(str2)) {
                    System.out.println("Rematch starting...");
                    rematch = true;
                } else {
                    rematch = false;
                }

            } while (rematch);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    // --- Sending methods ---
    public void sendPlaceShip(Ship s) {
        try {
            out.writeObject(new PlaceShipRequest(myPlayer, s));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendReady() {
        try {
            out.writeObject(new ReadyRequest(myPlayer));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFire(Position p) {
        try {
            out.writeObject(new FireRequest(p));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRematchRequest() {
        try {
            out.writeObject(new RematchRequest(myPlayer));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
