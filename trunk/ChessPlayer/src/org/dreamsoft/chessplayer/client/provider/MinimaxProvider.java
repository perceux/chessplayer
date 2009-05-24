package org.dreamsoft.chessplayer.client.provider;

import java.util.ArrayList;
import java.util.Vector;

import org.dreamsoft.chessplayer.client.ChessBoard;
import org.dreamsoft.chessplayer.client.ChessBoardUtils;
import org.dreamsoft.chessplayer.client.ChessMove;
import org.dreamsoft.chessplayer.client.provider.ProviderListener.GameCommand;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * This class implements a naive minimax optimizing chessplayer.
 * 
 */
public class MinimaxProvider extends Provider {

	private static final double POS_INFTY = Double.MAX_VALUE;

	private static final double NEG_INFTY = -POS_INFTY;

	/**
	 * The depth to which the player will search the game tree for possible
	 * solutions.
	 */
	private int maxDepth = 1;

	private int color;

	/**
	 * Getting the next move using a minimax tactic.
	 * 
	 * @return The best move minimaxing could find.
	 */
	@Override
	public ChessMove getNextMove(int color) {
		possibilities.clear();
		noOfEvaluations = 0;
		this.setColor(color);
		miniMax(getChessBoard(), getMaxDepth(), true, color);

		// Extracting the result from the minimax algorithm
		// (a bit cumbersome, but thats the way it is done.
		ChessMove result = null;
		if (!possibilities.isEmpty()) {
			result = possibilities.get(Random.nextInt(possibilities.size() - 1));
		}

		return result;
	}

	private int getMaxDepth() {
		String depth = depthTextBox.getValue();
		int result = maxDepth;
		try {
			result = Integer.parseInt(depth);
		} catch (Exception e) {
			depthTextBox.setValue("" + maxDepth);
		}
		return result;
	}

	/**
	 * Capturing moves considered during the top level evaluations.
	 */
	// private Map<ChessMove, Double> capturingMovesConsidered = new
	// HashMap<ChessMove, Double>();
	/**
	 * The number of evaluations that were performed to find the move were
	 * selecting now.
	 */
	private long noOfEvaluations = 0;

	/**
	 * Create a new instance of a player with a specific color..
	 */
	public MinimaxProvider() {
	}

	/**
	 * The final maximizing move found when searching.
	 */
	private Vector<ChessMove> possibilities = new Vector<ChessMove>();

	/**
	 * The minimax algorithm, instrumented to report its final maximizing move.
	 * 
	 * metacode for minimax (from wikipedia).
	 * 
	 * <pre>
	 * function minimax(node, depth)
	 *     if node is a terminal node or depth = 0
	 *         return the heuristic value of node
	 *     if the adversary is to play at node
	 *         let a := +infty
	 *         foreach child of node
	 *             a := min(a, minimax(child, depth-1))
	 *     else {we are to play at node}
	 *         let a := -infty
	 *         foreach child of node
	 *             a := max(a, minimax(child, depth-1))
	 *     return a
	 * </pre>
	 * 
	 * @param b
	 *            the chessboard
	 * @param depth
	 *            the level we are working at, must be 1 or more to generate
	 *            moves before trying to select move.
	 * @param doMaximize
	 *            Is this a maximizing move were finding?.
	 * @return the integer evaluation maximizing or minimizing the options the
	 *         player has.
	 */
	private synchronized double miniMax(final ChessBoard b, final int depth, final boolean doMaximize, final int color) {

		if (!b.hasTwoKings()) {
			// If we are maximizing, then the previous minimax was
			// a minimizing one, that means that if a capture of the
			// king did happen, then it was our king that was captured.
			// This means that if we are maximizing, then a terminal
			// position means that the oposition will win, hence
			// the value we return is negative infinity.
			if (doMaximize) {
				return NEG_INFTY;
			} else {
				return POS_INFTY;
			}
		} else if (depth == 0) {
			// Count number evaluations to measure performance.
			noOfEvaluations += 1;
			debug.setText("noOfEvaluations=" + noOfEvaluations);
			// We are always evaluating with our color in mind.
			// the minimaxing will figure out who should benefit.
			return evaluate(b, getColor());
		} else if (!doMaximize) {
			// Find minimizing move
			double a = POS_INFTY;
			ArrayList<ChessMove> moves = ChessBoardUtils.getAllMoves(b, 3 - color);
			for (ChessMove m : moves) {
				ChessBoard bam = ChessBoardUtils.getBoardAfterMove(b, m);
				double c = miniMax(bam, depth - 1, !doMaximize, color);
				if (c < a) {
					a = c;
				}
			}
			return a;
		} else {
			// Find maximizing move
			double a = NEG_INFTY;
			ArrayList<ChessMove> moves = ChessBoardUtils.getAllMoves(b, color);

			for (ChessMove m : moves) {
				ChessBoard bam = ChessBoardUtils.getBoardAfterMove(b, m);
				double c = miniMax(bam, depth - 1, !doMaximize, color);
				if (a < c) {
					// If this is the top level iteration through moves,
					// then we shuld collect all the maximizing moves.
					if (depth == maxDepth) {
						possibilities.clear();
						possibilities.add(m);
					}
					a = c;
				} else if (a == c && depth == maxDepth) {
					possibilities.add(m);
				}
			}
			return a;
		}
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor() {
		return color;
	}

	/**
	 * Evaluate a board as seen from the perspective of a color.
	 * 
	 * @param b
	 *            the board.
	 * @param c
	 *            the color.
	 * @return the combined goodness.
	 */
	public double evaluate(final ChessBoard b, final int c) {
		int scores[] = b.getScores();
		if (c == BLACK) {
			return calculateGoodness(scores[1], scores[0]);
		} else {
			return calculateGoodness(scores[0], scores[1]);
		}
	}

	@Override
	public boolean isAuto() {
		return true;
	}

	/**
	 * Given the relative strenghts of the the different colors, calculate the
	 * combined goodness of the combination.
	 * 
	 * @param a
	 *            the strength of one of the colors.
	 * @param b
	 *            the strength of the other color.
	 * @return the combined goodness.
	 */
	private static double calculateGoodness(final double a, final double b) {
		double result;
		if (a == 0 && b > 0) {
			result = NEG_INFTY;
		} else if (b == 0 && a > 0) {
			result = POS_INFTY;
		} else {
			result = a - b;
		}
		return result;
	}

	@Override
	public String getShortName() {
		return "Computer";
	}

	TextBox depthTextBox = new TextBox();

	HTML debug = new HTML();

	@Override
	public HorizontalPanel getToolbarPanel() {
		if (toolbarPanel == null) {
			toolbarPanel = new HorizontalPanel();
			toolbarPanel.add(new HTML("Depth:"));
			toolbarPanel.add(depthTextBox);

			Button buttonPlay = new Button("&gt;", new ClickHandler() {
				public void onClick(ClickEvent event) {
					fireGameCommand(GameCommand.PLAY);
				}
			});
			toolbarPanel.add(buttonPlay);

			toolbarPanel.add(new HTML(" Debug:"));
			toolbarPanel.add(debug);
		}
		return toolbarPanel;
	}

}
