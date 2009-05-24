package org.dreamsoft.chessplayer.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.dreamsoft.chessplayer.client.ChessBoardRenderer.HighlightMode;

public class ChessBoard implements Cloneable, Constantes {

	private final int board[][] = new int[8][8];

	private final int[] kingPos = new int[] { 0, 0, 0 };

	private LinkedList<ChessMove> moveHistory = new LinkedList<ChessMove>();

	private ChessBoardRenderer renderer;

	private ArrayList<int[]> legalMoveForselectPiece = null;

	private int selectPos = -1;

	private final int getPiece(int x, int y) {
		// color + type * 10 + moves * 100
		return board[x][y];
	}

	public int getColor(int x, int y) {
		return board[x][y] % 10;
	}

	public int getType(int x, int y) {
		return board[x][y] % 100 / 10;
	}

	private int getMoves(int x, int y) {
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
	}

	public ArrayList<int[]> getAllowedMoves(int x, int y) {
		ArrayList<int[]> list = new ArrayList<int[]>();
		int type = getType(x, y);
		int color = getColor(x, y);
		int oponentColor = (color == WHITE ? BLACK : WHITE);

		switch (type) {
		case PAWN:
			int step = (color == WHITE) ? -1 : 1;
			int y1 = y + step;
			int y2 = y + 2 * step;

			if (getColor(x, y1) == EMPTY) {
				addValid(x, y, list, x, y1);
				if (getMoves(x, y) == 0 && getColor(x, y2) == EMPTY) {
					addValid(x, y, list, x, y2);
				}
			}
			if (x < 7 && getColor(x + 1, y1) == oponentColor) {
				addValid(x, y, list, x + 1, y1);
			}
			if (x > 0 && getColor(x - 1, y1) == oponentColor) {
				addValid(x, y, list, x - 1, y1);
			}

			// En passant
			int enPassantY = (color == WHITE) ? 3 : 4;
			if (y == enPassantY) {
				ChessMove lastMove = moveHistory.getLast();
				if (lastMove.fromPiece % 100 == PAWN * 10 + oponentColor && lastMove.toY == enPassantY && Math.abs(lastMove.fromY - lastMove.toY) > 1) {
					if (lastMove.toX == x - 1) {
						addValid(x, y, list, x - 1, y1);
					} else if (lastMove.toX == x + 1) {
						addValid(x, y, list, x + 1, y1);
					}
				}
			}

			break;
		case QUEEN:
			addValidUntilBlocked(x, y, list, 0, 1);
			addValidUntilBlocked(x, y, list, 0, -1);
			addValidUntilBlocked(x, y, list, 1, 0);
			addValidUntilBlocked(x, y, list, -1, 0);
			addValidUntilBlocked(x, y, list, 1, 1);
			addValidUntilBlocked(x, y, list, 1, -1);
			addValidUntilBlocked(x, y, list, -1, 1);
			addValidUntilBlocked(x, y, list, -1, -1);

			break;
		case ROOK:
			addValidUntilBlocked(x, y, list, 0, 1);
			addValidUntilBlocked(x, y, list, 0, -1);
			addValidUntilBlocked(x, y, list, 1, 0);
			addValidUntilBlocked(x, y, list, -1, 0);

			break;
		case KNIGHT:
			ArrayList<int[]> mvtList = getKnightMovesArrayList(x, y);
			for (Iterator<int[]> iter = mvtList.iterator(); iter.hasNext();) {
				int pos[] = (int[]) iter.next();
				addValid(x, y, list, pos[0], pos[1]);
			}

			break;
		case BISHOP:
			// 4 diagonales, cas d'arrêt :
			// pièce adversaire avec case autorisée
			// pièce de ma couleur avec case autorisée
			// sortie de grid
			addValidUntilBlocked(x, y, list, 1, 1);
			addValidUntilBlocked(x, y, list, 1, -1);
			addValidUntilBlocked(x, y, list, -1, 1);
			addValidUntilBlocked(x, y, list, -1, -1);
			break;
		case KING:
			addValid(x, y, list, x + 1, y);
			addValid(x, y, list, x + 1, y + 1);
			addValid(x, y, list, x + 1, y - 1);
			addValid(x, y, list, x, y + 1);
			addValid(x, y, list, x, y - 1);
			addValid(x, y, list, x - 1, y + 1);
			addValid(x, y, list, x - 1, y - 1);
			addValid(x, y, list, x - 1, y);
			// T0,1,2,3,R4,5,6,T7
			if (getMoves(x, y) == 0) {
				if (getPiece(1, y) == EMPTY && getPiece(2, y) == EMPTY && getPiece(3, y) == EMPTY && getPiece(0, y) == ROOK * 10 + color && !isChessAfterMove(x, y, x - 1, y)) {
					addValid(x, y, list, x - 2, y); // grand roc
				}
				if (getPiece(5, y) == EMPTY && getPiece(6, y) == EMPTY && getPiece(7, y) == (ROOK * 10 + color) && !isChessAfterMove(x, y, x + 1, y)) {
					addValid(x, y, list, x + 2, y); // petit roc
				}
			}
			break;
		}
		return list;
	}

