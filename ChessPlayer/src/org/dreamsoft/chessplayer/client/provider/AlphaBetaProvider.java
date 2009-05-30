package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;
import java.util.Vector;

import org.dreamsoft.chessplayer.client.ChessMove;
import org.dreamsoft.chessplayer.client.provider.ProviderListener.GameCommand;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class AlphaBetaProvider extends Provider {

	private Vector<String> bestBoards = new Vector<String>();

	private int maxDepth = 3;

	private int nb;

	private int color;

	@Override
	public ChessMove getNextMove(int color) {
		nb = 0;
		setColor(color);
		alphaBeta(chessBoard.getBoard(), -Integer.MAX_VALUE, Integer.MAX_VALUE, getMaxDepth(), color);

		ChessMove result = null;
		if (!bestBoards.isEmpty()) {
			result = toChessMove(lastMove(bestBoards.get(Random.nextInt(bestBoards.size() - 1))));
		}
		debug.setHTML(nb + " evaluations");
		// System.out.println(nb + " evaluations");
		return result;
	}

	private void setColor(int color) {
		this.color = color;
	}

	@Override
	public boolean isAuto() {
		return autoPlay.getValue();
	}

	private int getMaxDepth() {
		String depth = depthTextBox.getValue();
		int result = maxDepth;
		try {
			result = Integer.parseInt(depth);
		} catch (Exception e) {
			depthTextBox.setValue("" + maxDepth);
		}
		return result;
	}

	@Override
	public String getShortName() {
		return "AlphaBeta";
	}

	public ChessMove toChessMove(String sMove) {
		if (sMove == null || sMove.length() == 0)
			return null;
		return new ChessMove(sMove.charAt(0), sMove.charAt(1) % 8, sMove.charAt(1) / 8, sMove.charAt(2), sMove.charAt(3) % 8, sMove.charAt(3) / 8);
	}

	// A < B
	public int alphaBeta(String board, int a, int b, int depth, int color) {
		int current;
		if (depth <= 0) {
			nb++;
			current = evaluate(board, color);
		} else {
			current = -Integer.MAX_VALUE;
			for (String childBoard : getChildBoards(board, color)) {
				int score = -alphaBeta(childBoard, -b, -a, depth - 1, 3 - color);
				if (score >= current) {
					if (depth == maxDepth) {
						if (score > current) {
							bestBoards.clear();
							debugBoards.clear();
						}
						debugBoards.add(new HTML(boardToHTML(childBoard, " score=" + current + " move=" + toChessMove(lastMove(childBoard)))));
						bestBoards.add(childBoard);
					}
					current = score;
					if (score >= a) {
						a = score;
						if (score >= b) {
							break;
						}
					}
				}
			}
		}
		return current;
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

	private static int[][] knightMovesDelta = new int[][] { { -2, 1 }, { -1, 2 }, { 1, 2 }, { 2, 1 }, { 2, -1 }, { 1, -2 }, { -1, -2 }, { -2, -1 } };

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
					if (i % 8 < 7 && b.charAt(i + 1 + step * 8) % 10 == 3 - color) {
						addCheckedBoard(childs, b, i, 1, step);
					}
					if (i % 8 > 0 && b.charAt(i - 1 + step * 8) % 10 == 3 - color) {
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
		int x = i % 8;
		int y = i / 8;
		int color = b.charAt(i) % 10;
		boolean exit = true;
		do {
			exit = true;
			x += dx;
			y += dy;
			if (isOnBoard(x, y)) {
				int i2 = x + (y) * 8;
				if ((b.charAt(i2) % 10) != (color)) {
					String bam = move(b, i, i2);
					if (!isChess(bam, color)) {
						childs.add(bam);
					}
					if (repeat && b.charAt(i2) == EMPTY) {
						exit = false;
					}
				}
			}
		} while (!exit);
	}

	private static void addCheckedBoard(ArrayList<String> childs, String b, int i, int dx, int dy) {
		addCheckedBoard(childs, b, i, dx, dy, false);
	}

	// A tester :)
	private static boolean isChess(String bam, int color) {
		int kp1 = bam.substring(0, 64).indexOf(KING * 10 + color);
		int kp2 = bam.substring(0, 64).indexOf(100 + KING * 10 + color);
		int kp = (kp1 >= 0 && kp1 < 65) ? kp1 : kp2;
		return kp >= 0 && kp < 65 && isAttacked(bam, kp % 8, kp / 8, color);
	}

	private static boolean isAttacked(String b, int x, int y, int color) {
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
							int t = (p % 100) / 10;
							if (first && (t == KING || (t == PAWN && (deltax * deltay != 0) && ((dy > y && color == BLACK) || (dy < y && color == WHITE)))) || (t == BISHOP && (deltax * deltay != 0))
									|| t == QUEEN || (t == ROOK && (deltax * deltay == 0)))
								return true;
						}
						first = false;
						break;
					}
					first = false;
				}
			}
		}
		for (int[] d : knightMovesDelta) {
			if (isOnBoard(x + d[0], y + d[1])) {
				if (b.charAt(x + d[0] + (y + d[1]) * 8) % 100 == KNIGHT * 10 + (3 - color)) {
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
			if ((tmp % 10) == 3 - color)
				result -= pieceValue[(tmp % 100) / 10];
		}
		if (evalDebug.getValue())
			debugBoards.add(new HTML(boardToHTML(b, "color=" + color + " eval=" + result)));
		return result;
	}

	TextBox depthTextBox = new TextBox();

	HTML debug = new HTML();

	private CheckBox autoPlay = new CheckBox("auto", true);

	private static CheckBox evalDebug = new CheckBox("evalDebug", true);

	private static FlowPanel debugBoards = new FlowPanel();

	@Override
	public HorizontalPanel getToolbarPanel() {
		if (toolbarPanel == null) {
			autoPlay.setValue(true);
			depthTextBox.setValue("" + getMaxDepth());
			toolbarPanel = new HorizontalPanel();
			toolbarPanel.add(new HTML("Depth:"));
			toolbarPanel.add(depthTextBox);
			toolbarPanel.add(autoPlay);
			toolbarPanel.add(evalDebug);
			toolbarPanel.add(new Button("Moves", new ClickHandler() {
				public void onClick(ClickEvent event) {
					debugBoards.clear();
					for (String childBoard : getChildBoards(chessBoard.getBoard(), BLACK)) {
						debugBoards.add(new HTML(boardToHTML(childBoard, "score=" + evaluate(childBoard, BLACK))));
					}
				}
			}));

			toolbarPanel.add(new Button("Bests", new ClickHandler() {
				public void onClick(ClickEvent event) {
					debugBoards.clear();
					for (String childBoard : bestBoards) {
						debugBoards.add(new HTML(boardToHTML(childBoard, "best score=" + evaluate(childBoard, BLACK))));
					}
				}
			}));
			// TODO Remove le debug à l'arrache
			RootPanel.get().add(debugBoards);

			Button buttonPlay = new Button("&gt;", new ClickHandler() {
				public void onClick(ClickEvent event) {
					fireGameCommand(GameCommand.PLAY);
				}
			});
			toolbarPanel.add(buttonPlay);

			toolbarPanel.add(new HTML(" Debug:"));
			toolbarPanel.add(debug);
		}
		return toolbarPanel;
	}

	public static String boardToHTML(String b, String title) {
		String html = "<table border=1 cellspacing=0 cellpadding=0>";
		html += "<tr><td colspan=8 style='font-size:8px;'>" + title + "</td></tr>";
		int lastMoveFrom = -1;
		int lastMoveTo = -1;
		if (b.length() > 67) {
			lastMoveFrom = b.charAt(65);
			lastMoveTo = b.charAt(67);
		}
		for (int i = 0; i < 8; i++) {
			html += "<tr>";
			for (int j = 0; j < 8; j++) {
				int c = b.charAt(j + i * 8) % 100;
				boolean hl = (j + i * 8 == lastMoveFrom || j + i * 8 == lastMoveTo);
				html += "<td" + (hl ? " style='background-color:green;'" : " style='width:16px'") + "><img style='width:16px;height:16px' src='http://localhost:8080/images/" + c + ".gif'></td>";
			}
			html += "</tr>";
		}
		html += "</table><br>";
		return html;
	}

	public int getColor() {
		return color;
	}

}
