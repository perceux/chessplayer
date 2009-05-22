package org.dreamsoft.chessplayer.client;

import java.util.ArrayList;
import java.util.List;

import org.dreamsoft.chessplayer.client.ChessBoardRenderer.HighlightMode;

import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ChessGame extends Composite implements Constantes {

	public static final String PIECE_LETTERS = " TCFRD";

	public static final String PIECE_LETTERS_ENGLISH = " RNBKQ";

	private CheckBox autoCheckbox = new CheckBox("Random");

	private ChessBoard qcb = new ChessBoard();

	private ChessBoardRenderer qcr = new ChessBoardRenderer() {
		@Override
		protected void onPromoteSelected(int x, int y, int piece) {
			qcb.setPromote(x, y, piece);
			checkForStatus();
		}

		@Override
		protected void onBoardClick(int x, int y) {
			if (qcb.getSelectedPos() == -1 && qcb.getColor(x, y) == getTurn()) {
				qcb.select(x, y);
			} else {
				if (qcb.getSelectedPos() != x + y * 10 && qcb.moveSelectedPiece(x, y)) {
					changeTurn();
					checkForStatus();
				} else {
					qcb.unselect();
				}
			}
		}
	};

	private int turn = 1;

	private HTML moveMessage = new HTML();

	private HTML moveLog = new HTML();

	private HTML turnLabel;

	private boolean isEnglish;

	public ChessGame() {
		initUI();
	}

	protected void checkForStatus() {
		if (qcb.ifChess(getTurn())) {
			int kingPos[] = qcb.getKingXY(getTurn());
			highlight(kingPos[0], kingPos[1], HighlightMode.CHESS);
			// On commence par vérifier si le roi peut bouger
			ArrayList<int[]> kingMove = qcb.getAllowedMoves(kingPos[0], kingPos[1]);
			if (kingMove.size() == 0) {
				// Test du echec et mat!!
				if (qcb.ifMat(getTurn())) {
					showMessage((((getOponent() == WHITE) ? "les blancs" : "les noirs") + " gagnent!"));
				}
			}
		} else {
			if (qcb.ifMat(getTurn())) {
				showMessage("partie nulle");
			}
		}
	}

	public ChessGame(ChessBoard qcb) {
		this.qcb = qcb;
		initUI();
	}

	public void highlight(int x, int y, HighlightMode hl) {
		if (qcr != null)
			qcr.highlight(x, y, hl);
	}

	private void initUI() {
		qcb.setRenderer(qcr);

		FlexTable table = new FlexTable();
		table.setSize("400", "400");
		table.setBorderWidth(1);
		table.setCellPadding(0);
		table.setCellSpacing(5);
		table.setWidget(0, 0, qcr.getWidget());
		table.getFlexCellFormatter().setRowSpan(0, 0, 2);
		moveLog.setWordWrap(false);
		ScrollPanel p1 = new ScrollPanel(moveLog);
		p1.setWidth("100");
		p1.setHeight("100%");
		turnLabel = new HTML();
		HorizontalPanel p = new HorizontalPanel();
		p.add(turnLabel);
		p.add(autoCheckbox);
		table.setWidget(0, 1, p);
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

	public int getTurn() {
		return turn;
	}

	public void setTurn(int turn) {
		this.turn = turn;
		qcb.unselect();
		turnLabel.setText(getTurn() == WHITE ? "Blanc" : "Noir");
		// TODO: create ComputerPlayer and HumanPlayer
		if (autoCheckbox.getValue()) {
			// Récupère les mouvements possibles
			Timer t = new Timer() {
				@Override
				public void run() {
					ArrayList<ChessMove> allMoves = qcb.getAllMoves(getTurn());
					if (allMoves.size() > 0) {
						// Choix aléatoire
						int r = Random.nextInt(allMoves.size() - 1);
						qcb.move(allMoves.get(r));
						changeTurn();
					}
				}
			};
			t.schedule(100);
		}
	}

	public int getOponent() {
		return getTurn() == WHITE ? BLACK : WHITE;
	}

	/**
	 * Affiche un commentaire sur le coup effectué
	 * 
	 * @param text
	 */
	public void showMessage(String text) {
		moveMessage.setText(text);
	}

	public void startNewGame() {
		isEnglish = false;
		qcb.reset();

		// Remise à zero
		setTurn(WHITE);
		moveMessage.setHTML("");
		moveLog.setHTML("");
		qcb.unselect();
	}

	/**
	 * Effectue un déplacement de partie
	 * 
	 * @param text
	 */
	public void parseMove(String text) {
		String pat = "([" + (PIECE_LETTERS_ENGLISH + PIECE_LETTERS).replaceAll(" ", "") + "]{0,1})([a-h]{0,1})([1-8]{0,1})[x]{0,1}([a-h])([1-8])[\\+#]{0,1}";

		// TODO traiter l'exception du petit et grand rock "O-O" et "O-O-O"
		if (text.matches(pat)) {
			ArrayList<?> matches = new ArrayList<Object>();
			regexpMatches(pat, "", text, matches);
			String typePiece = (String) matches.get(1);

			// System anglophone " TCFRD" <=> " RNBKQ"
			int type = 1 + PIECE_LETTERS.indexOf(typePiece);
			int typeEnglish = 1 + PIECE_LETTERS_ENGLISH.indexOf(typePiece);
			if (isEnglish) {
				type = typeEnglish;
			} else {
				if (typeEnglish > 1 && type != KING) {
					isEnglish = true;
					type = typeEnglish;
				}
			}
			if (type < 1) {
				type = PAWN;
			}

			int col_start = -1 + " abcdefgh".indexOf((String) matches.get(2));
			int row_start = 8 - Integer.parseInt("0" + (String) matches.get(3));
			int col_end = -1 + " abcdefgh".indexOf((String) matches.get(4));
			int row_end = 8 - Integer.parseInt("0" + (String) matches.get(5));

			if (qcb.moveTo(type, getTurn(), row_start, col_start, row_end, col_end)) {
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

}
