package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;

import org.dreamsoft.chessplayer.client.ChessBoard;
import org.dreamsoft.chessplayer.client.ChessBoardUtils;
import org.dreamsoft.chessplayer.client.ChessMove;
import org.dreamsoft.chessplayer.client.Constantes;

import com.google.gwt.junit.client.GWTTestCase;

public class AlphaBetaProviderTest extends GWTTestCase {
	@Override
	public String getModuleName() {
		return "org.dreamsoft.chessplayer.ChessPlayer";
	}

	public void testGetAllChildBoards() throws Exception {
		AlphaBetaProvider abp = new AlphaBetaProvider();
		ChessBoard cb = new ChessBoard();
		cb.reset();
		String moves[] = new String[] { "e4", "f7f5" };
		int color = Constantes.WHITE;
		for (String m : moves) {
			ChessMove cm = ChessBoardUtils.parseMove(m, false, cb, color);
			color = 3 - color;
			if (cm != null) {
				cb.move(cm);
			} else {
				System.out.println("error:" + cm);
			}
		}

		String toHTML = AlphaBetaProvider.boardToHTML(cb.getBoard(), "test");
		System.out.println(toHTML);
		abp.setChessBoard(cb);
		ArrayList<String> childBoards = AlphaBetaProvider.getChildBoards(cb.getBoard(), color);
		int i = 0;
		for (String board : childBoards) {
			System.out.println(AlphaBetaProvider.boardToHTML(board, "" + (++i)));
		}
	}
}
