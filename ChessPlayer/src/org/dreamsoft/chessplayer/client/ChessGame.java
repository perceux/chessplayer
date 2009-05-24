package org.dreamsoft.chessplayer.client;

import java.util.ArrayList;

import org.dreamsoft.chessplayer.client.ChessBoardRenderer.HighlightMode;
import org.dreamsoft.chessplayer.client.provider.FileProvider;
import org.dreamsoft.chessplayer.client.provider.Provider;
import org.dreamsoft.chessplayer.client.provider.ProviderListener;
import org.dreamsoft.chessplayer.client.provider.SelectableProvider;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ChessGame extends Composite implements Constantes {

	private ProviderListener providerListener = new ProviderListener() {
		public void onGameCommand(Provider provider, GameCommand t) {
			switch (t) {
			case START:
				startNewGame();
				break;
			case END:
				ChessMove move = null;
				while (true) {
					move = provider.getNextMove(getTurn());
					if (move == null)
						break;
					board.move(move);
					changeTurn();
				}
				playTurn(provider);
				break;
			case PLAY:
				playTurn(provider);
				break;
			case MESSAGE:
				showMessage(provider.getMessage());
				break;
			default:
				break;
			}
		}

		public void onProviderChange(Provider provider) {
			toolbars.clear();
			if (provider.getToolbarPanel() != null) {
				toolbars.add(provider.getToolbarPanel());
			}
			if (provider instanceof FileProvider) {
				for (int color : new int[] { WHITE, BLACK }) {
					selectableProvider[color].setProvider(provider);
				}
			}
		}
	};

	private SelectableProvider[] selectableProvider = new SelectableProvider[] { null, new SelectableProvider(providerListener), new SelectableProvider(providerListener) };

	private HTML turnLabel[] = new HTML[] { null, new HTML("White"), new HTML("Black") };

	private ChessBoard board = new ChessBoard();

	private FlowPanel toolbars = new FlowPanel();

	private ChessBoardRenderer renderer = new ChessBoardRenderer() {
		@Override
		protected void onPromoteSelected(int x, int y, int piece) {
			board.setPromote(x, y, piece);
			checkForStatus();
		}

		@Override
		protected void onBoardClick(int x, int y) {
			if (board.getSelectedPos() == -1 && board.getColor(x, y) == getTurn()) {
				board.select(x, y);
			} else {
				if (board.getSelectedPos() != x + y * 10 && board.moveSelectedPiece(x, y)) {
					changeTurn();
					checkForStatus();
				} else {
					board.unselect();
				}
			}
		}
	};

	private int turn = 1;

	private HTML moveMessage = new HTML();

	private HTML moveLog = new HTML();

	public ChessGame() {
		initUI();
	}

	protected void checkForStatus() {
		if (board.isChess(getTurn())) {
			int kingPos[] = board.getKingXY(getTurn());
			if (renderer != null)
				renderer.highlight(kingPos[0], kingPos[1], HighlightMode.CHESS);
			// On commence par vérifier si le roi peut bouger
			ArrayList<int[]> kingMove = board.getAllowedMoves(kingPos[0], kingPos[1]);
			if (kingMove.size() == 0) {
				// Test du echec et mat!!
				if (board.isMat(getTurn())) {
					showMessage((((getTurn() == WHITE) ? "White" : "Black") + " MAT !!"));
				}
			}
		} else {
			if (board.isMat(getTurn())) {
				showMessage("PAT !!");
			}
		}
	}

	private void initUI() {
		board.setRenderer(renderer);
		int row = 0;
		FlexTable table = new FlexTable();
		table.setSize("400", "400");
		table.setBorderWidth(1);
		table.setCellPadding(0);
		table.setCellSpacing(5);
		table.setWidget(row, 0, toolbars);
		table.getFlexCellFormatter().setColSpan(row, 0, 2);
		row++;
		table.setWidget(row, 0, renderer.getWidget());
		table.getFlexCellFormatter().setRowSpan(row, 0, 2);
		moveLog.setWordWrap(false);
		ScrollPanel p1 = new ScrollPanel(moveLog);
		p1.setWidth("100");
		p1.setHeight("100%");
		Grid p = new Grid(2, 2);
		int j = 0;
		for (int i : new int[] { WHITE, BLACK }) {
			p.setWidget(j, 0, turnLabel[i]);
			p.setWidget(j++, 1, selectableProvider[i].getListBox());
			selectableProvider[i].setChessBoard(board);
		}
		table.setWidget(row, 1, p);
		table.getCellFormatter().setHeight(row, 1, "20px");
		row++;
		table.setWidget(row, 0, p1);
		table.getCellFormatter().setHeight(row, 0, "300px");
		ScrollPanel p2 = new ScrollPanel(moveMessage);
		p2.setWidth("100%");
		p2.setHeight("100");
		row++;
		table.setWidget(row, 0, p2);
		table.getFlexCellFormatter().setColSpan(row, 0, 2);
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
		turnLabel[getTurn()].setStyleName("turnSelected");
		turnLabel[getOpponentTurn()].setStyleName("turnUnselected");
		if (selectableProvider[getTurn()].isAuto()) {
			playTurn(selectableProvider[getTurn()]);
		}
	}

	private void playTurn(Provider provider) {
		board.unselect();
		final ChessMove nextMove = provider.getNextMove(getTurn());
		System.out.println("[" + provider + "] move=" + nextMove);
		if (nextMove != null) {
			if (nextMove.text != null) {
				moveLog.setHTML(moveLog.getHTML() + (((getTurn() == BLACK) ? "-" : "<br>") + nextMove.text));
			}
			board.move(nextMove);
			changeTurn();
		}
	}

	public int getOpponentTurn() {
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
		// Remise à zero
		setTurn(WHITE);
		moveMessage.setHTML("");
		moveLog.setHTML("");
		board.unselect();
		board.reset();
	}

}
