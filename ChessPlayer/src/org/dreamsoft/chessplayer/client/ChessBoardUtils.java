package org.dreamsoft.chessplayer.client;

import java.util.ArrayList;
import java.util.Iterator;

import org.dreamsoft.chessplayer.client.ChessBoardRenderer.HighlightMode;

public class ChessBoardUtils implements Constantes {
	public static ArrayList<ChessMove> getAllMoves(ChessBoard b, int color) {
		ArrayList<ChessMove> allMoves = new ArrayList<ChessMove>();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (b.getColor(i, j) == color) {
					ArrayList<int[]> potentialMoves = new ArrayList<int[]>();
					try {
						potentialMoves = getAllowedMoves(b, i, j);
					} catch (Exception e) {
						e.printStackTrace();
					}
					for (Iterator<int[]> iterator = potentialMoves.iterator(); iterator.hasNext();) {
						int[] toXY = iterator.next();
						allMoves.add(b.getMove(i, j, toXY[0], toXY[1]));
					}
				}
			}
		}
		return allMoves;
	}

	public static ArrayList<int[]> getAllowedMoves(ChessBoard b, int x, int y) {
		ArrayList<int[]> list = new ArrayList<int[]>();
		int type = b.getType(x, y);
		int color = b.getColor(x, y);
		int oponentColor = (color == WHITE ? BLACK : WHITE);

		switch (type) {
		case PAWN:
			int step = (color == WHITE) ? -1 : 1;
			int y1 = y + step;
			int y2 = y + 2 * step;

			if (b.getColor(x, y1) == EMPTY) {
				addValid(b, x, y, list, x, y1);
				if (b.getMoves(x, y) == 0 && b.getColor(x, y2) == EMPTY) {
					addValid(b, x, y, list, x, y2);
				}
			}
			if (x < 7 && b.getColor(x + 1, y1) == oponentColor) {
				addValid(b, x, y, list, x + 1, y1);
			}
			if (x > 0 && b.getColor(x - 1, y1) == oponentColor) {
				addValid(b, x, y, list, x - 1, y1);
			}

			// En passant
			int enPassantY = (color == WHITE) ? 3 : 4;
			if (y == enPassantY) {
				ChessMove lastMove = b.moveHistory.getLast();
				if (lastMove.fromPiece % 100 == PAWN * 10 + oponentColor && lastMove.toY == enPassantY && Math.abs(lastMove.fromY - lastMove.toY) > 1) {
					if (lastMove.toX == x - 1) {
						addValid(b, x, y, list, x - 1, y1);
					} else if (lastMove.toX == x + 1) {
						addValid(b, x, y, list, x + 1, y1);
					}
				}
			}

			break;
		case QUEEN:
			addValidUntilBlocked(b, x, y, list, 0, 1);
			addValidUntilBlocked(b, x, y, list, 0, -1);
			addValidUntilBlocked(b, x, y, list, 1, 0);
			addValidUntilBlocked(b, x, y, list, -1, 0);
			addValidUntilBlocked(b, x, y, list, 1, 1);
			addValidUntilBlocked(b, x, y, list, 1, -1);
			addValidUntilBlocked(b, x, y, list, -1, 1);
			addValidUntilBlocked(b, x, y, list, -1, -1);

			break;
		case ROOK:
			addValidUntilBlocked(b, x, y, list, 0, 1);
			addValidUntilBlocked(b, x, y, list, 0, -1);
			addValidUntilBlocked(b, x, y, list, 1, 0);
			addValidUntilBlocked(b, x, y, list, -1, 0);

			break;
		case KNIGHT:
			ArrayList<int[]> mvtList = getKnightMovesArrayList(x, y);
			for (Iterator<int[]> iter = mvtList.iterator(); iter.hasNext();) {
				int pos[] = (int[]) iter.next();
				addValid(b, x, y, list, pos[0], pos[1]);
			}

			break;
		case BISHOP:
			// 4 diagonales, cas d'arrêt :
			// pièce adversaire avec case autorisée
			// pièce de ma couleur avec case autorisée
			// sortie de grid
			addValidUntilBlocked(b, x, y, list, 1, 1);
			addValidUntilBlocked(b, x, y, list, 1, -1);
			addValidUntilBlocked(b, x, y, list, -1, 1);
			addValidUntilBlocked(b, x, y, list, -1, -1);
			break;
		case KING:
			addValid(b, x, y, list, x + 1, y);
			addValid(b, x, y, list, x + 1, y + 1);
			addValid(b, x, y, list, x + 1, y - 1);
			addValid(b, x, y, list, x, y + 1);
			addValid(b, x, y, list, x, y - 1);
			addValid(b, x, y, list, x - 1, y + 1);
			addValid(b, x, y, list, x - 1, y - 1);
			addValid(b, x, y, list, x - 1, y);
			// T0,1,2,3,R4,5,6,T7
			if (b.getMoves(x, y) == 0) {
				if (b.getPiece(1, y) == EMPTY && b.getPiece(2, y) == EMPTY && b.getPiece(3, y) == EMPTY && b.getPiece(0, y) == ROOK * 10 + color && !isChessAfterMove(b, x, y, x - 1, y)) {
					addValid(b, x, y, list, x - 2, y); // grand roc
				}
				if (b.getPiece(5, y) == EMPTY && b.getPiece(6, y) == EMPTY && b.getPiece(7, y) == (ROOK * 10 + color) && !isChessAfterMove(b, x, y, x + 1, y)) {
					addValid(b, x, y, list, x + 2, y); // petit roc
				}
			}
			break;
		}
		return list;
	}

	public static enum ChessStatus {
		NONE, CHESS, MAT, PAT
	}

	protected static ChessStatus checkForStatus(ChessBoard b, int color) {
		ChessStatus chessStatus = ChessStatus.NONE;
		if (b.getRenderer() != null) {
			b.getRenderer().clearSelection();
		}
		if (isChess(b, color)) {
			int kingPos[] = b.getKingXY(color);
			if (b.getRenderer() != null) {
				b.getRenderer().highlight(kingPos[0], kingPos[1], HighlightMode.CHESS);
			}
			// On commence par vérifier si le roi peut bouger
			ArrayList<int[]> kingMove = getAllowedMoves(b, kingPos[0], kingPos[1]);
			if (kingMove.size() == 0) {
				// Test du echec et mat!!
				if (isMat(b, color)) {
					chessStatus = ChessStatus.MAT;
				}
			}
		} else {
			if (isMat(b, color)) {
				chessStatus = ChessStatus.PAT;
			}
		}
		return chessStatus;
	}

	public static boolean isMat(ChessBoard b, int color) {
		// On doit tester si une autre pièce peut empècher l'echec
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (b.getColor(i, j) == color && getAllowedMoves(b, i, j).size() > 0) {
					return false;
				}
			}
		}
		return true;
	}

	private static void addValidUntilBlocked(ChessBoard b, int x, int y, ArrayList<int[]> list, int deltax, int deltay) {
		int x2 = x;
		int y2 = y;
		while (true) {
			x2 += deltax;
			y2 += deltay;
			if (!b.isOnBoard(x2, y2))
				break;
			addValid(b, x, y, list, x2, y2);
			// on s'arrete si on a rencontré une piece!!
			if (b.getPiece(x2, y2) != EMPTY)
				break;
		}
	}

	private static void addValid(ChessBoard b, int x, int y, ArrayList<int[]> list, int x2, int y2) {
		if (b.isOnBoard(x2, y2) && b.getColor(x2, y2) != b.getColor(x, y)) {
			if (!isChessAfterMove(b, x, y, x2, y2)) {
				list.add(new int[] { x2, y2 });
			}
		}
	}

	private static ArrayList<int[]> getKnightMovesArrayList(int _x, int _y) {
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

	private static boolean isChessAfterMove(ChessBoard b, int x, int y, int _x, int _y) {
		ChessBoard testingBoard = b.clone();
		testingBoard.move(x, y, _x, _y);
		return isChess(testingBoard, b.getColor(x, y));
	}

	public static boolean isChess(ChessBoard b, int color) {
		int[] kp = b.getKingXY(color);
		return isAttacked(b, kp[0], kp[1]);
	}

	private static boolean isAttacked(ChessBoard b, int x, int y) {
		boolean attacked = false;
		int color = b.getColor(x, y);
		for (int deltax = -1; deltax <= 1; deltax++) {
			for (int deltay = -1; deltay <= 1; deltay = ((deltax == 0 && deltay == -1) ? 1 : deltay + 1)) {
				int dx = x;
				int dy = y;
				boolean first = true;
				while (true) {
					dx += deltax;
					dy += deltay;
					if (!b.isOnBoard(dx, dy))
						break;
					// on s'arrete si on a rencontré une piece!!
					int p = b.getPiece(dx, dy);
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
			if (b.isOnBoard(pos[0], pos[1])) {
				if (b.getType(pos[0], pos[1]) == KNIGHT && b.getColor(pos[0], pos[1]) != color) {
					return true;
				}
			}
		}
		return false;
	}

	public static int[] searchPieceXY(ChessBoard b, int type, int color, int row_start, int col_start, int row_end, int col_end) {
		int[] foundPiece = null;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				if (b.getColor(x, y) == color && b.getType(x, y) == type) {
					// Verifie les indices de position de depart
					if (col_start > 0 && x != col_start) {
						continue;
					}
					if (row_start < 8 && y != row_start) {
						continue;
					}

					// Verifie la destination
					ArrayList<int[]> allowedMoves = getAllowedMoves(b, x, y);
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

	public static ChessBoard getBoardAfterMove(ChessBoard b, ChessMove m) {
		ChessBoard cloned = b.clone();
		cloned.move(m);
		return cloned;
	}

}
