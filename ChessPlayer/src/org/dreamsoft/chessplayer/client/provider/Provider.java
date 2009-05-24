package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;
import java.util.Iterator;

import org.dreamsoft.chessplayer.client.ChessBoard;
import org.dreamsoft.chessplayer.client.ChessMove;
import org.dreamsoft.chessplayer.client.Constantes;
import org.dreamsoft.chessplayer.client.provider.ProviderListener.GameCommand;

import com.google.gwt.user.client.ui.HorizontalPanel;

public abstract class Provider implements Constantes {

	protected ChessBoard chessBoard;

	protected HorizontalPanel toolbarPanel;

	protected int delay = 0;

	private ArrayList<ProviderListener> providerListeners;

	protected String message;

	private boolean auto = false;

	abstract public ChessMove getNextMove(int color);

	public void setChessBoard(ChessBoard chessBoard) {
		this.chessBoard = chessBoard;
	}

	protected ChessBoard getChessBoard() {
		return chessBoard;
	}

	public HorizontalPanel getToolbarPanel() {
		return toolbarPanel;
	}

	public void setToolbarPanel(HorizontalPanel toolbarPanel) {
		this.toolbarPanel = toolbarPanel;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	public void addProviderListener(ProviderListener providerListener) {
		if (providerListeners == null)
			providerListeners = new ArrayList<ProviderListener>();
		providerListeners.add(providerListener);
	}

	public void removeProviderListener(ProviderListener providerListener) {
		if (providerListeners == null)
			providerListeners.remove(providerListener);
	}

	protected void fireProviderChange(Provider provider) {
		for (Iterator<ProviderListener> iterator = providerListeners.iterator(); iterator.hasNext();) {
			ProviderListener pl = iterator.next();
			pl.onProviderChange(provider);
		}
	}

	protected void fireGameCommand(GameCommand commandType) {
		for (Iterator<ProviderListener> iterator = providerListeners.iterator(); iterator.hasNext();) {
			ProviderListener pl = iterator.next();
			pl.onGameCommand(this, commandType);
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
		fireGameCommand(GameCommand.MESSAGE);
	}

	public void setAuto(boolean auto) {
		this.auto = auto;
	}

	public boolean isAuto() {
		return auto;
	}

	abstract public String getShortName();

}
