package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;
import java.util.Iterator;

import org.dreamsoft.chessplayer.client.ChessBoardUtils;
import org.dreamsoft.chessplayer.client.ChessMove;
import org.dreamsoft.chessplayer.client.ChessBoardRenderer.HighlightMode;
import org.dreamsoft.chessplayer.client.provider.ProviderListener.GameCommand;

import com.google.gwt.user.client.Command;

public class HumanProvider extends Provider {

	private ArrayList<int[]> legalMoveForselectPiece = null;

	private int startPos = -1;

	private int endPos = -1;

	private ChessMove currentMove = null;

	public void unselect() {
		startPos = -1;
		endPos = -1;
		if (chessBoard.getRenderer() != null) {
			chessBoard.getRenderer().clearSelection();
		}
	}

	@Override
	public ChessMove getNextMove(int color) {
		unselect();
		return currentMove;
	}

	public void boardClick(int x, int y, int color) {
		if (startPos == -1 && chessBoard != null && chessBoard.getColor(x, y) == color) {
			startPos = x + y * 10;
			if (chessBoard.getRenderer() != null) {
				chessBoard.getRenderer().highlight(x, y, HighlightMode.SELECTED);
				legalMoveForselectPiece = ChessBoardUtils.getAllowedMoves(chessBoard, x, y);
				// Changer le style
				for (Iterator<int[]> iter = legalMoveForselectPiece.iterator(); iter.hasNext();) {
					int pos[] = (int[]) iter.next();
					chessBoard.getRenderer().highlight(pos[0], pos[1], HighlightMode.LEGAL);
				}
			}
		} else {
			endPos = x + y * 10;
			if (startPos != endPos) {
				currentMove = null;
				for (Iterator<int[]> iter = legalMoveForselectPiece.iterator(); iter.hasNext();) {
					int pos[] = (int[]) iter.next();
					if (pos[0] == x && pos[1] == y) {
						currentMove = chessBoard.getMove(startPos % 10, startPos / 10, endPos % 10, endPos / 10);
						if (currentMove.fromPiece % 100 / 10 == PAWN && (currentMove.toY == 0 || currentMove.toY == 7)) {
							chessBoard.getRenderer().promote(currentMove, new Command() {
								public void execute() {
									fireGameCommand(GameCommand.PLAY);
								}
							});
						} else {
							fireGameCommand(GameCommand.PLAY);
						}
						return;
					}
				}
			}
			unselect();
		}
	}

	@Override
	public String getShortName() {
		return "Human";
	}

}
