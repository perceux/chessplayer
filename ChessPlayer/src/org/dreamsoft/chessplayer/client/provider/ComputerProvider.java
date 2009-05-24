package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;

import org.dreamsoft.chessplayer.client.ChessBoard;
import org.dreamsoft.chessplayer.client.ChessMove;

import com.google.gwt.user.client.Random;

public class ComputerProvider extends Provider {

	public ComputerProvider() {
		setAuto(true);
	}

	@Override
	public ChessMove getNextMove(int color) {
		return getBestMove(getChessBoard(), color, color, 1 * 2);
	}

	private ChessMove getBestMove(ChessBoard chessBoardTmp, int initColor, int tryColor, int depth) {
		ChessMove bestMove = null;
		if (depth > 0) {
			ArrayList<ChessMove> allMoves = chessBoardTmp.getAllMoves(tryColor);
			if (allMoves.size() > 0) {
				for (ChessMove chessMove : allMoves) {
					ChessBoard chessBoardTmp2 = chessBoardTmp.clone();
					chessBoardTmp2.move(chessMove);
					chessMove.note = notate(chessBoardTmp2, tryColor) + Random.nextDouble();
					//ChessMove subChessMove = getBestMove(chessBoardTmp2, initColor, (tryColor == WHITE ? BLACK : WHITE), depth - 1);
					//if (subChessMove != null) {
					//	chessMove.note += ((initColor == tryColor) ? -1 : 1) * subChessMove.note;
					//}
					if (bestMove == null || chessMove.note > bestMove.note) {
						bestMove = chessMove;
					}
				}
			}
		}
		if (depth == 2)
			System.out.println("depth:" + depth + " " + bestMove);
		return bestMove;
	}

	private int notate(ChessBoard chessBoardTmp, int color) {
		int sum = 0;
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				int caseColor = chessBoardTmp.getColor(x, y);
				if (caseColor != EMPTY) {
					int type = chessBoardTmp.getType(x, y);
					sum = ((caseColor == color) ? 1 : -1) * notatePiece(type);
				}
			}
		}
		return sum;
	}

	private int notatePiece(int type) {
		int sum = 0;
		switch (type) {
		case EMPTY:
			break;
		case PAWN:
			sum++;
			break;
		case KNIGHT:
		case BISHOP:
			sum += 3;
			break;
		case ROOK:
			sum += 5;
			break;
		case QUEEN:
			sum += 10;
			break;
		case KING:
			sum += 1000;
			break;
		default:
			break;
		}
		return sum;
	}

}
