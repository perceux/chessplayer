package org.dreamsoft.chessplayer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ChessPlayer implements EntryPoint {

	final ChessGame chessGame = new ChessGame();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		chessGame.startNewGame();
		VerticalPanel vpanel = new VerticalPanel();
		vpanel.add(chessGame);
		RootPanel.get().add(vpanel);
	}



	}
