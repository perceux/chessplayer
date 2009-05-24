package org.dreamsoft.chessplayer.client;

import java.util.LinkedList;

public class ChessBoard implements Cloneable, Constantes {

	private final int board[][] = new int[8][8];

	private final int[] kingPos = new int[] { 0, 0, 0 };

	LinkedList<ChessMove> moveHistory = new LinkedList<ChessMove>();

	private ChessBoardRenderer renderer;

	public final int getPiece(int x, int y) {
		// color + type * 10 + moves * 100
		return board[x][y];
	}

	public int getColor(int x, int y) {
		return board[x][y] % 10;
	}

	public int getType(int x, int y) {
		return board[x][y] % 100 / 10;
	}

	int getMoves(int x, int y) {
		return board[x][y] / 100;
	}

	private int makePiece(int type, int color) {
		return type * 10 + color;
	}

	public void reset() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				setPiece(i, j, EMPTY);
			}
		}
		int colors[] = new int[] { WHITE, BLACK };
		for (int i = 0; i < colors.length; i++) {
			int color = colors[i];
			int pawnRow = (color != WHITE) ? 1 : 6;
			int pieceRow = (color != WHITE) ? 0 : 7;
			for (int j = 0; j < 8; j++) {
				setPiece(j, pawnRow, makePiece(PAWN, color));
			}
			int pieces[] = new int[] { ROOK, KNIGHT, BISHOP, QUEEN, KING, BISHOP, KNIGHT, ROOK };
			for (int j = 0; j < pieces.length; j++) {
				setPiece(j, pieceRow, makePiece(pieces[j], color));
			}
		}

		moveHistory.clear();
		if (renderer != null)
			renderer.clearSelection();
	}

	boolean isOnBoard(int _x, int _y) {
		return (_x >= 0 && _x < 8 && _y >= 0 && _y < 8);
	}

	public void move(int x, int y, int _x, int _y) {
		int piece = getPiece(x, y);
		if (piece != EMPTY) {
			// Enregistrement du mouvement
			moveHistory.add(new ChessMove(getPiece(x, y), x, y, getPiece(_x, _y), _x, _y));

			// Detection du rock pour bouger la tour
			if (getType(x, y) == KING && Math.abs(x - _x) == 2) {
				// Deplacement de la tour
				move(_x > x ? 7 : 0, _y, _x > x ? _x - 1 : _x + 1, _y);
			}

			// Detection du pion en fin de ligne pour promote
			if (getType(x, y) == PAWN && (_y == 0 || _y == 7)) {
				if (renderer != null) {
					renderer.setPromote(_x, _y, getColor(x, y));
				} else {
					piece = QUEEN * 10 + piece % 10;
				}
			}

			// Detection du "en passant" pour supprimer le pion adverse
			if (getType(x, y) == PAWN && Math.abs(y - _y) > 0 && Math.abs(x - _x) > 0 && getPiece(_x, _y) == EMPTY) {
				// TODO history?
				setPiece(_x, y, EMPTY);
			}

			setPiece(_x, _y, piece + 100); // moves + 1
			setPiece(x, y, EMPTY);
		} else {
			System.out.println("move empty " + x + "," + y);
		}
	}

	public void undo() {
		/*
		 * QuickChessMove move = moveHistory.pop(); setPiece(move.toX, move.toY,
		 * move.toPiece); setPiece(move.fromX, move.fromY, move.fromPiece);
		 */
	}

	protected void setPiece(int x, int y, int value) {
		if (!isOnBoard(x, y)) {
			return;
		}
		if (value % 100 / 10 == KING) {
			kingPos[value % 10] = x + y * 10;
		}
		board[x][y] = value;
		if (renderer != null)
			renderer.render(x, y, value);
	}

	public int[] getKingXY(int color) {
		int kp = kingPos[color];
		return new int[] { kp % 10, kp / 10 };
	}

	public final ChessBoard clone() {
		final ChessBoard cloned = new ChessBoard();
		for (int i = 0; i < 8; i++) {
			System.arraycopy(this.board[i], 0, cloned.board[i], 0, board[i].length);
		}
		System.arraycopy(this.kingPos, 0, cloned.kingPos, 0, kingPos.length);
		return cloned;
	}

	public void setRenderer(ChessBoardRenderer quickChessBoardRenderer) {
		this.renderer = quickChessBoardRenderer;
	}

	public ChessMove getMove(int x, int y, int i, int j) {
		return new ChessMove(getPiece(x, y), x, y, getPiece(i, j), i, j);
	}

	public void move(ChessMove chessMove) {
		move(chessMove.fromX, chessMove.fromY, chessMove.toX, chessMove.toY);
	}

	public void setPromote(int x, int y, int piece) {
		setPiece(x, y, piece);
	}

	public ChessBoardRenderer getRenderer() {
		return renderer;
	}

	public boolean hasTwoKings() {
		for (int color : new int[] { WHITE, BLACK }) {
			int[] kp = getKingXY(color);
			if (getType(kp[0], kp[1]) != KING)
				return false;
		}
		return true;
	}

}
