package org.dreamsoft.chessplayer.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SourcesTableEvents;
import com.google.gwt.user.client.ui.TableListener;

/**
 * Echiquier
 * 
 * @author pcv
 * 
 */
public class ChessBoard extends Composite {

	public static final int EMPTY = 0;

	public static final int WHITE = 1;

	public static final int BLACK = 2;

	private Grid grid = new Grid(10, 10);

	private int turn = 1;

	private ChessPiece selectPiece = null;

	private ArrayList<int[]> legalMoveForselectPiece = null;

	private ChessPiece kings[] = new ChessPiece[3];

	private ArrayList<ChessMove> moveHistory = new ArrayList<ChessMove>();

	private HTML moveMessage = new HTML();

	private HTML moveLog = new HTML();

	private HTML turnLabel;

	private boolean isEnglish;

	public ChessBoard() {
		grid.setCellSpacing(0);
		initBorderStyle();
		initCaseStyle();
		grid.addTableListener(new TableListener() {
			public void onCellClicked(SourcesTableEvents sender, int y, int x) {
				ChessPiece selectCase = (ChessPiece) ((Grid) sender).getWidget(y, x);
				if (selectPiece == null) {
					if (selectCase != null && selectCase.color == getTurn()) {
						selectPiece = selectCase;
						addStyleCase(x, y, "selected");
						legalMoveForselectPiece = allowedMove(selectPiece);
						// Changer le style
						for (Iterator<int[]> iter = legalMoveForselectPiece.iterator(); iter.hasNext();) {
							int pos[] = (int[]) iter.next();
							addStyleCase(pos[0], pos[1], "legalmove");
						}
					}
				} else {
					if (selectCase != selectPiece) {
						boolean f = false;
						for (Iterator<int[]> iter = legalMoveForselectPiece.iterator(); iter.hasNext();) {
							int pos[] = (int[]) iter.next();
							if (pos[0] == x && pos[1] == y) {
								f = true;
								break;
							}
						}
						if (f) {
							movePiece(selectPiece, x, y);
							changeTurn();
						}
					}
					selectPiece = null;
					initCaseStyle();
					if (ifChess()) {
						ChessPiece king = kings[getTurn()];
						addStyleCase(king.x, king.y, "chessed");
						// On commence par vérifier si le roi peut bouger
						ArrayList<int[]> kingMove = allowedMove(king);
						if (kingMove.size() == 0) {
							// Test du echec et mat!!
							if (ifMat()) {
								showMessage((((getOponent() == WHITE) ? "les blancs" : "les noirs") + " gagnent!"));
							}
						}
					} else {
						if (ifMat()) {
							showMessage("partie nulle");
						}
					}
					selectPiece = null;
				}
			}

		});

		FlexTable table = new FlexTable();
		table.setSize("400", "400");
		table.setBorderWidth(1);
		table.setCellPadding(0);
		table.setCellSpacing(5);
		table.setWidget(0, 0, grid);
		table.getFlexCellFormatter().setRowSpan(0, 0, 2);
		moveLog.setWordWrap(false);
		ScrollPanel p1 = new ScrollPanel(moveLog);
		p1.setWidth("100");
		p1.setHeight("100%");
		turnLabel = new HTML();
		table.setWidget(0, 1, turnLabel);
		table.getCellFormatter().setHeight(0, 1, "20px");
		table.setWidget(1, 0, p1);
		table.getCellFormatter().setHeight(1, 0, "300px");
		ScrollPanel p2 = new ScrollPanel(moveMessage);
		p2.setWidth("100%");
		p2.setHeight("100");
		table.setWidget(2, 0, p2);
		table.getFlexCellFormatter().setColSpan(2, 0, 2);
		initWidget(table);
	}

	private void changeTurn() {
		setTurn(1 + (turn % 2));
	}

