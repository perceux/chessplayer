package org.dreamsoft.chessplayer.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class ChessPlayer implements EntryPoint {

	final ChessBoard board = new ChessBoard();

	final ListBox listBox = new ListBox();

	protected String[] lines;

	protected int lineNumber;

	ToggleButton buttonPlay;

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setSize("100%", "100%");
		panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

		listBox.setWidth("100%");
		listBox.addItem("partie1.txt");
		listBox.addItem("partie2.txt");
		listBox.addItem("partie3.txt");
		listBox.setSelectedIndex(1);

		Button buttonLoad = new Button("^", new ClickHandler() {
			public void onClick(ClickEvent event) {
				setPlayMode(false);
				loadGame("../games/" + listBox.getItemText(listBox.getSelectedIndex()));
			}
		});

		Button buttonBegin = new Button("&lt;&lt;", new ClickHandler() {
			public void onClick(ClickEvent event) {
				setPlayMode(false);
				board.startNewGame();
				lineNumber = 0;
			}
		});

		Button buttonNext = new Button("&gt;", new ClickHandler() {
			public void onClick(ClickEvent event) {
				setPlayMode(false);
				playNextMove();
			}
		});

		buttonPlay = new ToggleButton(">|", "[ ]");
		buttonPlay.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				setPlayMode(!playMode);
			}
		});

		Button buttonEnd = new Button("&gt;&gt;", new ClickHandler() {
			public void onClick(ClickEvent event) {
				setPlayMode(false);
				while (playNextMove()) {

				}
			}
		});

		panel.setBorderWidth(1);
		panel.add(listBox);
		panel.add(buttonLoad);
		panel.add(buttonBegin);
		panel.add(buttonNext);
		panel.add(buttonPlay);
		panel.add(buttonEnd);

		board.startNewGame();

		VerticalPanel vpanel = new VerticalPanel();
		panel.setCellWidth(listBox, "80%");
		vpanel.add(panel);
		vpanel.add(board);

		RootPanel.get().add(vpanel);
	}

	boolean playMode = false;

	Timer playTimer = new Timer() {
		public void run() {
			if (playMode && playNextMove()) {
				this.schedule(1000);
			}
		};
	};

	private void setPlayMode(boolean pMode) {
		playMode = pMode;
		if (playMode) {
			playTimer.run();
		} else {
			playTimer.cancel();
		}
		buttonPlay.setDown(playMode);
	}

	public boolean playNextMove() {
		boolean mustPlay = lines != null && (lineNumber < lines.length);
		if (mustPlay) {
			String line = lines[lineNumber];
			String data[] = line.split("\\t");

			// Jouer les coups
			String coups[] = data[0].replaceAll("\\n", "").replaceAll("\\r", "").split(" ");
			for (int j = 0; j < coups.length; j++) {
				String coup = coups[j];
				if (coup.indexOf(".") > 0) {
					coup = coup.substring(coup.lastIndexOf(".") + 1);
				}
				board.doMove(coup);
			}

			// Afficher les commentaires
			board.showMessage("");
			for (int j = 1; j < data.length; j++) {
				board.showMessage(data[j]);
			}

			lineNumber++;
		}
		return mustPlay;
	}

	public void loadGame(String filename) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, filename);
		rb.setCallback(new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				board.showMessage("Erreur de chargement");
			}

			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == 404) {
					board.showMessage("Erreur non trouve");
				} else {
					lineNumber = 0;
					lines = response.getText().split("\\n");
					board.startNewGame();
					board.showMessage("Partie chargee!");
				}
			}
		});
		try {
			rb.send();
		} catch (RequestException e) {
			board.showMessage("Erreur lors de la demande de fichier");
		}
	}
}
