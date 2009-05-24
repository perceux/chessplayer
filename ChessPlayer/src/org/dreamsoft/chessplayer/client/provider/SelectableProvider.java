package org.dreamsoft.chessplayer.client.provider;

import org.dreamsoft.chessplayer.client.ChessBoard;
import org.dreamsoft.chessplayer.client.ChessMove;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class SelectableProvider extends Provider {

	protected ListBox listBox = new ListBox(false);

	protected Provider provider = new HumanProvider();

	protected Provider[] providers = new Provider[] { new HumanProvider(), new RandomProvider(), new FileProvider(), new ComputerProvider() };

	public SelectableProvider(final ProviderListener providerListener) {
		listBox.addItem("Human");
		listBox.addItem("Random");
		listBox.addItem("File");
		listBox.addItem("Computer");
		for (int i = 0; i < providers.length; i++) {
			Provider p = providers[i];
			p.addProviderListener(providerListener);
		}
		addProviderListener(providerListener);
		listBox.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				provider = providers[listBox.getSelectedIndex()];
				fireProviderChange();
			}
		});
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
		int i = -1;
		if (provider instanceof HumanProvider) {
			i = 0;
		} else if (provider instanceof RandomProvider) {
			i = 1;
		} else if (provider instanceof FileProvider) {
			i = 2;
		} else if (provider instanceof ComputerProvider) {
			i = 3;
		}
		if (i != -1) {
			listBox.setSelectedIndex(i);
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
}