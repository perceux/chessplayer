package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;
import java.util.HashMap;

import org.dreamsoft.chessplayer.client.ChessBoardUtils;
import org.dreamsoft.chessplayer.client.ChessMove;
import org.dreamsoft.chessplayer.client.provider.ProviderListener.GameCommand;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ToggleButton;

public class FileProvider extends Provider {

	private boolean isEnglish;

	protected int turnNumber;

	final ListBox listBox = new ListBox();

	private ToggleButton buttonPlay;

	ArrayList<String> moves = new ArrayList<String>();

	HashMap<Integer, String> comments = new HashMap<Integer, String>();

	@Override
	public HorizontalPanel getToolbarPanel() {
		if (toolbarPanel == null) {
			toolbarPanel = new HorizontalPanel();
			toolbarPanel.setSize("100%", "100%");
			toolbarPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
			toolbarPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

			listBox.setWidth("100%");
			listBox.addItem("partie1.txt");
			listBox.addItem("partie2.txt");
			listBox.addItem("partie3.txt");
			listBox.setSelectedIndex(1);

			Button buttonLoad = new Button("^", new ClickHandler() {
				public void onClick(ClickEvent event) {
					setDelay(0);
					loadGame("../games/" + listBox.getItemText(listBox.getSelectedIndex()));
					fireGameCommand(GameCommand.START);
				}
			});

			Button buttonBegin = new Button("&lt;&lt;", new ClickHandler() {
				public void onClick(ClickEvent event) {
					turnNumber = 0;
					setAuto(false);
					fireGameCommand(GameCommand.START);
				}
			});

			Button buttonNext = new Button("&gt;", new ClickHandler() {
				public void onClick(ClickEvent event) {
					setAuto(false);
					fireGameCommand(GameCommand.PLAY);
				}
			});

			buttonPlay = new ToggleButton(">|", "[ ]");
			buttonPlay.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					setAuto(!isAuto());
					if (isAuto())
						fireGameCommand(GameCommand.PLAY);
				}
			});

			Button buttonEnd = new Button("&gt;&gt;", new ClickHandler() {
				public void onClick(ClickEvent event) {
					setDelay(0);
					fireGameCommand(GameCommand.END);
				}
			});

			toolbarPanel.setBorderWidth(1);
			toolbarPanel.add(listBox);
			toolbarPanel.add(buttonLoad);
			toolbarPanel.add(buttonBegin);
			toolbarPanel.add(buttonNext);
			toolbarPanel.add(buttonPlay);
			toolbarPanel.add(buttonEnd);
			toolbarPanel.setCellWidth(listBox, "80%");

		}

		return toolbarPanel;
	}

	@Override
	public void setAuto(boolean auto) {
		super.setAuto(auto);
		buttonPlay.setDown(auto);
	}

	public void loadGame(String filename) {
		RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, filename);
		rb.setCallback(new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				setMessage("Erreur de chargement");
			}

			public void onResponseReceived(Request request, Response response) {
				if (response.getStatusCode() == 404) {
					setMessage("Erreur: fichier non trouve");
				} else {
					isEnglish = false;
					String[] lines = response.getText().split("\\n");
					moves.clear();
					comments.clear();
					turnNumber = 0;

					for (int i = 0; i < lines.length; i++) {
						String line = lines[i];
						String data[] = line.split("\\t");

						// Stocker les coups
						String coups[] = data[0].replaceAll("\\n", "").replaceAll("\\r", "").split(" ");
						for (int j = 0; j < coups.length; j++) {
							String coup = coups[j];
							if (coup.indexOf(".") > 0) {
								coup = coup.substring(coup.lastIndexOf(".") + 1);
							}
							moves.add(coup);
						}

						// Stocker les commentaires
						String comment = "";
						for (int j = 1; j < data.length; j++) {
							comment = comment + data[j];
						}
						comments.put(i, comment);
					}
					setMessage("Partie chargee! (" + moves.size() + ")");
				}
			}
		});
		try {
			rb.send();
		} catch (RequestException e) {
			setMessage("Erreur lors de la demande de fichier");
		}
	}

	@Override
	public ChessMove getNextMove(int color) {
		if (moves != null && (turnNumber < moves.size())) {
			String text = moves.get(turnNumber);
			String comment = comments.get(new Integer(turnNumber));
			int turnColor = (turnNumber % 2 == 0) ? WHITE : BLACK;
			System.out.println("getNextMove:" + turnNumber + " " + text + " " + color + " " + turnColor);
			if (turnColor != color)
				return null;
			turnNumber++;

			// Affiche le commentaire
			setMessage(comment);

			return ChessBoardUtils.parseMove(text, isEnglish, chessBoard, turnColor);

		}
		return null;
	}

	@Override
	public String getShortName() {
		return "File";
	}
}