package org.dreamsoft.chessplayer.client.provider;

import org.dreamsoft.chessplayer.client.ChessBoard;
import org.dreamsoft.chessplayer.client.ChessMove;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class SelectableProvider extends Provider {

	protected ListBox listBox = new ListBox(false);

	protected Provider provider = null;

	protected Provider[] providers = new Provider[] { new HumanProvider(), new RandomProvider(), new FileProvider(), new MinimaxProvider() };

	public SelectableProvider(final ProviderListener providerListener) {
		for (int i = 0; i < providers.length; i++) {
			Provider p = providers[i];
			listBox.addItem(p.getShortName());
			p.addProviderListener(providerListener);
			if (provider == null)
				provider = p;
		}
		addProviderListener(providerListener);
		listBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				provider = providers[listBox.getSelectedIndex()];
				fireProviderChange(provider);
			}
		});
	}

	public void setSelectedIndex(int index) {
		listBox.setSelectedIndex(index);
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
		for (int j = 0; j < providers.length; j++) {
			Provider p = providers[j];
			if (provider.getClass().equals(p.getClass())) {
				providers[j] = p;
				p.setChessBoard(chessBoard);
				listBox.setSelectedIndex(j);
			}
		}
	}

	@Override
	public String getMessage() {
		return provider.getMessage();
	}

	@Override
	public void setMessage(String message) {
		provider.setMessage(message);
	}

	@Override
	public ChessMove getNextMove(int color) {
		return provider.getNextMove(color);
	}

	@Override
	public void setChessBoard(ChessBoard chessBoard) {
		this.chessBoard = chessBoard;
		for (int i = 0; i < providers.length; i++) {
			Provider p = providers[i];
			p.setChessBoard(chessBoard);
		}
	}

	@Override
	public HorizontalPanel getToolbarPanel() {
		return provider.getToolbarPanel();
	}

	@Override
	public int getDelay() {
		return provider.getDelay();
	}

	public ListBox getListBox() {
		return listBox;
	}

	@Override
	public boolean isAuto() {
		return provider.isAuto();
	}

	@Override
	public String getShortName() {
		return provider.getShortName();
	}
}