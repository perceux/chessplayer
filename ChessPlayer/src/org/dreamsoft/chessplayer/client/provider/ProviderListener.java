package org.dreamsoft.chessplayer.client.provider;

public interface ProviderListener {

	public enum GameCommand {
		START, PLAY, STOP, END, MESSAGE
	};

	void onProviderChange(Provider provider);

	void onGameCommand(Provider provider, GameCommand t);

}
