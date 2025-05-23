package client;

import javax.swing.*;
import java.awt.*;

/**
 * StartScreen provides the initial UI where the user can enter
 * the server IP and port to connect to a Battleship game.
 */
public class StartScreen extends JFrame {
    private final JTextField hostField;
    private final JTextField portField;
    private final JButton findGameBtn;

    /**
     * Constructor to initialize the start screen UI.
     */
    public StartScreen() {
        super("Battleship – Start Screen");

        // === Apply Nimbus Look & Feel for modern UI ===
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // === Main Panel Setup ===
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30)); // dark theme background

        // === Title ===
        JLabel titleLabel = new JLabel("Battleship");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // === Input Fields (host and port) ===
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setOpaque(false); // keep background transparent

        JLabel hostLabel = new JLabel("Server IP:");
        hostLabel.setForeground(Color.LIGHT_GRAY);
        hostLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        hostField = new JTextField("localhost"); // default value
        hostField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(Color.LIGHT_GRAY);
        portLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        portField = new JTextField("12345"); // default port
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add components to input panel
        inputPanel.add(hostLabel);
        inputPanel.add(hostField);
        inputPanel.add(portLabel);
        inputPanel.add(portField);
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // === Find Game Button ===
        findGameBtn = new JButton("Find Game");
        findGameBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        findGameBtn.setBackground(new Color(70, 130, 180)); // steel blue
        findGameBtn.setForeground(Color.WHITE);
        findGameBtn.setFocusPainted(false);
        findGameBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Button container for alignment
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(findGameBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // === Button Action ===
        findGameBtn.addActionListener(e -> {
            dispose(); // close current screen
            SwingUtilities.invokeLater(() -> 
                new LobbyUI(hostField.getText().trim(), portField.getText().trim())
            );
        });

        // === Final Frame Setup ===
        setContentPane(mainPanel);
        setSize(450, 300);
        setResizable(false);
        setLocationRelativeTo(null); // center on screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Main method to launch the start screen.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartScreen::new);
    }
}
