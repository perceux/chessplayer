package org.dreamsoft.chessplayer.client;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.gwt.junit.client.GWTTestCase;

public class ChessBoardUtilsTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "org.dreamsoft.chessplayer.ChessPlayer";
	}

	@Test
	public void testMoveBoard() throws Exception {
		String board = "1234567890";
		int pos1 = 9;
		int pos2 = 4;
		String board2 = ChessBoardUtils.move(board, pos1, pos2);
		System.out.println("1=" + board);
		System.out.println("2=" + board2);
		assertNotSame(board, board2);
	}
	
	@Test
	public void testReset() throws Exception {
		ChessBoard b = new ChessBoard();
		b.reset();
		String board = b.getBoard();
		System.out.println(board);
		
	}
}
