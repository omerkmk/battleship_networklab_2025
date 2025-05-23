package server;

import common.*;
import model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * GameSession manages a full game round between two players,
 * from handshake to battle phase and rematch decision.
 */
public class GameSession {

    private final Socket sock0, sock1;
    private final ObjectOutputStream out0, out1;
    private final ObjectInputStream in0, in1;

    /**
     * Initializes the session with two connected client sockets.
     */
    public GameSession(Socket sock0, Socket sock1) throws IOException {
        this.sock0 = sock0;
        this.sock1 = sock1;
        this.out0  = new ObjectOutputStream(sock0.getOutputStream());
        this.in0   = new ObjectInputStream(sock0.getInputStream());
        this.out1  = new ObjectOutputStream(sock1.getOutputStream());
        this.in1   = new ObjectInputStream(sock1.getInputStream());
    }

    /**
     * Runs the full game session once and returns whether both players agreed to rematch.
     * @return true if both players requested a rematch
     */
    public boolean playOnceAndCheckRematch() {
        GameState state = new GameState();

        try {
            // 1. Handshake + MatchFound
            out0.writeObject("WELCOME"); out0.flush();
            out1.writeObject("WELCOME"); out1.flush();
            out0.writeObject(new MatchFoundMessage(0)); out0.flush();
            out1.writeObject(new MatchFoundMessage(1)); out1.flush();

            // 2. Ship placement & Ready phase
            CountDownLatch readyLatch = new CountDownLatch(2);
            startInitThread(0, in0, out0, state, readyLatch);
            startInitThread(1, in1, out1, state, readyLatch);
            readyLatch.await(); // wait for both players to send ReadyRequest

            // 3. Battle phase
            playBattle(state);

            // 4. Rematch check
            boolean firstRematch  = in0.readObject() instanceof RematchRequest;
            boolean secondRematch = in1.readObject() instanceof RematchRequest;

            return firstRematch && secondRematch;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Handles ship placement and readiness confirmation in a separate thread for each player.
     */
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
                        latch.countDown(); // mark this player as ready
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Handles the battle phase turn-by-turn between the two players.
     */
    private void playBattle(GameState state) throws IOException, ClassNotFoundException {
        ObjectInputStream[]  ins  = {in0, in1};
        ObjectOutputStream[] outs = {out0, out1};

        // Initial TurnMessage: player 0 starts
        outs[0].writeObject(new TurnMessage(true));  outs[0].flush();
        outs[1].writeObject(new TurnMessage(false)); outs[1].flush();

        while (true) {
            int attacker = state.getCurrentPlayer();
            int defender = 1 - attacker;

            Object msg = ins[attacker].readObject();
            if (!(msg instanceof FireRequest freq)) continue;

            // Process the fire
            Board.Cell result = state.fire(freq.getPosition());
            FireResponse fresp = new FireResponse(freq.getPosition(), result);

            // Send FireResponse to both players
            for (ObjectOutputStream out : outs) {
                out.writeObject(fresp); out.flush();
            }

            // Check game over
            if (state.isGameOver()) {
                GameOverMessage gom = new GameOverMessage(state.getWinner());
                for (ObjectOutputStream out : outs) {
                    out.writeObject(gom); out.flush();
                }
                break;
            }

            // Send updated TurnMessage to both
            boolean hit = (result == Board.Cell.HIT);
            outs[attacker].writeObject(new TurnMessage(hit));  outs[attacker].flush();
            outs[defender].writeObject(new TurnMessage(!hit)); outs[defender].flush();
        }
    }
}
