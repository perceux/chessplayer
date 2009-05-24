package org.dreamsoft.chessplayer.client;

public class ChessMove {
	public int fromPiece;

	public int toPiece;

	public int fromX;

	public int fromY;

	public int toX;

	public int toY;

	public String text = "";

	public double note = 0.0;

	public ChessMove(int fromPiece, int x, int y, int toPiece, int toX, int toY) {
		this.fromPiece = fromPiece;
		this.toPiece = toPiece;
		this.fromX = x;
		this.fromY = y;
		this.toX = toX;
		this.toY = toY;
	}

	@Override
	public String toString() {
		return "{" + text + " <" + note + "> : " + fromPiece + "(" + fromX + "," + fromY + ")" + " " + toPiece + "(" + toX + "," + toY + ") }";
	}

}
