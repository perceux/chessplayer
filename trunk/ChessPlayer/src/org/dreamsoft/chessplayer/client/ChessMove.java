package org.dreamsoft.chessplayer.client;

public class ChessMove {
	public int fromPiece;

	public int toPiece;

	public int fromX;

	public int fromY;

	public int toX;

	public int toY;

	public ChessMove(int fromPiece, int x, int y, int toPiece, int toX, int toY) {
		this.fromPiece = fromPiece;
		this.toPiece = toPiece;
		this.fromX = x;
		this.fromY = y;
		this.toX = toX;
		this.toY = toY;
	}

}
