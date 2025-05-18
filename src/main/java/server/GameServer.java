package server;

import common.*;
import model.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class GameServer {
    private static final int PORT = 12345;
    private final GameState state = new GameState();
    private final boolean[] ready = new boolean[2];

    public static void main(String[] args) {
        new GameServer().start();
    }

    public void start() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            Socket sock0 = server.accept();
            System.out.println("Player 0 connected.");
            Socket sock1 = server.accept();
            System.out.println("Player 1 connected.");

            ObjectOutputStream out0 = new ObjectOutputStream(sock0.getOutputStream());
            ObjectInputStream  in0  = new ObjectInputStream(sock0.getInputStream());
            ObjectOutputStream out1 = new ObjectOutputStream(sock1.getOutputStream());
            ObjectInputStream  in1  = new ObjectInputStream(sock1.getInputStream());

            // Basit handshake
            out0.writeObject("WELCOME"); out0.flush();
            out1.writeObject("WELCOME"); out1.flush();

            new Thread(() -> handleClient(0, in0, out0, out1)).start();
            new Thread(() -> handleClient(1, in1, out1, out0)).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(int player,
                          ObjectInputStream in,
                          ObjectOutputStream out,
                          ObjectOutputStream oppOut) {
    try {
        while (true) {
            Object msg = in.readObject();

            if (msg instanceof PlaceShipRequest) {
                PlaceShipRequest req = (PlaceShipRequest) msg;
                boolean ok = state.placeShip(req.getPlayer(), req.getShip());
                out.writeObject(new PlaceShipResponse(ok));
                out.flush();

            } else if (msg instanceof ReadyRequest) {
                ready[player] = true;
                System.out.println("Player " + player + " is READY");
                // Her iki oyuncu da gemilerini yerleştirip hazırsa savaşı başlat
                if (state.getShipsPlaced(0) == GameRules.numShips()
                 && state.getShipsPlaced(1) == GameRules.numShips()
                 && ready[0] && ready[1]) {

                    // Başlangıç turunu player0 alacak şekilde gönder
                    out.writeObject(new TurnMessage(player == 0));
                    out.flush();
                    oppOut.writeObject(new TurnMessage(player == 1));
                    oppOut.flush();
                    System.out.println("Both ready → sending initial TurnMessage");
                }

            } else if (msg instanceof FireRequest) {
                FireRequest req = (FireRequest) msg;
                Position pos = req.getPosition();

                // 1) Atışı işle
                Board.Cell result = state.fire(pos);
                FireResponse resp = new FireResponse(pos, result);

                // 2) Her iki tarafa da FireResponse gönder
                out.writeObject(resp);
                out.flush();
                oppOut.writeObject(resp);
                oppOut.flush();

                // 3) Oyun bitti mi?
                if (state.isGameOver()) {
                    GameOverMessage gom = new GameOverMessage(state.getWinner());
                    out.writeObject(gom);
                    out.flush();
                    oppOut.writeObject(gom);
                    oppOut.flush();
                    break;
                }

                // 4) Sıra yönetimi: HIT ise saldıran kalır, MISS ise sıra karşı tarafa geçer
                if (result == Board.Cell.HIT) {
                    // Saldıran oyuncunun sırası devam etsin
                    out.writeObject(new TurnMessage(true));
                    out.flush();
                    oppOut.writeObject(new TurnMessage(false));
                    oppOut.flush();
                } else {
                    // Iskaladı, sıra diğerinde
                    out.writeObject(new TurnMessage(false));
                    out.flush();
                    oppOut.writeObject(new TurnMessage(true));
                    oppOut.flush();
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Error handling Player " + player + ": " + e.getMessage());
        e.printStackTrace();
    }
}


}
