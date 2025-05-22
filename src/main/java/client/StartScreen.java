package client;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JFrame {
    private final JTextField hostField;
    private final JTextField portField;
    private final JButton findGameBtn;

    public StartScreen() {
        super("Battleship – Başlangıç Ekranı");

        // Nimbus Look & Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30));

        // Başlık
        JLabel titleLabel = new JLabel("Battleship");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Girdi paneli
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.setOpaque(false);
        JLabel hostLabel = new JLabel("Sunucu IP:");
        hostLabel.setForeground(Color.LIGHT_GRAY);
        hostLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        hostField = new JTextField("localhost");
        hostField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(Color.LIGHT_GRAY);
        portLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        portField = new JTextField("12345");
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        inputPanel.add(hostLabel);
        inputPanel.add(hostField);
        inputPanel.add(portLabel);
        inputPanel.add(portField);
        mainPanel.add(inputPanel, BorderLayout.CENTER);

        // Buton
        findGameBtn = new JButton("Oyun Ara");
        findGameBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        findGameBtn.setBackground(new Color(70, 130, 180));
        findGameBtn.setForeground(Color.WHITE);
        findGameBtn.setFocusPainted(false);
        findGameBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(findGameBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        // Buton aksiyonu
        findGameBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(() ->
                new LobbyUI(hostField.getText().trim(), portField.getText().trim())
            );
        });

        setContentPane(mainPanel);
        setSize(450, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StartScreen::new);
    }
}