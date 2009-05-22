package org.dreamsoft.chessplayer.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.dreamsoft.chessplayer.client.ChessBoardRenderer.HighlightMode;

import com.google.gwt.user.client.Window;

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

	int getColor(int x, int y) {
		return board[x][y] % 10;
	}

	private int getType(int x, int y) {
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
		int _x = x;
		int _y = y;

		switch (type) {
		case PAWN:
			int sens = (color == WHITE) ? -1 : 1;
			int nexty = _y + sens;
			int nextnexty = _y + 2 * sens;

			if (getColor(_x, nexty) == EMPTY) {
				addValid(x, y, list, _x, nexty);
				if (getMoves(x, y) == 0 && getColor(_x, nextnexty) == EMPTY) {
					addValid(x, y, list, _x, nextnexty);
				}
			}
			if (_x < 7 && getColor(_x + 1, nexty) == oponentColor) {
				addValid(x, y, list, _x + 1, nexty);
			}
			if (_x > 0 && getColor(_x - 1, nexty) == oponentColor) {
				addValid(x, y, list, _x - 1, nexty);
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
			ArrayList<int[]> mvtList = getKnightMovesArrayList(_x, _y);
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
			addValid(x, y, list, _x + 1, _y);
			addValid(x, y, list, _x + 1, _y + 1);
			addValid(x, y, list, _x + 1, _y - 1);
			addValid(x, y, list, _x, _y + 1);
			addValid(x, y, list, _x, _y - 1);
			addValid(x, y, list, _x - 1, _y + 1);
			addValid(x, y, list, _x - 1, _y - 1);
			addValid(x, y, list, _x - 1, _y);
			// T0,1,2,3,R4,5,6,T7
			if (getMoves(x, y) == 0) {
				if (getPiece(1, y) == EMPTY && getPiece(2, y) == EMPTY && getPiece(3, y) == EMPTY && getPiece(0, y) == ROOK * 10 + color && tryMoveForChess(x, y, x - 1, y)) {
					addValid(x, y, list, x - 2, y); // grand roc
				}
				if (getPiece(5, y) == EMPTY && getPiece(6, y) == EMPTY && getPiece(7, y) == (ROOK * 10 + color) && tryMoveForChess(x, y, x + 1, y)) {
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
		int dx = x;
		int dy = y;
		while (true) {
			dx += deltax;
			dy += deltay;
			if (!isOnBoard(dx, dy))
				break;
			addValid(x, y, list, dx, dy);
			// on s'arrete si on a rencontré une piece!!
			if (getPiece(dx, dy) != EMPTY)
				break;
		}
	}

	private void addValid(int x, int y, ArrayList<int[]> list, int _x, int _y) {
		if (isOnBoard(_x, _y)) {
			int targetColor = getColor(_x, _y);
			if (targetColor == EMPTY || targetColor != getColor(x, y)) {
				if (!tryMoveForChess(x, y, _x, _y)) {
					list.add(new int[] { _x, _y });
				}
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

	private boolean tryMoveForChess(int x, int y, int _x, int _y) {
		ChessBoard testingBoard = clone();
		testingBoard.move(x, y, _x, _y);
		return testingBoard.ifChess(getColor(x, y));
	}

	public boolean ifChess(int color) {
		int[] kp = getKingXY(color);
		int i = kp[0];
		int j = kp[1];
		return checkKingChessLine(i, j, 0, 1, color) || checkKingChessLine(i, j, 0, -1, color) || checkKingChessLine(i, j, 1, 0, color) || checkKingChessLine(i, j, -1, 0, color)
				|| checkKingChessLine(i, j, 1, 1, color) || checkKingChessLine(i, j, 1, -1, color) || checkKingChessLine(i, j, -1, 1, color) || checkKingChessLine(i, j, -1, -1, color)
				|| checkKingChessKnight(i, j, color);
	}

	private boolean checkKingChessKnight(int _x, int _y, int color) {
		ArrayList<int[]> mvtList = getKnightMovesArrayList(_x, _y);
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

	private boolean checkKingChessLine(int x, int y, int deltax, int deltay, int color) {
		int dx = x;
		int dy = y;
		boolean first = true;
		while (true) {
			dx += deltax;
			dy += deltay;
			if (!isOnBoard(dx, dy))
				break;
			// on s'arrete si on a rencontré une piece!!
			if (getPiece(dx, dy) != EMPTY)
				if (getColor(dx, dy) != color) {
					switch (getType(dx, dy)) {
					case PAWN:
						return (first && (deltax * deltay != 0) && ((dy > y && color == BLACK) || (dy < y && color == WHITE)));
					case KING:
						return first;
					case BISHOP:
						return (deltax * deltay != 0);
					case QUEEN:
						return true;
					case ROOK:
						return (deltax * deltay == 0);
					default:
						return false;
					}
				} else {
					return false;
				}
			first = false;
		}
		return false;
	}

	public boolean moveTo(int type, int color, int row_start, int col_start, int row_end, int col_end) {
		int[] foundPiece = null;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				if (getColor(x, y) == color && getType(x, y) == type) {
					// Verifie les indices de position de depart
					if (col_start >= 0 && x != col_start) {
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
		if (foundPiece == null)
			return false;

		move(foundPiece[0], foundPiece[1], col_end, row_end);
		return true;
	}

	public void move(int x, int y, int _x, int _y) {
		int piece = getPiece(x, y);
		if (piece != EMPTY) {
			// Enregistrement du mouvement
			moveHistory.push(new ChessMove(getPiece(x, y), x, y, getPiece(_x, _y), _x, _y));

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

			// TODO Detection du "en passant" pour supprimer le pion adverse
			// if (type == PAWN && Math.abs(pos[1] - y) >
			// 1 && Math.abs(pos[0] - x) > 1) {
			// }

			setPiece(_x, _y, piece + 100); // moves + 1
			setPiece(x, y, EMPTY);
		} else {
			System.out.println("move empty " + x + "," + y);
		}
	}

	public boolean ifMat(int color) {
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

	protected final ChessBoard clone() {
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
						Window.alert("_____________ i,j=" + i + "," + j);
						System.out.println("_____________ piece i,j=" + getPiece(i, j));
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
}
