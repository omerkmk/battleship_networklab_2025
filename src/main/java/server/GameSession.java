package server;

import common.*;
import model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * Tek bir oyun turunu oynatıp, oyunculardan rematch isteğini dönen session.
 */
public class GameSession {
    private final Socket sock0, sock1;
    private final ObjectOutputStream out0, out1;
    private final ObjectInputStream in0, in1;

    public GameSession(Socket sock0, Socket sock1) throws IOException {
        this.sock0 = sock0;
        this.sock1 = sock1;
        this.out0  = new ObjectOutputStream(sock0.getOutputStream());
        this.in0   = new ObjectInputStream(sock0.getInputStream());
        this.out1  = new ObjectOutputStream(sock1.getOutputStream());
        this.in1   = new ObjectInputStream(sock1.getInputStream());
    }

    /**
     * Oyun akışını bir kez çalıştırır ve rematch isteğini kontrol eder.
     * @return true ise her iki oyuncu da rematch istedi.
     */
    public boolean playOnceAndCheckRematch() {
        GameState state = new GameState();
        try {
            // Handshake ve eşleşme bildirimi
            out0.writeObject("WELCOME"); out0.flush();
            out1.writeObject("WELCOME"); out1.flush();
            out0.writeObject(new MatchFoundMessage(0)); out0.flush();
            out1.writeObject(new MatchFoundMessage(1)); out1.flush();

            // Gemi yerleşimi ve hazır olma
            CountDownLatch readyLatch = new CountDownLatch(2);
            startInitThread(0, in0, out0, state, readyLatch);
            startInitThread(1, in1, out1, state, readyLatch);
            readyLatch.await();

            // Savaş fazını oynat
            playBattle(state);

            // Rematch isteklerini oku
            boolean first  = in0.readObject() instanceof RematchRequest;
            boolean second = in1.readObject() instanceof RematchRequest;

            return first && second;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void startInitThread(int player,
                                 ObjectInputStream in,
                                 ObjectOutputStream out,
                                 GameState state,
                                 CountDownLatch latch) {
        new Thread(() -> {
            try {
                while (true) {
                    Object msg = in.readObject();
                    if (msg instanceof PlaceShipRequest req) {
                        boolean ok = state.placeShip(req.getPlayer(), req.getShip());
                        out.writeObject(new PlaceShipResponse(ok)); out.flush();
                    } else if (msg instanceof ReadyRequest) {
                        latch.countDown();
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void playBattle(GameState state) throws IOException, ClassNotFoundException {
        ObjectInputStream[]  ins  = {in0, in1};
        ObjectOutputStream[] outs = {out0, out1};

        // İlk TurnMessage
        outs[0].writeObject(new TurnMessage(true));  outs[0].flush();
        outs[1].writeObject(new TurnMessage(false)); outs[1].flush();

        while (true) {
            int attacker = state.getCurrentPlayer();
            int defender = 1 - attacker;

            Object msg = ins[attacker].readObject();
            if (!(msg instanceof FireRequest freq)) continue;

            Board.Cell result = state.fire(freq.getPosition());
            FireResponse fresp = new FireResponse(freq.getPosition(), result);

            // Tüm katılımcılara sonucu bildir
            for (ObjectOutputStream o : outs) {
                o.writeObject(fresp); o.flush();
            }

            if (state.isGameOver()) {
                GameOverMessage gom = new GameOverMessage(state.getWinner());
                for (ObjectOutputStream o : outs) {
                    o.writeObject(gom); o.flush();
                }
                break;
            }

            // Yeni TurnMessage
            boolean hit = (result == Board.Cell.HIT);
            outs[attacker].writeObject(new TurnMessage(hit));  outs[attacker].flush();
            outs[defender].writeObject(new TurnMessage(!hit)); outs[defender].flush();
        }
    }
}
