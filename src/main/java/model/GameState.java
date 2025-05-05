package model;


public class GameState {
    private Board myBoard;
    private Board opponentBoard;
    private boolean myTurn;
    private boolean gameOver;

    public GameState() {
        myBoard = new Board();
        opponentBoard = new Board();
        myTurn = false;
        gameOver = false;
    }

    public Board getMyBoard() {
        return myBoard;
    }

    public Board getOpponentBoard() {
        return opponentBoard;
    }

    public boolean isMyTurn() {
        return myTurn;
    }

    public void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}
