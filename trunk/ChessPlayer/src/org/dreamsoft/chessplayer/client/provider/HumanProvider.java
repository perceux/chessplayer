package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;
import java.util.Iterator;

import org.dreamsoft.chessplayer.client.ChessBoardUtils;
import org.dreamsoft.chessplayer.client.ChessMove;
import org.dreamsoft.chessplayer.client.ChessBoardRenderer.HighlightMode;
import org.dreamsoft.chessplayer.client.provider.ProviderListener.GameCommand;

public class HumanProvider extends Provider {

	private ArrayList<int[]> legalMoveForselectPiece = null;

	private int startPos = -1;

	private int endPos = -1;

	public void unselect() {
		startPos = -1;
		endPos = -1;
		if (chessBoard.getRenderer() != null) {
			chessBoard.getRenderer().clearSelection();
		}
	}

	@Override
	public ChessMove getNextMove(int color) {
		ChessMove move = chessBoard.getMove(startPos % 10, startPos / 10, endPos % 10, endPos / 10);
		unselect();
		return move;
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
				for (Iterator<int[]> iter = legalMoveForselectPiece.iterator(); iter.hasNext();) {
					int pos[] = (int[]) iter.next();
					if (pos[0] == x && pos[1] == y) {
						fireGameCommand(GameCommand.PLAY);
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
