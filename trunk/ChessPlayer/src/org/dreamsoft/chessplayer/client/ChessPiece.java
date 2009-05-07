package org.dreamsoft.chessplayer.client;

import com.google.gwt.user.client.ui.Image;

/**
 * Piece de lechiquier
 * 
 * @author pcv
 * 
 */
public class ChessPiece extends Image implements Cloneable {

	public static final int PAWN = 1;

	public static final int ROOK = 2;

	public static final int KNIGHT = 3;

	public static final int BISHOP = 4;

	public static final int KING = 5;

	public static final int QUEEN = 6;

	public static final String PIECE_LETTERS = " TCFRD";
	
	public static final String PIECE_LETTERS_ENGLISH = " RNBKQ";

	public int color = 0;

	public int type = 0;

	public int nbMove = -1;

	public int x = 0;

	public int y = 0;

	public ChessPiece(int type, int color) {
		super("images/" + type + "" + color + ".gif");
		setSize("32px", "32px");
		this.type = type;
		this.color = color;
	}

}