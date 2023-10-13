package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {
	private static final int MAX_DEPTH = 7;

	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		Action bestAction = findBestAction(game, board);
		game.removeSphere(bestAction.pylosSphere);
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		Action bestAction = findBestAction(game, board);
		if (bestAction.pass) {
			game.pass();
		} else {
			game.removeSphere(bestAction.pylosSphere);
		}
	}

	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		Action bestAction = findBestAction(game, board);
		game.moveSphere(bestAction.pylosSphere, bestAction.location);
	}

	private Action findBestAction(PylosGameIF game, PylosBoard board) {
		Action bestAction = null;
		int bestScore = Integer.MIN_VALUE;
		int alpha = Integer.MIN_VALUE;
		int beta = Integer.MAX_VALUE;

		PylosGameSimulator simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);
		ArrayList<Action> possibleActions = generatePossibleActions(board, simulator);

		for (Action action : possibleActions) {
			int score = -minimax(simulator, board, StudentPlayer.MAX_DEPTH - 1, alpha, beta, this.PLAYER_COLOR.other());
			if (score > bestScore) {
				bestScore = score;
				bestAction = action;
			}
			if (beta <= alpha) {
				break;
			}
		}

		return bestAction;
	}

	private int minimax(PylosGameSimulator simulator, PylosBoard board, int depth, int alpha, int beta, PylosPlayerColor playerColor) {
		if (depth == 0 || simulator.getState().equals(PylosGameState.COMPLETED) || simulator.getState().equals(PylosGameState.DRAW)) {
			return evaluate(board, playerColor);
		}

		int bestScore = (playerColor == this.PLAYER_COLOR) ? Integer.MIN_VALUE : Integer.MAX_VALUE;

		ArrayList<Action> possibleActions = generatePossibleActions(board, simulator);

		for (Action action : possibleActions) {
			int score = -minimax(simulator, board, depth - 1, alpha, beta, playerColor.other());
			if (playerColor == this.PLAYER_COLOR) {
				bestScore = Math.max(bestScore, score);
				alpha = Math.max(alpha, bestScore);
			} else {
				bestScore = Math.min(bestScore, score);
				beta = Math.min(beta, bestScore);
			}
			if (beta <= alpha) {
				return bestScore;
			}
		}

		return bestScore;
	}

	public ArrayList<Action> generatePossibleActions(PylosBoard board, PylosGameSimulator simulator) {
		ArrayList<Action> possibleActions = new ArrayList<>();
		PylosGameState state = simulator.getState();
		PylosPlayerColor color = simulator.getColor();

		if(state == PylosGameState.MOVE) {
			generateMoveActions(board, simulator, possibleActions, state, color);
		} else if(state == PylosGameState.REMOVE_FIRST) {
			generateRemoveActions(board, possibleActions, state, color);
		} else {
			generatePassOrRemoveActions(board, possibleActions, state, color);
		}

		assert !possibleActions.isEmpty() : "No possible actions found";
		return possibleActions;
	}

	private void generateMoveActions(PylosBoard board, PylosGameSimulator simulator, ArrayList<Action> possibleActions,
									 PylosGameState state, PylosPlayerColor color) {
		PylosSphere reserveSphere = board.getReserve(color);
		for (PylosLocation location : board.getLocations()) {
			// Move a sphere from the reserve to an available spot
			if (location.isUsable()) possibleActions.add(new Action(reserveSphere, location, state, color));
			// Move a sphere on the board to a higher location
			for (PylosSphere sphere : board.getSpheres(color)) {
				if(sphere.canMoveTo(location) && !sphere.isReserve()) possibleActions.add(new Action(sphere, location, state, color));
			}
		}
	}

	private void generateRemoveActions(PylosBoard board, ArrayList<Action> possibleActions,
									   PylosGameState state, PylosPlayerColor color) {
		// remove a sphere in a square
		for(PylosSphere sphere : board.getSpheres(color)){
			if(sphere.canRemove()) possibleActions.add(new Action(sphere, sphere.getLocation(), state, color));
		}
	}

	private void generatePassOrRemoveActions(PylosBoard board, ArrayList<Action> possibleActions,
											 PylosGameState state, PylosPlayerColor color) {
		// Pass
		possibleActions.add(new Action(true));
		// remove a sphere in a square
		for(PylosSphere sphere : board.getSpheres(color)){
			if(sphere.canRemove()) possibleActions.add(new Action(sphere, state, color));
		}
	}

	private int evaluate(PylosBoard board, PylosPlayerColor playerColor) {
		int reservesScore = board.getReservesSize(this.PLAYER_COLOR) - board.getReservesSize(playerColor.other());
		int squaresScorePlayer = countSquares(playerColor, board);
		int squaresScoreOpponent = countSquares(playerColor.other(), board);
		int squaresScore = squaresScorePlayer - squaresScoreOpponent;
		int centerScorePlayer = countCenters(playerColor, board);
		int centerScoreOpponent = countCenters(playerColor.other(), board);
		int centerScore = centerScorePlayer - centerScoreOpponent;

		// New factors to consider
		int potentialSquareScore = countPotentialSquares(playerColor, board) - countPotentialSquares(playerColor.other(), board);

		return reservesScore + squaresScore + centerScore + 2* potentialSquareScore;
	}

	private int countSquares(PylosPlayerColor playerColor, PylosBoard board) {
		int count = 0;
		for (PylosSquare square : board.getAllSquares()) {
			if (square.isSquare(playerColor)) {
				count++;
			}
		}
		return count;
	}

	private int countCenters(PylosPlayerColor playerColor, PylosBoard board) {
		int count = 0;
		for (PylosSphere sphere : board.getSpheres(playerColor)) {
			if (isCentered(sphere, board)) {
				count++;
			}
		}
		return count;
	}
	private int countPotentialSquares(PylosPlayerColor playerColor, PylosBoard board) {
		int count = 0;
		for (PylosSquare square : board.getAllSquares()) {
			if (square.getInSquare(playerColor) == 3 && square.getInSquare() == 3) {
				count++;
			}
		}
		return count;
	}


	private boolean isCentered(PylosSphere sphere, PylosBoard board) {
		if (!sphere.isReserve()) {
			PylosLocation location = sphere.getLocation();
			int x = location.X;
			int y = location.Y;
			int size = board.SIZE;
			return x > 0 && x < size - 1 && y > 0 && y < size - 1;
		}
		return false;
	}

	private static class Action {
		PylosSphere pylosSphere;
		PylosLocation location;
		boolean pass;
		PylosGameState state;
		PylosPlayerColor color;

		public Action(PylosSphere pylosSphere, PylosGameState state, PylosPlayerColor color) {
			this.pylosSphere = pylosSphere;
			this.state = state;
			this.color = color;
		}
		public Action(PylosSphere pylosSphere, PylosLocation location, PylosGameState state, PylosPlayerColor color) {
			this.pylosSphere = pylosSphere;
			this.location = location;
			this.state = state;
			this.color = color;
		}

		Action(boolean pass) {
			this.pass = pass;
		}
	}
}