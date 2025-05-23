package client;

import common.PlaceShipResponse;
import common.TurnMessage;
import model.Position;
import model.Ship;
import model.GameRules;
import model.GameState;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;

/**
 * GameUI handles the ship placement phase and readiness confirmation.
 * Dark theme, clear color palette, and drag-and-drop interface are used.
 */
public class GameUI extends JFrame {
    private static final int G = GameRules.GRID_SIZE;

    // --- Color palette for the dark-themed UI ---
    private static final Color WATER        = Color.decode("#0D1B2A");
    private static final Color EMPTY_CELL   = Color.decode("#F8F9FA");
    private static final Color SHIP_COLOR   = Color.decode("#E74C3C");
    private static final Color BORDER_COL   = Color.decode("#2C3E50");
    private static final Color HIGHLIGHT    = Color.decode("#F39C12");
    private static final Color READY_BTN_BG = Color.decode("#27AE60");
    private static final Color SIDEBAR_BG   = Color.decode("#34495E");

    // --- Client and state references ---
    private final GameClient client;
    private final int myPlayer;
    private final GameState state = new GameState();

    // --- UI components and state variables ---
    private int shipIdx = 0;
    private boolean dragHoriz = true;
    private Position firstClick = null;
    private final int[] sizes = GameRules.SHIP_SIZES;

    private final BoardPanel board;
    private final JLabel infoLabel = new JLabel();
    private final JButton readyBtn = new JButton("Ready");

    /**
     * Constructs the GameUI for ship placement phase.
     */
    public GameUI(GameClient client, int myPlayer) {
        super("Place Ships – Player " + (myPlayer + 1));
        this.client = client;
        this.myPlayer = myPlayer;

        // Set Nimbus Look & Feel if available
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        // --- Frame setup ---
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(WATER);
        setLayout(new BorderLayout(15, 15));

        // --- Main board area ---
        board = new BoardPanel();
        add(board, BorderLayout.CENTER);

        // --- Sidebar with ships and rotate button ---
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR_BG);
        side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        JLabel shipsLabel = new JLabel("Ships");
        shipsLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        shipsLabel.setForeground(Color.WHITE);
        shipsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        side.add(shipsLabel);
        side.add(Box.createVerticalStrut(10));

        // Rotate Button
        JButton rot = new JButton("Rotate");
        rot.setFont(new Font("Segoe UI", Font.BOLD, 14));
        rot.setBackground(HIGHLIGHT);
        rot.setForeground(Color.BLACK);
        rot.setFocusPainted(false);
        rot.setAlignmentX(Component.CENTER_ALIGNMENT);
        rot.addActionListener(e -> dragHoriz = !dragHoriz);
        side.add(rot);
        side.add(Box.createVerticalStrut(15));