	private boolean isOnBoard(int _x, int _y) {
		return (_x >= 0 && _x < 8 && _y >= 0 && _y < 8);
	}

	private void addValidUntilBlocked(int x, int y, ArrayList<int[]> list, int deltax, int deltay) {
		int x2 = x;
		int y2 = y;
		while (true) {
			x2 += deltax;
			y2 += deltay;
			if (!isOnBoard(x2, y2))
				break;
			addValid(x, y, list, x2, y2);
			// on s'arrete si on a rencontré une piece!!
			if (getPiece(x2, y2) != EMPTY)
				break;
		}
	}

	private void addValid(int x, int y, ArrayList<int[]> list, int x2, int y2) {
		if (isOnBoard(x2, y2) && getColor(x2, y2) != getColor(x, y)) {
			if (!isChessAfterMove(x, y, x2, y2)) {
				list.add(new int[] { x2, y2 });
			}
		}
	}

	private ArrayList<int[]> getKnightMovesArrayList(int _x, int _y) {
		ArrayList<int[]> mvtList = new ArrayList<int[]>();
		mvtList.add(new int[] { _x + 2, _y - 1 });
		mvtList.add(new int[] { _x + 2, _y + 1 });
		mvtList.add(new int[] { _x - 2, _y - 1 });
		mvtList.add(new int[] { _x - 2, _y + 1 });
		mvtList.add(new int[] { _x + 1, _y + 2 });
		mvtList.add(new int[] { _x + 1, _y - 2 });
		mvtList.add(new int[] { _x - 1, _y + 2 });
		mvtList.add(new int[] { _x - 1, _y - 2 });
		return mvtList;
	}

	private boolean isChessAfterMove(int x, int y, int _x, int _y) {
		ChessBoard testingBoard = clone();
		testingBoard.move(x, y, _x, _y);
		return testingBoard.isChess(getColor(x, y));
	}

	public boolean isChess(int color) {
		int[] kp = getKingXY(color);
		return isAttacked(kp[0], kp[1]);
	}

