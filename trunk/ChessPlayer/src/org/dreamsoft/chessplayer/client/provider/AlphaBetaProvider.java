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
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class AlphaBetaProvider extends Provider {

	private Vector<String> bestBoards = new Vector<String>();

	private int maxDepth = 2;

	private int nb;

	private int col;

	private TreeItem root = new TreeItem();

	private Tree debugTree = new Tree();

	@Override
	public ChessMove getNextMove(int color) {
		nb = 0;
		setColor(color);
		bestBoards.clear();
		debugBoards.clear();
		// negascout(root, chessBoard.getBoard(), -Integer.MAX_VALUE,
		// Integer.MAX_VALUE, getMaxDepth(), color);
		negascout(chessBoard.getBoard(), -Integer.MAX_VALUE, Integer.MAX_VALUE, maxDepth, color);
		debugTree.addItem(root);
		debugBoards.clear();
		debugBoards.add(debugTree);

		ChessMove result = null;
		if (!bestBoards.isEmpty()) {
			result = toChessMove(lastMove(bestBoards.get(Random.nextInt(bestBoards.size() - 1))));
		}
		debug.setHTML(nb + " evaluations");
		// System.out.println(nb + " evaluations");
		return result;
	}

	private void setColor(int color) {
		this.col = color;
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

	public int negascout(String board, int alpha, int beta, int depth, int color) {
		if (depth == 0) {
			nb++;
			return evaluate(board, 3-color);
		}
		int a = alpha;
		int b = beta;
		int i = 0;
		ArrayList<String> children = getChildBoards(board, color);
		for (String child : children) {
			int t = -negascout(child, -b, -a, depth - 1, 3 - color);
			if (t >= a && t < beta && i > 0) {
				a = -negascout(child, -beta, -t, depth - 1, 3 - color);
				if (depth == maxDepth) {
					if (t > a)
						bestBoards.clear();
					bestBoards.add(child);
				}
			}
			if (t >= a) {
				a = t;
				if (depth == maxDepth) {
					if (t > a)
						bestBoards.clear();
					bestBoards.add(child);
				}
			}
			if (a >= beta) {
				return a;
			}
			b = a + 1; /* set new null window */
		}
		return a;
	}

	/**
	 * <pre>
	 * function negascout(node, depth, α, β)
	 * 	    if node is a terminal node or depth = 0
	 * 	        return the heuristic value of node
	 * 	    b := β
	 * 	    foreach child of node
	 * 	        a := -negascout (child, depth-1, -b, -α)
	 * 	        if a&gt;α
	 * 	            α := a
	 * 	        if α≥β
	 * 	            return α
	 * 	        if α≥b
	 * 	           α := -negascout(child, depth-1, -β, -α)  
	 * 	           if α≥β
	 * 	               return α
	 * 	        b := α+1             
	 * 	    return α
	 * </pre>
	 * 
	 * @param item
	 */
	public int negascout(TreeItem item, String board, int α, int β, int depth, int color) {
		if (depth == 0) {
			nb++;
			return evaluate(board, color);
		}
		int b = β; // (* initial window is (-β, -α) *)
		ArrayList<String> children = getChildBoards(board, color);
		for (String child : children) {
			TreeItem childItem = new TreeItem();
			// childItem.setHTML(boardToHTML(child, "size=" + children.size() +
			// " depth=" + (depth - 1) + " color=" + (3 - color)));
			// item.addItem(childItem);
			int a = -negascout(childItem, child, -b, -α, depth - 1, 3 - color);
			if (a >= α) {
				if (depth == maxDepth) {
					if (a > α)
						bestBoards.clear();
					bestBoards.add(child);
				}
				α = a;
			}
			if (α >= β) {
				return α;// (* Beta cut-off *)
			}
			if (α >= b) { // (* check if null-window failed high*)
				α = -negascout(childItem, child, -β, -α, depth - 1, 3 - color);
				// (* full re-search *)
				if (α >= β)
					return α;// (* Beta cut-off *)
			}
			b = α + 1;
		}
		return α;
	}

	/**
	 * <pre>
	 </pre>
	 * 
	 * @param item
	 * @param board
	 * @param a
	 * @param b
	 * @param depth
	 * @param color
	 * @return
	 */
	// A < B
	public int alphaBeta(TreeItem item, String board, int a, int b, int depth, int color) {
		int current;
		if (depth <= 0) {
			nb++;
			current = evaluate(board, color);
		} else {
			current = -Integer.MAX_VALUE;
			for (String childBoard : getChildBoards(board, color)) {
				TreeItem childItem = new TreeItem();
				int score = -alphaBeta(childItem, childBoard, -b, -a, depth - 1, 3 - color);
				childItem.setHTML(boardToHTML(childBoard, "a=" + a + " b=" + b + " current=" + current + " depth=" + (depth - 1) + " color=" + (3 - color) + " score=" + score));
				item.addItem(childItem);
				if (score >= current) {
					if (depth == maxDepth) {
						if (score > current) {
							bestBoards.clear();
							debugBoards.clear();
						}
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

	public static String setPiece(String board, int pos, char c) {
		return board.substring(0, pos) + c + board.substring(pos + 1);
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

	public static ArrayList<String> getChildBoards(String b, int color) {
		ArrayList<String> childs = new ArrayList<String>();
		// Add legal move board
		for (int i = 0; i < 64; i++) {
			int p = b.charAt(i);
			if ((p % 10) == color) {
				switch (p % 100 / 10) {

				case PAWN:
					int step = (color == WHITE) ? -1 : 1;

					// Promote
					if (i / 8 + step == 0 || i / 8 + step == 7) {
						addCheckedBoard(childs, setPiece(b, i, (char) (QUEEN * 10 + color)), i, 0, step);
						addCheckedBoard(childs, setPiece(b, i, (char) (BISHOP * 10 + color)), i, 0, step);
						addCheckedBoard(childs, setPiece(b, i, (char) (KNIGHT * 10 + color)), i, 0, step);
						addCheckedBoard(childs, setPiece(b, i, (char) (ROOK * 10 + color)), i, 0, step);
					} else {
						if (b.charAt(i + step * 8) == EMPTY) {
							addCheckedBoard(childs, b, i, 0, step);
							if (p < 100 && b.charAt(i + step * 2 * 8) == EMPTY) {
								addCheckedBoard(childs, b, i, 0, step * 2);
							}
						}
						if (i % 8 < 7 && b.charAt(i + 1 + step * 8) % 10 == (3 - color)) {
							addCheckedBoard(childs, b, i, 1, step);
						}
						if (i % 8 > 0 && b.charAt(i - 1 + step * 8) % 10 == (3 - color)) {
							addCheckedBoard(childs, b, i, -1, step);
						}

						// En passant
						/**
						 * <pre>
						 * int enPassantY = (color == WHITE) ? 3 : 4;
						 * 						if (i / 8 == enPassantY) {
						 * 							String lastMove = lastMove(b);
						 * 							if (lastMove.charAt(0) % 100 == PAWN * 10 + (3 - color) &amp;&amp; lastMove.charAt(3) / 8 == enPassantY &amp;&amp; Math.abs(lastMove.charAt(1) / 8 - lastMove.charAt(3) / 8) &gt; 1) {
						 * 								if (lastMove.charAt(3) % 8 == i % 8 - 1) {
						 * 									addCheckedBoard(childs, setPiece(b, lastMove.charAt(3), EMPTY), i, -1, step);
						 * 								} else if (lastMove.charAt(3) % 8 == i % 8 + 1) {
						 * 									addCheckedBoard(childs, setPiece(b, lastMove.charAt(3), EMPTY), i, 1, step);
						 * 								}
						 * 							}
						 * }
						 */

					}
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
		String html = "<style>td img {font-size:8px;height:16px;width:16px}</style><table border=1 cellspacing=0 cellpadding=0>";
		html += "<tr><td colspan=8>" + title + "</td></tr>";
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
				html += "<td" + (hl ? " style='background-color:green;'" : "") + "><img src='images/" + c + ".gif'></td>";
			}
			html += "</tr>";
		}
		html += "</table><br>";
		return html;
	}

	public int getColor() {
		return col;
	}

}
