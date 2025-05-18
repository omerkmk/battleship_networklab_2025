// src/client/GameUI.java
package client;

import common.PlaceShipRequest;
import common.PlaceShipResponse;
import common.ReadyRequest;
import common.TurnMessage;
import model.Position;
import model.Ship;
import model.GameRules;
import model.GameState;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;

/**
 * Gemi yerleştirme + Ready ekranı. Savaş yok.
 */
public class GameUI extends JFrame {
    private static final int G = GameRules.GRID_SIZE;
    private static final Color WATER = new Color(173, 216, 230);
    private static final Border CELL_BORDER = BorderFactory.createLineBorder(Color.BLUE, 1);

    private final GameClient client;
    private final int myPlayer;
    private final GameState state = new GameState();

    private final BoardPanel board;
    private final JLabel info = new JLabel();
    private final JButton ready = new JButton("Ready");

    private int shipIdx = 0;
    private boolean dragHoriz = true;
    private Position firstClick = null;
    private final int[] sizes = GameRules.SHIP_SIZES;

    public GameUI(GameClient client, int myPlayer) {
        super("Place Ships – Player " + (myPlayer + 1));
        this.client   = client;
        this.myPlayer = myPlayer;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5,5));

        // Tahta
        board = new BoardPanel();
        add(board, BorderLayout.CENTER);

        // Sidebar
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createTitledBorder("Ships"));
        JButton rot = new JButton("Rotate");
        rot.addActionListener(e -> dragHoriz = !dragHoriz);
        side.add(rot);
        side.add(Box.createVerticalStrut(10));
        for(int len:sizes){
            JLabel lab = new JLabel(""+len, SwingConstants.CENTER);
            lab.setMaximumSize(new Dimension(40,30));
            lab.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            lab.setTransferHandler(new ValueExportTransferHandler(len));
            lab.addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    JComponent c = (JComponent)e.getSource();
                    c.getTransferHandler().exportAsDrag(c,e,TransferHandler.COPY);
                }
            });
            side.add(lab);
            side.add(Box.createVerticalStrut(5));
        }
        add(side, BorderLayout.EAST);

        // Bottom: bilgi + Ready
        JPanel bot = new JPanel(new FlowLayout(FlowLayout.LEFT));
        info.setFont(new Font("SansSerif",Font.BOLD,14));
        bot.add(info);
        ready.setEnabled(false);
        ready.addActionListener(e->{
            client.sendReady();
            ready.setEnabled(false);
            info.setText("Waiting for opponent...");
        });
        bot.add(ready);
        add(bot, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        updateView();
    }

    /** Yerleştirme görünümünü yeniler */
    private void updateView(){
        board.clear();
        board.revealShips(state.getBoard(myPlayer).getShips());
        board.setButtonsEnabled(shipIdx < sizes.length);

        if (shipIdx < sizes.length) {
            info.setText("Place ship size " + sizes[shipIdx]);
            ready.setEnabled(false);
        } else {
            info.setText("All ships placed");
            ready.setEnabled(true);
        }
    }

    /** Server’dan gelen yerleştirme cevabı */
    public void handlePlaceShipResponse(PlaceShipResponse resp){
        SwingUtilities.invokeLater(() -> {
            if (!resp.isSuccess()) {
                JOptionPane.showMessageDialog(this,
                    "Cannot place here!", "Invalid",
                    JOptionPane.WARNING_MESSAGE);
                shipIdx--;
            }
            updateView();
        });
    }

    /** Server’dan gelen sıra mesajı: BattleUI açılacak */
    public void handleTurnMessage(TurnMessage tm){
        SwingUtilities.invokeLater(() -> {
            BattleUI battle = new BattleUI(client, myPlayer, state, tm.isYourTurn());
            client.setUI(battle);
            dispose();
        });
    }

    // --- İç sınıflar ---

    private class BoardPanel extends JPanel {
        private final JButton[][] b = new JButton[G][G];
        BoardPanel(){
            setBorder(BorderFactory.createTitledBorder(CELL_BORDER,
                "Your Board", TitledBorder.CENTER, TitledBorder.TOP));
            setLayout(new GridLayout(G,G));
            for(int r=0;r<G;r++){
                for(int c=0;c<G;c++){
                    JButton btn = new JButton();
                    btn.setPreferredSize(new Dimension(35,35));
                    btn.setBackground(WATER); btn.setOpaque(true);
                    btn.setBorder(CELL_BORDER);

                    Position pos = new Position(r,c);
                    btn.putClientProperty("pos", pos);
                    btn.addActionListener(new CellHandler(pos));

                    btn.setDropTarget(new DropTarget(btn, new DropTargetAdapter(){
                        public void drop(DropTargetDropEvent e){
                            try {
                                String sval = (String)e.getTransferable()
                                    .getTransferData(DataFlavor.stringFlavor);
                                int len = Integer.parseInt(sval);
                                Ship s = dragHoriz
                                    ? Ship.fromHorizontal(pos,len)
                                    : Ship.fromVertical(pos,len);
                                if (!state.placeShip(myPlayer, s)) {
                                    JOptionPane.showMessageDialog(GameUI.this,
                                        "Cannot place!", "Invalid",
                                        JOptionPane.WARNING_MESSAGE);
                                    e.dropComplete(false);
                                } else {
                                    client.sendPlaceShip(s);
                                    shipIdx++;
                                    updateView();
                                    e.dropComplete(true);
                                }
                            } catch (Exception ex) {
                                e.rejectDrop();
                            }
                        }
                    }));

                    b[r][c] = btn;
                    add(btn);
                }
            }
        }

        void clear(){
            for(int r=0;r<G;r++)for(int c=0;c<G;c++){
                b[r][c].setText("");
                b[r][c].setBackground(WATER);
                b[r][c].setBorder(CELL_BORDER);
                b[r][c].setEnabled(true);
            }
        }
        void revealShips(java.util.List<Ship> ships){
            for(Ship s:ships)for(Position p:s.getPositions()){
                JButton btn = b[p.getRow()][p.getCol()];
                btn.setBackground(Color.DARK_GRAY);
                btn.setEnabled(false);
            }
        }
        void setButtonsEnabled(boolean en){
            for(int r=0;r<G;r++)for(int c=0;c<G;c++){
                if(b[r][c].isEnabled()) b[r][c].setEnabled(en);
            }
        }
    }

    private class CellHandler implements ActionListener {
        private final Position pos;
        CellHandler(Position pos) { this.pos = pos; }
        public void actionPerformed(ActionEvent e) {
            if (shipIdx >= sizes.length) return;
            if (firstClick == null) {
                firstClick = pos;
                highlight(pos);
            } else {
                Ship s = new Ship(firstClick, pos);
                int need = sizes[shipIdx];
                if (s.length() == need && state.placeShip(myPlayer,s)) {
                    client.sendPlaceShip(s);
                    shipIdx++;
                }
                firstClick = null;
                updateView();
            }
        }
        private void highlight(Position p) {
            JButton btn = board.b[p.getRow()][p.getCol()];
            btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW,3));
        }
    }

    static class ValueExportTransferHandler extends TransferHandler {
        private final String val;
        ValueExportTransferHandler(int v){ val = String.valueOf(v); }
        @Override protected Transferable createTransferable(JComponent c){
            return new StringSelection(val);
        }
        @Override public int getSourceActions(JComponent c){ return COPY; }
    }
}
