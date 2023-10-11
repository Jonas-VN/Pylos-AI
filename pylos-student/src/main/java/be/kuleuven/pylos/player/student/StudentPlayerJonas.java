package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;

public class StudentPlayerJonas extends PylosPlayer {
    final int MAX_DEPTH = 7;
    BoardEvaluator evaluator = new BoardEvaluator();

    @Override
    public void doMove(PylosGameIF game, PylosBoard board) {
        Move firstMove = new Move();
        Move bestMove = minimax(game.getState(), board, firstMove, this.PLAYER_COLOR, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);

        PylosSphere bestSphere = bestMove.getSphere();
        PylosLocation bestLocation = bestMove.getEndLocation();
        game.moveSphere(bestSphere, bestLocation);
    }

    @Override
    public void doRemove(PylosGameIF game, PylosBoard board) {
        Move firstMove = new Move();
        Move bestMove = minimax(game.getState(), board, firstMove, this.PLAYER_COLOR, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);

        PylosSphere bestSphere = bestMove.getSphere();
        game.removeSphere(bestSphere);
    }

    @Override
    public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
        Move firstMove = new Move();
        Move bestMove = minimax(game.getState(), board, firstMove, this.PLAYER_COLOR, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);

        if (bestMove.getMoveType() == MoveType.PASS) {
            game.pass();
        } else {
            PylosSphere bestSphere = bestMove.getSphere();
            game.removeSphere(bestSphere);
        }
    }

    private Move minimax(PylosGameState gameState, PylosBoard board, Move latestMove, PylosPlayerColor playerColor, int depth, int alpha, int beta) {
        if (depth == 0 || gameState == PylosGameState.COMPLETED) {
            latestMove.setEvaluationScore(evaluator.evaluateBoard(board, this.PLAYER_COLOR));
            return latestMove;
        }

        StudentPlayerGameSimulator simulator = new StudentPlayerGameSimulator(gameState, playerColor, board);
        latestMove.generateAllLegalMoves(gameState, playerColor, board);
        Move nextBestMove = null;
        int bestEvaluationScore;

        if (playerColor == this.PLAYER_COLOR) {
            // Maximize
            bestEvaluationScore = Integer.MIN_VALUE;

            for (final Move move : latestMove.getChildren()) {
                simulator.doMove(move);
                Move bestNextMove = minimax(simulator.getState(), board, move, simulator.getColor(), depth - 1, alpha, beta);

                // Beta cut-off
                if (bestNextMove.getEvaluationScore() > beta) {
                    latestMove.setEvaluationScore(beta);
                    simulator.undoMove(move);
                    return move;
                }

                alpha = Math.max(alpha, bestNextMove.getEvaluationScore());
                if (beta <= alpha) {
                    move.setEvaluationScore(bestNextMove.getEvaluationScore());
                    simulator.undoMove(move);
                    return move;
                }

                if (bestEvaluationScore < bestNextMove.getEvaluationScore()) {
                    bestEvaluationScore = bestNextMove.getEvaluationScore();
                    nextBestMove = move;
                    nextBestMove.setEvaluationScore(bestEvaluationScore);
                }
                simulator.undoMove(move);
            }
        }
        else {
            // Minimize
            bestEvaluationScore = Integer.MAX_VALUE;

            for (final Move move : latestMove.getChildren()) {
                simulator.doMove(move);

                Move bestNextMove = minimax(simulator.getState(), board, move, simulator.getColor(), depth - 1, alpha, beta);

                // Alpha cut-off
                if (bestNextMove.getEvaluationScore() < alpha) {
                    move.setEvaluationScore(alpha);
                    simulator.undoMove(move);
                    return move;
                }

                beta = Math.min(beta, bestNextMove.getEvaluationScore());
                if (beta <= alpha) {
                    move.setEvaluationScore(bestNextMove.getEvaluationScore());
                    simulator.undoMove(move);
                    return move;
                }

                if (bestEvaluationScore > bestNextMove.getEvaluationScore()) {
                    bestEvaluationScore = bestNextMove.getEvaluationScore();
                    nextBestMove = move;
                    nextBestMove.setEvaluationScore(bestEvaluationScore);
                }
                simulator.undoMove(move);
            }
        }
        return nextBestMove;
    }
}