	private boolean isAttacked(int x, int y) {
		boolean attacked = false;
		int color = getColor(x, y);
		for (int deltax = -1; deltax <= 1; deltax++) {
			for (int deltay = -1; deltay <= 1; deltay = ((deltax == 0 && deltay == -1) ? 1 : deltay + 1)) {
				int dx = x;
				int dy = y;
				boolean first = true;
				while (true) {
					dx += deltax;
					dy += deltay;
					if (!isOnBoard(dx, dy))
						break;
					// on s'arrete si on a rencontré une piece!!
					int p = getPiece(dx, dy);
					if (p != EMPTY) {
						if (p % 10 != color) {
							switch (p % 100 / 10) {
							case PAWN:
								attacked = (first && (deltax * deltay != 0) && ((dy > y && color == BLACK) || (dy < y && color == WHITE)));
								break;
							case KING:
								attacked = first;
								break;
							case BISHOP:
								attacked = (deltax * deltay != 0);
								break;
							case QUEEN:
								attacked = true;
								break;
							case ROOK:
								attacked = (deltax * deltay == 0);
								break;
							}
						}
						first = false;
						break;
					}
					first = false;
				}
				if (attacked)
					return true;
			}
		}
		ArrayList<int[]> mvtList = getKnightMovesArrayList(x, y);
		for (Iterator<int[]> iter = mvtList.iterator(); iter.hasNext();) {
			int pos[] = (int[]) iter.next();
			if (isOnBoard(pos[0], pos[1])) {
				if (getType(pos[0], pos[1]) == KNIGHT && getColor(pos[0], pos[1]) != color) {
					return true;
				}
			}
		}
		return false;
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

	public boolean isMat(int color) {
		// On doit tester si une autre pièce peut empècher l'echec
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (getColor(i, j) == color && getAllowedMoves(i, j).size() > 0) {
					return false;
				}
			}
		}
		return true;
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
	
	public ArrayList<ChessMove> getAllMoves(int color) {
		ArrayList<ChessMove> allMoves = new ArrayList<ChessMove>();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (getColor(i, j) == color) {
					ArrayList<int[]> potentialMoves = new ArrayList<int[]>();
					try {
						potentialMoves = getAllowedMoves(i, j);
					} catch (Exception e) {
						e.printStackTrace();
					}
					for (Iterator<int[]> iterator = potentialMoves.iterator(); iterator.hasNext();) {
						int[] toXY = iterator.next();
						allMoves.add(getMove(i, j, toXY[0], toXY[1]));
					}
				}
			}
		}
		return allMoves;
	}

	public void move(ChessMove quickChessMove) {
		move(quickChessMove.fromX, quickChessMove.fromY, quickChessMove.toX, quickChessMove.toY);
	}

	public void select(int x, int y) {
		selectPos = x + y * 10;
		if (renderer != null) {
			renderer.highlight(x, y, HighlightMode.SELECTED);
			legalMoveForselectPiece = getAllowedMoves(x, y);
			// Changer le style
			for (Iterator<int[]> iter = legalMoveForselectPiece.iterator(); iter.hasNext();) {
				int pos[] = (int[]) iter.next();
				renderer.highlight(pos[0], pos[1], HighlightMode.LEGAL);
			}
		}

	}

	public boolean moveSelectedPiece(int x, int y) {
		boolean f = false;
		if (selectPos != x + y * 10) {
			for (Iterator<int[]> iter = legalMoveForselectPiece.iterator(); iter.hasNext();) {
				int pos[] = (int[]) iter.next();
				if (pos[0] == x && pos[1] == y) {
					f = true;
					break;
				}
			}
			if (f) {
				move(selectPos % 10, selectPos / 10, x, y);
			}
		}
		return f;
	}

	public int getSelectedPos() {
		return selectPos;
	}

	public void unselect() {
		selectPos = -1;
		if (renderer != null) {
			renderer.clearSelection();
		}
	}

	public void setPromote(int x, int y, int piece) {
		setPiece(x, y, piece);
	}

	public int[] searchPieceXY(int type, int color, int row_start, int col_start, int row_end, int col_end) {
		int[] foundPiece = null;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				if (getColor(x, y) == color && getType(x, y) == type) {
					// Verifie les indices de position de depart
					if (col_start > 0 && x != col_start) {
						continue;
					}
					if (row_start < 8 && y != row_start) {
						continue;
					}

					// Verifie la destination
					ArrayList<int[]> allowedMoves = getAllowedMoves(x, y);
					for (Iterator<int[]> iter = allowedMoves.iterator(); iter.hasNext();) {
						int[] pos = (int[]) iter.next();
						if (pos[0] == col_end && pos[1] == row_end) {
							foundPiece = new int[] { x, y };
							break;
						}
					}
				}
				if (foundPiece != null)
					break;
			}
			if (foundPiece != null)
				break;
		}
		return foundPiece;
	}

}
