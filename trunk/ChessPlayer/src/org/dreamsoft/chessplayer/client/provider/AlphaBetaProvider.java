package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;
import java.util.Vector;

import org.dreamsoft.chessplayer.client.ChessMove;

import com.google.gwt.user.client.Random;

public class AlphaBetaProvider extends Provider {

	private Vector<String> stringMoves = new Vector<String>();

	private int maxDepth = 5;

	private int nb;

	@Override
	public ChessMove getNextMove(int color) {
		nb = 0;
		alphaBeta(chessBoard.getBoard(), -Integer.MAX_VALUE, Integer.MAX_VALUE, maxDepth, color);
		ChessMove result = null;
		if (!stringMoves.isEmpty()) {
			result = toChessMove(stringMoves.get(Random.nextInt(stringMoves.size() - 1)));
		}
		System.out.println(nb + " evaluations");
		return result;
	}

	@Override
	public boolean isAuto() {
		return true;
	}

	@Override
	public String getShortName() {
		return "AlphaBeta";
	}

	public ChessMove toChessMove(String sMove) {
		return new ChessMove(sMove.charAt(0), sMove.charAt(1) % 8, sMove.charAt(1) / 8, sMove.charAt(2), sMove.charAt(3) % 8, sMove.charAt(3) / 8);
	}

	// A < B
	public int alphaBeta(String board, int a, int b, int depth, int color) {
		if (depth == 0) {
			nb++;
			return evaluate(board, color);
		} else {
			int best = -Integer.MAX_VALUE;
			for (String childBoard : getChildBoards(board, color)) {
				int val = -alphaBeta(childBoard, -b, -a, depth - 1, 3 - color);
				if (val > best) {
					best = val;
					if (best > a) {
						if (depth == maxDepth) {
							stringMoves.clear();
							stringMoves.add(lastMove(childBoard));
						}
						a = best;
						if (a > b) {
							if (depth == maxDepth) {
								stringMoves.clear();
								stringMoves.add(lastMove(childBoard));
							}
							break;
						} else if (a == best && depth == maxDepth) {
							stringMoves.add(lastMove(childBoard));
						}
					} else if (a == best && depth == maxDepth) {
						stringMoves.add(lastMove(childBoard));
					}
				}
			}
			return best;
		}
	}

	public static String move(String board, int pos1, int pos2) {
		char piece = (char) ((board.charAt(pos1) % 100) + 100);
		String lastMove = "" + board.charAt(pos1) + (char) pos1 + board.charAt(pos2) + (char) pos2;
		// Stocker le lastMove dans le board pour pouvoir tester le "en passant"
		if (pos2 > pos1) {
			return board.substring(0, pos1) + EMPTY + board.substring(pos1 + 1, pos2) + piece + board.substring(pos2 + 1, 64) + lastMove; // lastmove
		} else if (pos1 > pos2) {
			return board.substring(0, pos2) + piece + board.substring(pos2 + 1, pos1) + EMPTY + board.substring(pos1 + 1, 64) + lastMove; // lastmove
		}
		return board;
	}

	private static int[][] knightMovesDelta = new int[][] { { +2, 1 }, { +2, 1 }, { -2, 1 }, { -2, 1 }, { +1, 2 }, { +1, 2 }, { -1, 2 }, { -1, 2 } };