class StudentPlayerGameSimulator extends PylosGameSimulator {
    public StudentPlayerGameSimulator(PylosGameState gameState, PylosPlayerColor playerColor, PylosBoard board) {
        super(gameState, playerColor, board);
    }

    public void doMove(Move move) {
        switch (move.getMoveType()) {
            case MOVE, ADD -> this.moveSphere(move.getSphere(), move.getEndLocation());
            case REMOVE_FIRST, REMOVE_SECOND -> this.removeSphere(move.getSphere());
            case PASS -> this.pass();
        }
    }

    public void undoMove(Move move) {
        switch (move.getMoveType()) {
            case MOVE -> this.undoMoveSphere(move.getSphere(), move.getStartLocation(), PylosGameState.MOVE, move.getPlayerColor());
            case ADD -> this.undoAddSphere(move.getSphere(), PylosGameState.MOVE, move.getPlayerColor());
            case REMOVE_FIRST -> this.undoRemoveFirstSphere(move.getSphere(), move.getStartLocation(), PylosGameState.REMOVE_FIRST, move.getPlayerColor());
            case REMOVE_SECOND -> this.undoRemoveSecondSphere(move.getSphere(), move.getStartLocation(), PylosGameState.REMOVE_SECOND, move.getPlayerColor());
            case PASS -> this.undoPass(PylosGameState.REMOVE_SECOND, move.getPlayerColor());
        }
    }
}

class BoardEvaluator {
    private static final int OWN_WEIGHT = 1;
    private static final int OTHER_WEIGHT = 2;
    // evaluateReserveSpheres
    private static final int RESERVE_SPHERE_WEIGHT = 10;

    // evaluateSquares
    private static final int FULL_SQUARE_BONUS = 3;
    private static final int ALMOST_FULL_SQUARE_BONUS = 1;
    private static final int BLOCK_OTHER_SQUARE_BONUS = 2;
    private static final int SQUARE_WEIGHT = 5;

    public int evaluateBoard(PylosBoard board, PylosPlayerColor playerColor) {
        return (
                this.evaluateReserveSpheres(board, playerColor) +
                this.evaluateSquares(board, playerColor)
                );
    }

    private int evaluateReserveSpheres(PylosBoard board, PylosPlayerColor playerColor) {
        return (
                board.getReservesSize(playerColor) * OWN_WEIGHT -
                board.getReservesSize(playerColor.other()) * OTHER_WEIGHT
                ) * RESERVE_SPHERE_WEIGHT;
    }

    private int evaluateSquares(PylosBoard board, PylosPlayerColor playerColor) {
        int own_score = 0;
        int other_score = 0;
        int own_spheres;
        int other_spheres;

        for (PylosSquare square : board.getAllSquares()) {
            own_spheres = square.getInSquare(playerColor);
            other_spheres = square.getInSquare(playerColor.other());

            // Nice, you made a full square!
            if (own_spheres == 4) own_score += FULL_SQUARE_BONUS;
            else if (other_spheres == 4) other_score += FULL_SQUARE_BONUS;

            // Nice, you almost made a full square!
            else if (own_spheres == 3 && other_spheres == 0) own_score += ALMOST_FULL_SQUARE_BONUS;
            else if (other_spheres == 3 && own_spheres == 0) other_score += ALMOST_FULL_SQUARE_BONUS;

            // Nice, you blocked a square of your opponent!
            else if (own_spheres == 1 && other_spheres == 3) own_score += BLOCK_OTHER_SQUARE_BONUS;
            else if (other_spheres == 1 && own_spheres == 3) other_score += BLOCK_OTHER_SQUARE_BONUS;
        }
        return (
                own_score * OWN_WEIGHT -
                other_score * OTHER_WEIGHT
                ) * SQUARE_WEIGHT;
    }
}