        // Ship labels for drag-and-drop
        for (int len : sizes) {
            JLabel shipLabel = new JLabel(len + "-cell");
            shipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            shipLabel.setForeground(Color.WHITE);
            shipLabel.setBackground(BORDER_COL.darker());
            shipLabel.setOpaque(true);
            shipLabel.setBorder(BorderFactory.createLineBorder(BORDER_COL, 2));
            shipLabel.setMaximumSize(new Dimension(80, 30));
            shipLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            shipLabel.setTransferHandler(new ValueExportTransferHandler(len));
            shipLabel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JComponent c = (JComponent) e.getSource();
                    c.getTransferHandler().exportAsDrag(c, e, TransferHandler.COPY);
                }
            });
            side.add(shipLabel);
            side.add(Box.createVerticalStrut(10));
        }
        add(side, BorderLayout.EAST);

        // --- Bottom panel with instructions and Ready button ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottom.setBackground(WATER);
        infoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoLabel.setForeground(Color.WHITE);
        bottom.add(infoLabel);

        readyBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        readyBtn.setBackground(READY_BTN_BG);
        readyBtn.setForeground(Color.WHITE);
        readyBtn.setFocusPainted(false);
        readyBtn.setBorder(BorderFactory.createLineBorder(BORDER_COL, 2));
        readyBtn.setEnabled(false);
        readyBtn.addActionListener(e -> {
            client.sendReady();
            readyBtn.setEnabled(false);
            infoLabel.setText("Waiting for opponent...");
        });
        bottom.add(readyBtn);
        add(bottom, BorderLayout.SOUTH);

        // --- Final UI setup ---
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);

        updateView();
    }

    /**
     * Updates the board view and UI labels.
     */
    private void updateView() {
        board.clear();
        board.revealShips(state.getBoard(myPlayer).getShips());
        board.setEnabled(shipIdx < sizes.length);
        board.setButtonsEnabled(shipIdx < sizes.length);

        if (shipIdx < sizes.length) {
            infoLabel.setText("Place ship size " + sizes[shipIdx]);
        } else {
            infoLabel.setText("All ships placed");
            readyBtn.setEnabled(true);
        }
    }

    /**
     * Handles server response for ship placement.
     */
    public void handlePlaceShipResponse(PlaceShipResponse resp) {
        SwingUtilities.invokeLater(() -> {
            if (!resp.isSuccess()) {
                JOptionPane.showMessageDialog(this, "Cannot place here!", "Invalid", JOptionPane.WARNING_MESSAGE);
                shipIdx--;
            }
            updateView();
        });
    }

    /**
     * Handles transition to battle phase.
     */
    public void handleTurnMessage(TurnMessage tm) {
        SwingUtilities.invokeLater(() -> {
            BattleUI battle = new BattleUI(client, myPlayer, state, tm.isYourTurn());
            client.setUI(battle);
            dispose();
        });
    }

    // === Inner class for the game board panel ===
    private class BoardPanel extends JPanel {
        private final JButton[][] buttons = new JButton[G][G];

        BoardPanel() {
            setBackground(WATER);
            setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COL, 2),
                "Your Board", TitledBorder.CENTER, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 18), Color.WHITE));
            setLayout(new GridLayout(G, G, 2, 2));
            initGrid();
        }

        private void initGrid() {
            for (int r = 0; r < G; r++) {
                for (int c = 0; c < G; c++) {
                    JButton btn = new JButton();
                    btn.setPreferredSize(new Dimension(40, 40));
                    btn.setBackground(EMPTY_CELL);
                    btn.setOpaque(true);
                    btn.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, BORDER_COL));
                    Position pos = new Position(r, c);
                    btn.putClientProperty("pos", pos);
                    btn.addActionListener(new CellHandler(pos));
                    btn.setDropTarget(new DropTarget(btn, new DropTargetAdapter() {
                        public void drop(DropTargetDropEvent e) {
                            try {
                                String sval = (String) e.getTransferable().getTransferData(DataFlavor.stringFlavor);
                                int len = Integer.parseInt(sval);
                                Ship s = dragHoriz ? Ship.fromHorizontal(pos, len) : Ship.fromVertical(pos, len);
                                if (!state.placeShip(myPlayer, s)) throw new Exception();
                                client.sendPlaceShip(s);
                                shipIdx++;
                                updateView();
                                e.dropComplete(true);
                            } catch (Exception ex) {
                                e.rejectDrop();
                            }
                        }
                    }));
                    buttons[r][c] = btn;
                    add(btn);
                }
            }
        }

        void clear() {
            for (int r = 0; r < G; r++) for (int c = 0; c < G; c++) {
                JButton btn = buttons[r][c];
                btn.setText("");
                btn.setBackground(EMPTY_CELL);
                btn.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, BORDER_COL));
                btn.setEnabled(true);
            }
        }

        void revealShips(java.util.List<Ship> ships) {
            for (Ship s : ships) for (Position p : s.getPositions()) {
                JButton btn = buttons[p.getRow()][p.getCol()];
                btn.setBackground(SHIP_COLOR);
                btn.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, SHIP_COLOR.darker()));
                btn.setEnabled(false);
            }
        }

        void setButtonsEnabled(boolean enabled) {
            for (int r = 0; r < G; r++) for (int c = 0; c < G; c++) {
                JButton btn = buttons[r][c];
                if (btn.isEnabled()) btn.setEnabled(enabled);
            }
        }
    }

    // === Handles two-click placement logic ===
    private class CellHandler implements ActionListener {
        private final Position pos;
        CellHandler(Position pos) { this.pos = pos; }

        public void actionPerformed(ActionEvent e) {
            if (shipIdx >= sizes.length) return;
            if (firstClick == null) {
                firstClick = pos;
                JButton b = board.buttons[pos.getRow()][pos.getCol()];
                b.setBorder(BorderFactory.createLineBorder(HIGHLIGHT, 3));
            } else {
                Ship s = new Ship(firstClick, pos);
                if (s.length() == sizes[shipIdx] && state.placeShip(myPlayer, s)) {
                    client.sendPlaceShip(s);
                    shipIdx++;
                }
                firstClick = null;
                updateView();
            }
        }
    }

    // === Handles drag source for ship labels ===
    static class ValueExportTransferHandler extends TransferHandler {
        private final String val;
        ValueExportTransferHandler(int v) { val = String.valueOf(v); }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection(val);
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }
}
