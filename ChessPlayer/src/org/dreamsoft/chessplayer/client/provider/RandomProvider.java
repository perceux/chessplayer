package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;

import org.dreamsoft.chessplayer.client.ChessMove;

import com.google.gwt.user.client.Random;

public class RandomProvider extends Provider {
	
	public RandomProvider() {
		setAuto(true);
	}
	
	@Override
	public ChessMove getNextMove(int color) {
		ArrayList<ChessMove> allMoves = getChessBoard().getAllMoves(color);
		if (allMoves.size() > 0) {
			// Choix aléatoire
			int r = Random.nextInt(allMoves.size() - 1);
			return allMoves.get(r);
		}
		return null;
	}

}