class Move {
    private final MoveType moveType;
    private final PylosSphere sphere;
    private final PylosLocation startLocation, endLocation;
    private final PylosPlayerColor playerColor;
    private final ArrayList<Move> children = new ArrayList<>();
    private int evaluationScore = 0;

    public Move(MoveType moveType, PylosSphere sphere, PylosLocation startLocation, PylosLocation endLocation, PylosPlayerColor playerColor) {
        this.moveType = moveType;
        this.sphere = sphere;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.playerColor = playerColor;
    }

    public Move() {
        this.moveType = null;
        this.sphere = null;
        this.startLocation = null;
        this.endLocation = null;
        this.playerColor = null;
    }

    public MoveType getMoveType() {
        return moveType;
    }

    public PylosSphere getSphere() {
        return sphere;
    }

    public PylosLocation getStartLocation() {
        return startLocation;
    }

    public PylosLocation getEndLocation() {
        return endLocation;
    }

    public PylosPlayerColor getPlayerColor() {
        return playerColor;
    }

    public int getEvaluationScore() {
        return evaluationScore;
    }

    public ArrayList<Move> getChildren() {
        return children;
    }

    public void setEvaluationScore(int evaluationScore) {
        this.evaluationScore = evaluationScore;
    }

    public void addChild(Move child) {
        children.add(child);
    }

    public void generateAllLegalMoves(PylosGameState gameState, PylosPlayerColor playerColor, PylosBoard board) {
        switch (gameState) {
            case MOVE:
                this.generateAllLegalMovesMove(playerColor, board);
                break;
            case REMOVE_FIRST:
                this.generateAllLegalMovesRemoveFirst(playerColor, board);
                break;
            case REMOVE_SECOND:
                this.generateAllLegalMovesRemoveSecond(playerColor, board);
                break;
        }
    }

    private void generateAllLegalMovesMove(PylosPlayerColor playerColor, PylosBoard board) {
        // Move a sphere to a higher level
        for (final PylosSquare square : board.getAllSquares()) {
            if (square.isSquare()) {
                final PylosLocation destinationLocation = square.getTopLocation();
                for (final PylosSphere sphere : board.getSpheres(playerColor)) {
                    if (!sphere.isReserve() && sphere.canMoveTo(destinationLocation)) {
                        Move move = new Move(MoveType.MOVE, sphere, sphere.getLocation(), destinationLocation, playerColor);
                        this.addChild(move);
                    }
                }
            }
        }
        // Add a reserve sphere
        for (final PylosLocation destinationLocation : board.getLocations()) {
            if (destinationLocation.isUsable()) {
                PylosSphere sphere = board.getReserve(playerColor);
                Move move = new Move(MoveType.ADD, sphere, sphere.getLocation(), destinationLocation, playerColor);
                this.addChild(move);
            }
        }
    }

    private void generateAllLegalMovesRemoveFirst(PylosPlayerColor playerColor, PylosBoard board) {
        // 1st remove: Always remove
        for (final PylosSphere sphere : board.getSpheres(playerColor)) {
            if (sphere.canRemove()) {
                Move move = new Move(MoveType.REMOVE_FIRST, sphere, sphere.getLocation(), null, playerColor);
                this.addChild(move);
            }
        }
    }

    private void generateAllLegalMovesRemoveSecond(PylosPlayerColor playerColor, PylosBoard board) {
        // 2nd remove: Also allowed to pass
        this.addChild(new Move(MoveType.PASS, null, null, null, playerColor));
        for (final PylosSphere sphere : board.getSpheres(playerColor)) {
            if (sphere.canRemove()) {
                Move move = new Move(MoveType.REMOVE_SECOND, sphere, sphere.getLocation(), null, playerColor);
                this.addChild(move);
            }
        }
    }

}

enum MoveType {
    MOVE,
    ADD,
    REMOVE_FIRST,
    REMOVE_SECOND,
    PASS
}
