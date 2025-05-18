package client;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                // Her pencereyi ayrı thread'de başlat
                new Thread(() -> {
                    try {
                        new GameClient("localhost", 12345, 0).start();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }).start();
                new Thread(() -> {
                    try {
                        new GameClient("localhost", 12345, 1).start();
                    } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }).start();
            } else {
                int myPlayer = Integer.parseInt(args[0]);
                new GameClient("localhost", 12345, myPlayer).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Usage: client.Main [<player 0|1>]");
        }
    }
}
