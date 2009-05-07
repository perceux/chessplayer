package org.dreamsoft.chessplayer.client;

import com.google.gwt.user.client.ui.Grid;
/**
 * Mouvement
 * @author pcv
 *
 */
public class ChessMove {
	public ChessMove(ChessPiece piece1, int x, int y, ChessPiece piece2, int x2, int y2) {
		this.fromPiece = piece1;
		this.toPiece = piece2;
		this.fromX = x;
		this.fromY = y;
		this.toX = x2;
		this.toY = y2;
	}

	public ChessPiece fromPiece;

	public ChessPiece toPiece;

	public int fromX;

	public int fromY;

	public int toX;

	public int toY;

	public void undoMove(Grid grid) {
		grid.remove(fromPiece);
		grid.setWidget(fromY, fromX, fromPiece);
		fromPiece.x = fromX;
		fromPiece.y = fromY;
		fromPiece.nbMove--;
		if (toPiece != null) {
			grid.setWidget(toY, toX, toPiece);
		} else {
			grid.clearCell(toY, toX);	
		}

	}
}