	private static ArrayList<String> getChildBoards(String b, int color) {
		ArrayList<String> childs = new ArrayList<String>();
		// Add legal move board
		for (int i = 0; i < 64; i++) {
			int p = b.charAt(i);
			if ((p % 10) == color) {
				switch (p % 100 / 10) {

				case PAWN:
					int step = (color == WHITE) ? -1 : 1;

					if (b.charAt(i + step * 8) == EMPTY) {
						addCheckedBoard(childs, b, i, 0, step);
						if (p < 100 && b.charAt(i + step * 2 * 8) == EMPTY) {
							addCheckedBoard(childs, b, i, 0, step * 2);
						}
					}
					if (i % 8 < 7 && b.charAt(i + 1 + step * 8) == 3 - color) {
						addCheckedBoard(childs, b, i, 1, step);
					}
					if (i % 8 > 0 && b.charAt(i - 1 + step) == 3 - color) {
						addCheckedBoard(childs, b, i, -1, step);
					}

					// En passant int enPassantY = (color == WHITE) ? 3 : 4;
					// if (y == enPassantY) {
					// String lastMove = lastMove(childBoard);
					// if (lastMove.fromPiece % 100 == PAWN 10 + oponentColor &&
					// lastMove.toY == enPassantY &&
					// Math.abs(lastMove.fromY - lastMove.toY) > 1) {
					// if (lastMove.toX == x - 1) { addValid(b, x, y, list, x -
					// 1, y1); }
					// else if (lastMove.toX == x + 1) {
					// addValid(b, x, y, list, x + 1, y1);
					// }
					// }
					// }
					// }
					break;
				case QUEEN:
					addCheckedBoard(childs, b, i, 0, 1, true);
					addCheckedBoard(childs, b, i, 0, -1, true);
					addCheckedBoard(childs, b, i, 1, 0, true);
					addCheckedBoard(childs, b, i, -1, 0, true);
					addCheckedBoard(childs, b, i, 1, 1, true);
					addCheckedBoard(childs, b, i, 1, -1, true);
					addCheckedBoard(childs, b, i, -1, 1, true);
					addCheckedBoard(childs, b, i, -1, -1, true);

					break;
				case ROOK:
					addCheckedBoard(childs, b, i, 0, 1, true);
					addCheckedBoard(childs, b, i, 0, -1, true);
					addCheckedBoard(childs, b, i, 1, 0, true);
					addCheckedBoard(childs, b, i, -1, 0, true);

					break;
				case KNIGHT:
					for (int[] d : knightMovesDelta) {
						addCheckedBoard(childs, b, i, d[0], d[1]);
					}
					break;
				case BISHOP:
					// 4 diagonales, cas d'arrêt :
					// pièce adversaire avec case autorisée
					// pièce de ma couleur avec case autorisée
					// sortie de grid
					addCheckedBoard(childs, b, i, 1, 1, true);
					addCheckedBoard(childs, b, i, 1, -1, true);
					addCheckedBoard(childs, b, i, -1, 1, true);
					addCheckedBoard(childs, b, i, -1, -1, true);
					break;
				case KING:
					addCheckedBoard(childs, b, i, 1, 0);
					addCheckedBoard(childs, b, i, 1, 1);
					addCheckedBoard(childs, b, i, 1, -1);
					addCheckedBoard(childs, b, i, 0, +1);
					addCheckedBoard(childs, b, i, 0, -1);
					addCheckedBoard(childs, b, i, -1, +1);
					addCheckedBoard(childs, b, i, -1, -1);
					addCheckedBoard(childs, b, i, -1, 0);

					// T0,1,2,3,R4,5,6,T7
					if (p < 100) { // O-O-O
						if (b.charAt(i - 3) == EMPTY && b.charAt(i - 2) == EMPTY && b.charAt(i - 1) == EMPTY && b.charAt(i - 4) == ROOK * 10 + color && !isChess(move(b, i, i - 1), color)) {
							addCheckedBoard(childs, move(b, i - 4, i - 1), i, -2, 0); // grand
							// roc
						} // O-O
						if (b.charAt(i + 1) == EMPTY && b.charAt(i + 2) == EMPTY && b.charAt(i + 3) == ROOK * 10 + color && !isChess(move(b, i, i + 1), color)) {
							addCheckedBoard(childs, move(b, i + 3, i + 1), i, +2, 0); // petit
							// roc
						}
					}
					break;
				}
			}
		}

		return childs;
	}

	private static String lastMove(String b) {
		return b.substring(64);
	}

	private static void addCheckedBoard(final ArrayList<String> childs, String b, int i, int dx, int dy, boolean repeat) {
		if (isOnBoard(i % 8 + dx, i / 8 + dy)) {
			int i2 = i + dx + dy * 8;
			if ((b.charAt(i2) % 10) != (b.charAt(i) % 10)) {
				String bam = move(b, i, i2);
				if (!isChess(bam, b.charAt(i) % 10)) {
					childs.add(bam);
				}
				if (repeat && b.charAt(i2) == EMPTY) {
					addCheckedBoard(childs, bam, i2, dx, dy, repeat);
				}
			}
		}
	}

	private static void addCheckedBoard(ArrayList<String> childs, String b, int i, int dx, int dy) {
		addCheckedBoard(childs, b, i, dx, dy, false);
	}

	// A tester :)
	private static boolean isChess(String bam, int color) {
		int kp = bam.substring(0, 64).indexOf(KING * 10 + color);
		return kp >= 0 && kp < 65 && isAttacked(bam, kp % 8, kp / 8, color);
	}

	private static boolean isAttacked(String b, int x, int y, int color) {
		boolean attacked = false;
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
					int p = b.charAt(dx + dy * 8);
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
		for (int[] d : knightMovesDelta) {
			if (isOnBoard(x + d[0], y + d[1])) {
				if (b.charAt(x + d[0] + (y + d[1]) * 8) == KNIGHT * 10 + color) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isOnBoard(int dx, int dy) {
		return (dx >= 0 && dx < 8 && dy >= 0 && dy < 8);
	}

	/**
	 * public static final int PAWN = 1; public static final int ROOK = 2;
	 * public static final int KNIGHT = 3; public static final int BISHOP = 4;
	 * public static final int KING = 5; public static final int QUEEN = 6;
	 */
	public static int pieceValue[] = { 0, 1, 5, 3, 3, 1, 10 };

	private static int evaluate(String b, int color) {
		int result = 0;
		for (int i = 0; i < 64; i++) {
			int tmp = b.charAt(i);
			if ((tmp % 10) == color)
				result += pieceValue[(tmp % 100) / 10];
		}
		return result;
	}
}