	private boolean ifMat() {
		// On doit tester si une autre pièce peut empècher l'echec
		for (int i = 1; i < 9; i++) {
			for (int j = 1; j < 9; j++) {
				ChessPiece piece = (ChessPiece) grid.getWidget(i, j);
				if (piece != null && piece.color == getTurn() && allowedMove(piece).size() > 0) {
					return false;
				}
			}
		}
		return true;
	}

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
		turnLabel.setText(getTurn() == WHITE ? "Blanc" : "Noir");
	}

	public int getOponent() {
		return getTurn() == WHITE ? BLACK : WHITE;
	}

	private void addStyleCase(int x, int y, String style) {
		String s = grid.getCellFormatter().getStyleName(y, x).endsWith("1") ? "1" : "2";
		grid.getCellFormatter().addStyleName(y, x, style + s);
	}

	private void initBorderStyle() {
		for (int i = 0; i < 8; i++) {
			String column = "" + new Character((char) ('a' + i));
			String row = "" + (i + 1);
			grid.setBorderWidth(2);
			grid.setText(0, i + 1, " (" + column + ") ");
			grid.getCellFormatter().setStyleName(0, i + 1, "enteteCol");
			grid.getCellFormatter().setWidth(0, i + 1, "34px");
			grid.setText(9, i + 1, " (" + column + ") ");
			grid.getCellFormatter().setStyleName(9, i + 1, "enteteCol");
			grid.setText(8 - i, 0, row);
			grid.getCellFormatter().setStyleName(8 - i, 0, "enteteRow");
			grid.setText(8 - i, 9, row);
			grid.getCellFormatter().setStyleName(8 - i, 9, "enteteRow");
		}
	}

	private void initCaseStyle() {
		int n = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				grid.getCellFormatter().setStyleName(i + 1, j + 1, "case" + (n + 1));
				n = (n + 1) % 2;
			}
			n = (n + 1) % 2;
		}
	}

	public void startNewGame() {
		isEnglish = false;
		for (int i = 1; i < 8; i++) {
			for (int j = 1; j < 8; j++) {
				grid.clearCell(j, i);
			}
		}
		int colors[] = new int[] { WHITE, BLACK };
		for (int i = 0; i < colors.length; i++) {
			int color = colors[i];
			int pawnRow = (color != WHITE) ? 2 : 7;
			int pieceRow = (color != WHITE) ? 1 : 8;
			for (int j = 0; j < 8; j++) {
				movePiece(new ChessPiece(ChessPiece.PAWN, color), j + 1, pawnRow);
			}
			int pieces[] = new int[] { ChessPiece.ROOK, ChessPiece.KNIGHT, ChessPiece.BISHOP, ChessPiece.QUEEN, ChessPiece.KING, ChessPiece.BISHOP, ChessPiece.KNIGHT, ChessPiece.ROOK };
			for (int j = 0; j < pieces.length; j++) {
				ChessPiece tmpPiece = new ChessPiece(pieces[j], color);
				movePiece(tmpPiece, 1 + j, pieceRow);
				if (pieces[j] == ChessPiece.KING) {
					kings[color] = tmpPiece;
				}
			}
		}

		// Remise à zero
		setTurn(WHITE);
		moveMessage.setHTML("");
		moveLog.setHTML("");
		moveHistory.clear();
		initCaseStyle();
		selectPiece = null;
	}

	void movePiece(ChessPiece chessPiece, int x, int y) {
		if (chessPiece == null)
			return;
		grid.remove(chessPiece);
		grid.clearCell(chessPiece.y, chessPiece.x);

		// Detection du rock pour bouger la tour
		if (chessPiece.type == ChessPiece.KING && Math.abs(chessPiece.x - x) == 2) {
			ChessPiece tour = (ChessPiece) grid.getWidget(y, x > chessPiece.x ? 8 : 1);
			// Deplacement de la tour
			movePiece(tour, x > chessPiece.x ? x - 1 : x + 1, y);
		}

		// TODO Detection du "en passant" pour supprimer le pion adverse
		// if (chessPiece.type == ChessPiece.PAWN && Math.abs(pos[1] - y) >
		// 1 && Math.abs(pos[0] - x) > 1) {
		// }

		// Enregistrement du mouvement
		moveHistory.add(new ChessMove(chessPiece, chessPiece.x, chessPiece.y, (ChessPiece) grid.getWidget(y, x), x, y));

		// Repositionnement de la piece
		grid.setWidget(y, x, chessPiece);
		chessPiece.x = x;
		chessPiece.y = y;
		chessPiece.nbMove++;

	}

	private int nextCaseColor(int _x, int _y) {
		ChessPiece ChP = (ChessPiece) grid.getWidget(_y, _x);
		if (ChP == null) {
			return EMPTY;
		} else {
			return ChP.color;
		}
	}

	private void addValid(ChessPiece _chessPiece, ArrayList<int[]> list, int _x, int _y) {
		if (isOnBoard(_x, _y)) {
			int targetColor = nextCaseColor(_x, _y);
			if (targetColor == EMPTY || targetColor != getTurn()) {
				if (!tryMoveForChess(_chessPiece, _x, _y)) {
					list.add(new int[] { _x, _y });
				}
			}
		}
	}

	private boolean tryMoveForChess(ChessPiece chessPiece, int x, int y) {
		int historySize = moveHistory.size();
		movePiece(chessPiece, x, y);
		boolean test = ifChess();
		// Dépiler la pile des coups historisés...
		while (moveHistory.size() > 0 && moveHistory.size() > historySize) {
			ChessMove cm = (ChessMove) moveHistory.get(moveHistory.size() - 1);
			cm.undoMove(grid);
			moveHistory.remove(moveHistory.size() - 1);
		}
		return test;
	}

	private ArrayList<int[]> allowedMove(ChessPiece _chessPiece) {
		int _x = _chessPiece.x;
		int _y = _chessPiece.y;
		ArrayList<int[]> list = new ArrayList<int[]>();
		int oponentColor = getOponent();

		switch (_chessPiece.type) {
		case ChessPiece.PAWN:
			int sens = (_chessPiece.color == WHITE) ? -1 : 1;
			int nexty = _y + sens;
			int nextnexty = _y + 2 * sens;

			if (nextCaseColor(_x, nexty) == EMPTY) {
				addValid(_chessPiece, list, _x, nexty);
				if (_chessPiece.nbMove == 0 && nextCaseColor(_x, nextnexty) == EMPTY) {
					addValid(_chessPiece, list, _x, nextnexty);
				}
			}
			if (nextCaseColor(_x + 1, nexty) == oponentColor) {
				addValid(_chessPiece, list, _x + 1, nexty);
			}
			if (nextCaseColor(_x - 1, nexty) == oponentColor) {
				addValid(_chessPiece, list, _x - 1, nexty);
			}

			break;
		case ChessPiece.QUEEN:
			addValidUntilBlocked(_chessPiece, list, 0, 1);
			addValidUntilBlocked(_chessPiece, list, 0, -1);
			addValidUntilBlocked(_chessPiece, list, 1, 0);
			addValidUntilBlocked(_chessPiece, list, -1, 0);
			addValidUntilBlocked(_chessPiece, list, 1, 1);
			addValidUntilBlocked(_chessPiece, list, 1, -1);
			addValidUntilBlocked(_chessPiece, list, -1, 1);
			addValidUntilBlocked(_chessPiece, list, -1, -1);

			break;
		case ChessPiece.ROOK:
			addValidUntilBlocked(_chessPiece, list, 0, 1);
			addValidUntilBlocked(_chessPiece, list, 0, -1);
			addValidUntilBlocked(_chessPiece, list, 1, 0);
			addValidUntilBlocked(_chessPiece, list, -1, 0);

			break;
		case ChessPiece.KNIGHT:
			ArrayList<int[]> mvtList = getKnightMovesArrayList(_x, _y);
			for (Iterator<int[]> iter = mvtList.iterator(); iter.hasNext();) {
				int pos[] = (int[]) iter.next();
				addValid(_chessPiece, list, pos[0], pos[1]);
			}

			break;
		case ChessPiece.BISHOP:
			// 4 diagonales, cas d'arrêt :
			// pièce adversaire avec case autorisée
			// pièce de ma couleur avec case autorisée
			// sortie de grid
			addValidUntilBlocked(_chessPiece, list, 1, 1);
			addValidUntilBlocked(_chessPiece, list, 1, -1);
			addValidUntilBlocked(_chessPiece, list, -1, 1);
			addValidUntilBlocked(_chessPiece, list, -1, -1);
			break;
		case ChessPiece.KING:
			addValid(_chessPiece, list, _x + 1, _y);
			addValid(_chessPiece, list, _x + 1, _y + 1);
			addValid(_chessPiece, list, _x + 1, _y - 1);
			addValid(_chessPiece, list, _x, _y + 1);
			addValid(_chessPiece, list, _x, _y - 1);
			addValid(_chessPiece, list, _x - 1, _y + 1);
			addValid(_chessPiece, list, _x - 1, _y - 1);
			addValid(_chessPiece, list, _x - 1, _y);
			if (kings[getTurn()].nbMove == 0) {
				ChessPiece king = kings[getTurn()];

				boolean emptyOOO = true;
				for (int i = 2; i < king.x; i++) {
					if (grid.getWidget(king.y, i) != null) {
						emptyOOO = false;
						break;
					}
				}
				ChessPiece tour1 = (ChessPiece) grid.getWidget(king.y, 1);
				if (emptyOOO && tour1 != null && tour1.type == ChessPiece.ROOK && tour1.nbMove == 0) {
					// grand roc
					addValid(_chessPiece, list, king.x - 2, king.y);
				}

				boolean emptyOO = true;
				for (int i = king.x + 1; i < 8; i++) {
					if (grid.getWidget(king.y, i) != null) {
						emptyOO = false;
						break;
					}
				}
				ChessPiece tour2 = (ChessPiece) grid.getWidget(king.y, 8);
				if (emptyOO && tour2 != null && tour2.type == ChessPiece.ROOK && tour2.nbMove == 0) {
					// petit roc
					addValid(_chessPiece, list, king.x + 2, king.y);
				}
			}
			// TODO vérifier que le roi n'a pas été mis en échec sur le
			// chemin

			break;
		}
		return list;
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

	public boolean ifChess() {
		ChessPiece king = kings[getTurn()];
		return checkKingChessLine(king.x, king.y, 0, 1) || checkKingChessLine(king.x, king.y, 0, -1) || checkKingChessLine(king.x, king.y, 1, 0) || checkKingChessLine(king.x, king.y, -1, 0) || checkKingChessLine(king.x, king.y, 1, 1) || checkKingChessLine(king.x, king.y, 1, -1) || checkKingChessLine(king.x, king.y, -1, 1) || checkKingChessLine(king.x, king.y, -1, -1) || checkKingChessKnight(king.x, king.y);
	}

	private boolean checkKingChessKnight(int _x, int _y) {
		ArrayList<int[]> mvtList = getKnightMovesArrayList(_x, _y);
		for (Iterator<int[]> iter = mvtList.iterator(); iter.hasNext();) {
			int pos[] = (int[]) iter.next();
			if (isOnBoard(pos[0], pos[1])) {
				ChessPiece pieceToCheck = (ChessPiece) grid.getWidget(pos[1], pos[0]);
				if (pieceToCheck != null && pieceToCheck.color == getOponent() && pieceToCheck.type == ChessPiece.KNIGHT) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkKingChessLine(int x, int y, int deltax, int deltay) {
		int dx = x;
		int dy = y;
		boolean first = true;
		while (true) {
			dx += deltax;
			dy += deltay;
			if (!isOnBoard(dx, dy))
				break;
			// on s'arrete si on a rencontré une piece!!
			ChessPiece pieceToCheck = (ChessPiece) grid.getWidget(dy, dx);
			if (pieceToCheck != null)
				if (pieceToCheck.color == getOponent()) {
					switch (pieceToCheck.type) {
					case ChessPiece.PAWN:
						return (first && (dx * dy != 0) && ((dy > y && getTurn() == BLACK) || (dy < y && getTurn() == WHITE)));
					case ChessPiece.KING:
						return first;
					case ChessPiece.BISHOP:
						return (deltax * deltay != 0);
					case ChessPiece.QUEEN:
						return true;
					case ChessPiece.ROOK:
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

	private void addValidUntilBlocked(ChessPiece _chessPiece, ArrayList<int[]> list, int deltax, int deltay) {
		int dx = _chessPiece.x;
		int dy = _chessPiece.y;
		while (true) {
			dx += deltax;
			dy += deltay;
			if (!isOnBoard(dx, dy))
				break;
			addValid(_chessPiece, list, dx, dy);
			// on s'arrete si on a rencontré une piece!!
			if (grid.getWidget(dy, dx) instanceof ChessPiece)
				break;
		}

	}

	public boolean isOnBoard(int _x, int _y) {
		return (_x > 0 && _x < 9 && _y > 0 && _y < 9);
	}

	/**
	 * Effectue un déplacement de partie
	 * 
	 * @param text
	 */
	public void doMove(String text) {
		String pat = "([" + (ChessPiece.PIECE_LETTERS_ENGLISH + ChessPiece.PIECE_LETTERS).replaceAll(" ", "") + "]{0,1})([a-h]{0,1})([1-8]{0,1})[x]{0,1}([a-h])([1-8])[\\+#]{0,1}";

		// TODO traiter l'exception du petit et grand rock "O-O" et "O-O-O"
		if (text.matches(pat)) {
			ArrayList<?> matches = new ArrayList<Object>();
			regexpMatches(pat, "", text, matches);
			String typePiece = (String) matches.get(1);

			// System anglophone " TCFRD" <=> " RNBKQ"
			int type = 1 + ChessPiece.PIECE_LETTERS.indexOf(typePiece);
			int typeEnglish = 1 + ChessPiece.PIECE_LETTERS_ENGLISH.indexOf(typePiece);
			if (isEnglish) {
				type = typeEnglish;
			} else {
				if (typeEnglish > 1 && type != ChessPiece.KING) {
					isEnglish = true;
					type = typeEnglish;
				}
			}
			if (type < 1) {
				type = ChessPiece.PAWN;
			}

			int col_start = " abcdefgh".indexOf((String) matches.get(2));
			int row_start = 9 - Integer.parseInt("0" + (String) matches.get(3));
			int col_end = " abcdefgh".indexOf((String) matches.get(4));
			int row_end = 9 - Integer.parseInt("0" + (String) matches.get(5));

			ChessPiece foundPiece = null;
			for (int x = 1; x < 9; x++) {
				for (int y = 1; y < 9; y++) {
					ChessPiece pieceToCheck = (ChessPiece) grid.getWidget(y, x);
					if (pieceToCheck != null && pieceToCheck.color == getTurn() && pieceToCheck.type == type) {
						// Verifie les indices de position de depart
						if (col_start > 0 && pieceToCheck.x != col_start) {
							continue;
						}
						if (row_start < 9 && pieceToCheck.y != row_start) {
							continue;
						}

						// Verifie la destination
						ArrayList<int[]> allowedMoves = allowedMove(pieceToCheck);
						for (Iterator<int[]> iter = allowedMoves.iterator(); iter.hasNext();) {
							int[] pos = (int[]) iter.next();
							if (pos[0] == col_end && pos[1] == row_end) {
								foundPiece = pieceToCheck;
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
			if (foundPiece != null) {
				movePiece(foundPiece, col_end, row_end);
				moveLog.setHTML(moveLog.getHTML() + (((getTurn() == BLACK) ? "-" : "<br>") + text));
				changeTurn();
			} else {
				showMessage("Coup impossible");
			}
		}
	}

	private native void regexpMatches(String pattern, String flags, String text, List<?> matches)/*-{
		var regExp = new RegExp(pattern, flags);
		var result = text.match(regExp);
		if (result == null) return;
		for (var i=0;i<result.length;i++)
		matches.@java.util.ArrayList::add(Ljava/lang/Object;)(result[i]);
	}-*/;

	/**
	 * Affiche un commentaire sur le coup effectué
	 * 
	 * @param text
	 */
	public void showMessage(String text) {
		moveMessage.setText(text);
	}

}
