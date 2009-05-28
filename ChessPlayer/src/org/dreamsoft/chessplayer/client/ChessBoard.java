package org.dreamsoft.chessplayer.client;

import java.util.LinkedList;

public class ChessBoard implements Cloneable, Constantes {

	private String board = "                                                                ";

	LinkedList<ChessMove> moveHistory = new LinkedList<ChessMove>();

	private ChessBoardRenderer renderer;

	public final int getPiece(int x, int y) {
		return getBoard().charAt(x + y * 8); // color + type * 10 + moves * 100
	}

	public int getColor(int x, int y) {
		return getBoard().charAt(x + y * 8) % 10;
	}

	public int getType(int x, int y) {
		return getBoard().charAt(x + y * 8) % 100 / 10;
	}

	public boolean hasMoved(int x, int y) {
		return (getBoard().charAt(x + y * 8) > 100);
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
			moveHistory.add(new ChessMove(piece, x, y, getPiece(_x, _y), _x, _y));

			// Detection du rock pour bouger la tour
			if (getType(x, y) == KING && Math.abs(x - _x) == 2) {
				// Deplacement de la tour
				move(_x > x ? 7 : 0, _y, _x > x ? _x - 1 : _x + 1, _y);
			}

			if (getType(x, y) == PAWN) {
				// Detection du pion en fin de ligne pour promote
				if (_y == 0 || _y == 7) {
					if (renderer != null) {
						// TODO History ?
						renderer.setPromote(_x, _y, getColor(x, y));
					} else {
						setBoard(ChessBoardUtils.setPiece(getBoard(), x + y * 8, (char) piece));
					}
					// Detection du "en passant" pour supprimer le pion adverse
				} else if (Math.abs(y - _y) > 0 && Math.abs(x - _x) > 0 && getPiece(_x, _y) == EMPTY) {
					// TODO history?
					setBoard(ChessBoardUtils.setPiece(getBoard(), _x + y * 8, EMPTY));
					if (renderer != null) {
						renderer.render(_x, y, getPiece(_x, y));
					}
				}
			}

			// Déplacement de la piece
			setBoard(ChessBoardUtils.move(getBoard(), x + y * 8, _x + _y * 8));
			
			if (renderer != null) {
				renderer.render(_x, _y, getPiece(_x, _y));
				renderer.render(x, y, getPiece(x, y));
			}
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

	/**
	 * @deprecated
	 */
	protected void setPiece(int x, int y, int value) {
		if (!isOnBoard(x, y)) {
			return;
		}
		char[] sb = getBoard().toCharArray();
		sb[x + y * 8] = (char) value;
		setBoard(String.copyValueOf(sb));
		// board.[x + y * 8] = value;
		if (renderer != null)
			renderer.render(x, y, value);
	}

	public int getKingPos(int color) {
		int p1 = getBoard().indexOf(KING * 10 + color);
		int p2 = getBoard().indexOf(100 + KING * 10 + color);
		return p1 != -1 ? p1 : p2;
	}

	public final ChessBoard clone() {
		final ChessBoard cloned = new ChessBoard();
		cloned.setBoard(this.getBoard());
		return cloned;
	}

	public void setRenderer(ChessBoardRenderer renderer) {
		this.renderer = renderer;
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
		return getKingPos(WHITE) >= 0 && getKingPos(BLACK) >= 0;
	}

	/**
	 * public static final int PAWN = 1; public static final int ROOK = 2;
	 * public static final int KNIGHT = 3; public static final int BISHOP = 4;
	 * public static final int KING = 5; public static final int QUEEN = 6;
	 */
	public static int pieceValue[] = { 0, 1, 5, 3, 3, 1, 10 };

	public int[] getScores() {
		int result[] = new int[] { 0, 0 };
		int tmp = 0;
		for (int i = 0; i < 64; i++) {
			tmp = getBoard().charAt(i);
			if (tmp > 0)
				result[(tmp % 10) - 1] += pieceValue[(tmp % 100) / 10];
		}
		return result;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public String getBoard() {
		return board;
	}

}
