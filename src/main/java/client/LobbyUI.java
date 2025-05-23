package client;

import common.MatchFoundMessage;
import javax.swing.*;
import java.awt.*;

/**
 * LobbyUI class displays a waiting screen while searching for a match.
 * Connects to the server and launches the GameClient to start matchmaking.
 */
public class LobbyUI extends JFrame {
    private final String host;
    private final int port;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    /**
     * Constructs the lobby screen with host and port for matchmaking.
     */
    public LobbyUI(String host, String portStr) {
        super("Battleship – Waiting for Opponent");
        this.host = host;
        this.port = Integer.parseInt(portStr);

        // === 1) Set modern Look & Feel (Nimbus) ===
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus not available, fallback to default L&F
        }

        // === 2) Main content panel ===
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30)); // Dark background

        // === 3) Title label ===
        JLabel titleLabel = new JLabel("Searching for opponent...");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // === 4) Status label (shows host and port) ===
        statusLabel = new JLabel(host + ":" + port, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusLabel.setForeground(Color.LIGHT_GRAY);
        mainPanel.add(statusLabel, BorderLayout.CENTER);

        // === 5) Progress bar ===
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true); // spinning animation
        progressBar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        progressBar.setBackground(new Color(50, 50, 50));
        progressBar.setForeground(new Color(70, 130, 180)); // steel blue
        mainPanel.add(progressBar, BorderLayout.SOUTH);

        // === 6) Frame settings ===
        setContentPane(mainPanel);
        setSize(400, 200);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        // === 7) Start matchmaking ===
        startMatchmaking();
    }

    /**
     * Launches a background thread to initiate the GameClient and wait for match.
     */
    private void startMatchmaking() {
        new Thread(() -> {
            try {
                GameClient client = new GameClient(host, port, this);
                client.start();
            } catch (Exception ex) {
                // Show error dialog if connection fails
                SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this,
                        "Could not connect to server:\n" + ex.getMessage(),
                        "Connection Error",
                        JOptionPane.ERROR_MESSAGE)
                );
            }
        }, "Matchmaking-Thread").start();
    }

    /**
     * Callback when a match is successfully found.
     * Transitions to the ship placement screen (GameUI).
     */
    public void onMatchFound(int playerId, GameClient client) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(progressBar.getMaximum()); // visual cue

            dispose();  // Close lobby screen
            client.setUI(new GameUI(client, playerId)); // Launch ship placement UI
        });
    }
}
