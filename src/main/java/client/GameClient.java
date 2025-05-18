// src/client/GameClient.java
package client;

import common.*;
import model.*;

import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class GameClient {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final int myPlayer;
    private Object ui;  // önce GameUI, sonra BattleUI referansı buraya gelecek

    public GameClient(String host, int port, int myPlayer) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.myPlayer = myPlayer;
    }

    /**
     * Yerleştirme UI → BattleUI geçişi için yeni UI referansını saklar
     */
    public void setUI(Object ui) {
        this.ui = ui;
    }

    // src/client/GameClient.java
    // In src/client/GameClient.java

public void start() {
    System.out.println("Client " + myPlayer + " started.");

    // 1) Handshake: sunucudan "WELCOME" oku
    try {
        Object hs = in.readObject();
        System.out.println("Client " + myPlayer + " handshake: " + hs);
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }

    // 2) Yerleştirme UI'sini yarat ve referansı sakla
    try {
        SwingUtilities.invokeAndWait(() -> {
            GameUI gui = new GameUI(this, myPlayer);
            setUI(gui);
        });
    } catch (Exception e) {
        e.printStackTrace();
        return;
    }

    // 3) Mesaj dinleme döngüsü
    try {
        while (true) {
            Object msg = in.readObject();

            if (msg instanceof PlaceShipResponse) {
                // Gemi yerleştirme sonucu
                ((GameUI) ui).handlePlaceShipResponse((PlaceShipResponse) msg);

            } else if (msg instanceof TurnMessage) {
                // Sıra mesajı: henüz yerleşme ekranındaysak GameUI, savaştaysak BattleUI
                if (ui instanceof GameUI) {
                    ((GameUI) ui).handleTurnMessage((TurnMessage) msg);
                } else {
                    ((BattleUI) ui).handleTurnMessage((TurnMessage) msg);
                }

            } else if (msg instanceof FireResponse) {
                // Atış sonucu sadece BattleUI tarafından işlenir
                ((BattleUI) ui).handleFireResponse((FireResponse) msg);

            } else if (msg instanceof GameOverMessage) {
                // Oyun bitti
                ((BattleUI) ui).handleGameOverMessage((GameOverMessage) msg);
                break;

            } else {
                System.err.println("Unknown message type: " + msg.getClass().getSimpleName());
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}


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
}
