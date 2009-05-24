package org.dreamsoft.chessplayer.client;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class ChessBoardRenderer implements Constantes {

	public enum HighlightMode {
		SELECTED, UNSELECTED, LEGAL, CHESS, MAT, PAT
	}

	private Grid grid = new Grid(10, 10);

	private ArrayList<int[]> highlighted = new ArrayList<int[]>();

	public ChessBoardRenderer() {
		grid.setCellSpacing(0);
		initBorderStyle();
		initCaseStyle();
		grid.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				Cell cell = grid.getCellForEvent(event);
				if (cell == null)
					return;
				int y = cell.getRowIndex() - 1;
				int x = cell.getCellIndex() - 1;
				onBoardClick(x, y);
			}
		});
	}

	protected void onBoardClick(int x, int y) {
	}

	public void clearSelection() {
		for (int[] pos : highlighted) {
			String s = grid.getCellFormatter().getStyleName(pos[1] + 1, pos[0] + 1).endsWith("1") ? "1" : "2";
			grid.getCellFormatter().setStyleName(pos[1] + 1, pos[0] + 1, "case" + s);
		}
		highlighted.clear();
	}

	private void addStyleCase(int x, int y, String style) {
		String s = grid.getCellFormatter().getStyleName(y + 1, x + 1).endsWith("1") ? "1" : "2";
		grid.getCellFormatter().addStyleName(y + 1, x + 1, style + s);
		highlighted.add(new int[] { x, y });
	}

	private void initBorderStyle() {
		for (int i = 0; i < 8; i++) {
			String column = "" + new Character((char) ('a' + i));
			String row = "" + (i + 1);
			grid.setBorderWidth(2);
			grid.setText(0, i + 1, " (" + column + ") ");
			grid.getCellFormatter().setStyleName(0, i + 1, "enteteCol");
			grid.getColumnFormatter().setWidth(i + 1, "34px");
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

	public void render(int x, int y, int value) {
		Widget i = grid.getWidget(y + 1, x + 1);
		if (i != null && i instanceof Image) {
			((Image) i).setUrl("images/" + value % 100 + ".gif");
		} else {
			i = makeImage(value);
		}
		grid.setWidget(y + 1, x + 1, i);
	}

	public void highlight(int x, int y, HighlightMode hl) {
		switch (hl) {
		case SELECTED:
			addStyleCase(x, y, "selected");
			break;
		case LEGAL:
			addStyleCase(x, y, "legalmove");
			break;
		case CHESS:
			addStyleCase(x, y, "chessed");
			break;
		case UNSELECTED:
			break;
		default:
			break;
		}
	}

	public void setPromote(final int x, final int y, final int color) {
		final DialogBox promoteDialog = new DialogBox(true, true);
		promoteDialog.setTitle("Promotion");
		Grid f = new Grid(2, 4);
		f.setCellSpacing(4);
		int pieceChoice[] = new int[] { QUEEN, BISHOP, KNIGHT, ROOK };
		RadioButton group[] = new RadioButton[4];
		for (int i = 0; i < pieceChoice.length; i++) {
			f.setWidget(0, i, makeImage(pieceChoice[i] * 10 + color));
			RadioButton r = new RadioButton("type");
			r.setFormValue("" + pieceChoice[i]);
			final int p = pieceChoice[i];
			r.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					onPromoteSelected(x, y, p * 10 + color);
					if (promoteDialog.isShowing())
						promoteDialog.hide();
				};
			});
			f.setWidget(1, i, r);
			f.getCellFormatter().setHorizontalAlignment(1, i, HasAlignment.ALIGN_CENTER);
			group[i] = r;
		}

		promoteDialog.setWidget(f);
		promoteDialog.show();
		promoteDialog.center();

	}

	protected void onPromoteSelected(int x, int y, int piece) {
	}

	private Image makeImage(int value) {
		Image i = new Image("images/" + value % 100 + ".gif");
		i.setSize("32px", "32px");
		return i;
	}

	public Widget getWidget() {
		return grid;
	}

}
