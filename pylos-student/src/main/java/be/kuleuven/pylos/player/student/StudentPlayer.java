package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;

/**
 * Created by Jan on 20/02/2015.
 */
public class StudentPlayer extends PylosPlayer {
	int maxdepth = 3;
	PylosGameSimulator simulator;

	@Override
	public void doMove(PylosGameIF game, PylosBoard board) {
		int maxEval = Integer.MIN_VALUE;
		PylosSphere bestSphere = null;
		PylosLocation bestLocation = null;
		int depth = 3;  // Define a suitable depth for your game

		// Create a simulator
		simulator = new PylosGameSimulator(game.getState(), this.PLAYER_COLOR, board);

		// Iterate over all spheres and locations to find the best move
		for (PylosSphere sphere : board.getSpheres()) {
			for (PylosLocation location : board.getLocations()) {
				// Check if the move is legal
				if (location.isUsable() && location.hasAbove()) {
					// S	imulate the move
					simulator.moveSphere(sphere, location);

					// Evaluate the board state
					int eval = minimax(board, depth - 1, this.PLAYER_COLOR, game, simulator.getState(), Integer.MIN_VALUE, Integer.MAX_VALUE);

					// Undo the move
					simulator.undoMoveSphere(sphere, location, game.getState(), this.PLAYER_COLOR);

					// Update maxEval and bestMove if this move is better
					if (eval > maxEval) {
						maxEval = eval;
						bestSphere = sphere;
						bestLocation = location;
					}
				}
			}
		}

		// Perform the best move found
		if (bestSphere != null && bestLocation != null) {
			game.moveSphere(bestSphere, bestLocation);
		}
	}
	@Override
	public void doRemove(PylosGameIF game, PylosBoard board) {
		int maxEval = Integer.MIN_VALUE;
		PylosSphere bestSphere = null;
		int depth = 3;  // Define a suitable depth for your game

		// Iterate over all spheres to find the best one to remove
		for (PylosSphere sphere : board.getSpheres()) {
			// Check if the sphere can be removed
			if (sphere.canRemove()) {
				// Simulate the removal
				game.removeSphere(sphere);

				// Evaluate the board state
				int eval = minimax(board, depth - 1, this.PLAYER_COLOR, game, game.getState(), Integer.MIN_VALUE, Integer.MAX_VALUE);

				// Undo the removal
				game.removeSphereIsDraw(sphere);

				// Update maxEval and bestSphere if this removal is better
				if (eval > maxEval) {
					maxEval = eval;
					bestSphere = sphere;
				}
			}
		}

		// Perform the best removal found
		if (bestSphere != null) {
			game.removeSphere(bestSphere);
		}
	}

	@Override
	public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
		int depth = 3;
		int maxEvalWithoutRemoval = minimax(board, depth - 1, this.PLAYER_COLOR, game, game.getState(), Integer.MIN_VALUE, Integer.MAX_VALUE);

		int maxEvalWithRemoval = Integer.MIN_VALUE;
		PylosSphere bestSphere = null;
		  // Define a suitable depth for your game

		// Iterate over all spheres to find the best one to remove
		for (PylosSphere sphere : board.getSpheres()) {
			// Check if the sphere can be removed
			if (sphere.canRemove()) {
				// Simulate the removal
				game.removeSphere(sphere);

				// Evaluate the board state
				int eval = minimax(board, depth - 1, this.PLAYER_COLOR, game, game.getState(), Integer.MIN_VALUE, Integer.MAX_VALUE);

				// Undo the removal
				game.removeSphereIsDraw(sphere);

				// Update maxEvalWithRemoval and bestSphere if this removal is better
				if (eval > maxEvalWithRemoval) {
					maxEvalWithRemoval = eval;
					bestSphere = sphere;
				}
			}
		}

		// Decide whether to remove a sphere or pass based on which option has a higher evaluation
		if (maxEvalWithRemoval > maxEvalWithoutRemoval && bestSphere != null) {
			game.removeSphere(bestSphere);
		} else {
			game.pass();
		}
	}
	private int minimax(PylosBoard board, int depth, PylosPlayerColor color, PylosGameIF game, PylosGameState state, int alpha, int beta) {
		if (depth == 0 || state == PylosGameState.COMPLETED) {
			return board.getReserve(this).getLocation().getMaxInSquare(this);
		}

		if (color == this.PLAYER_COLOR) {	// my turn
			int maxEval = Integer.MIN_VALUE;
			for (PylosLocation location : board.getLocations()) {
				if (location.isUsable() && location.hasAbove()) {
					simulator.moveSphere(board.getReserve(this), location);
					int eval = minimax(board, depth - 1, color, game, state, alpha, beta);
					simulator.undoMoveSphere(board.getReserve(this), location, state, color);
					maxEval = Math.max(maxEval, eval);
					alpha = Math.max(alpha, eval);
					if (beta <= alpha) {
						break;  // beta cut-off
					}
				}
			}
			return maxEval;
		} else {
			int minEval = Integer.MAX_VALUE;
			for (PylosLocation location : board.getLocations()) {
				if (location.isUsable() && location.hasAbove()) {
					simulator.moveSphere(board.getReserve(this), location);
					int eval = minimax(board, depth - 1, color, game, state, alpha, beta);	// opponent's turn
					simulator.undoMoveSphere(board.getReserve(this), location, state, color);
					minEval = Math.min(minEval, eval);
					beta = Math.min(beta, eval);
					if (beta <= alpha) {
						break;  // alpha cut-off
					}
				}
			}
			return minEval;
		}
	}
}






