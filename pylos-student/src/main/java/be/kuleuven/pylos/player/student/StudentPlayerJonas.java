package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.*;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;

public class StudentPlayerJonas extends PylosPlayer {
    final int MAX_DEPTH = 8;
    final BoardEvaluator evaluator = new BoardEvaluator();
    boolean isFirstMove = true;

    @Override
    public void doMove(PylosGameIF game, PylosBoard board) {
        Move bestMove;
        if (isFirstMove) {
            bestMove = doFirstMove(board);
            isFirstMove = false;
        } else {
            Move root = new Move();
            bestMove = minimax(game.getState(), board, root, this.PLAYER_COLOR, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);
        }


        assert bestMove != null;
        PylosSphere bestSphere = bestMove.getSphere();
        PylosLocation bestLocation = bestMove.getEndLocation();
        game.moveSphere(bestSphere, bestLocation);
    }

    @Override
    public void doRemove(PylosGameIF game, PylosBoard board) {
        Move root = new Move();
        Move bestMove = minimax(game.getState(), board, root, this.PLAYER_COLOR, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);

        assert bestMove != null;
        PylosSphere bestSphere = bestMove.getSphere();
        game.removeSphere(bestSphere);
    }

    @Override
    public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
        Move root = new Move();
        Move bestMove = minimax(game.getState(), board, root, this.PLAYER_COLOR, MAX_DEPTH, Integer.MIN_VALUE, Integer.MAX_VALUE);

        assert bestMove != null;
        if (bestMove.getMoveType() == MoveType.PASS) {
            game.pass();
        } else {
            PylosSphere bestSphere = bestMove.getSphere();
            game.removeSphere(bestSphere);
        }
    }

    private Move doFirstMove(PylosBoard board) {
        final int z = 0;
        final int[] Y = {1, 2};
        final int[] X = {1, 2};

        for (final int x : X) {
            for (final int y : Y) {
                PylosLocation location = board.getBoardLocation(x, y, z);
                PylosSphere sphere = board.getReserve(this.PLAYER_COLOR);
                if (sphere.canMoveTo(location)) {
                    return new Move(MoveType.ADD, sphere, sphere.getLocation(), location, this.PLAYER_COLOR);
                }
            }
        }
        // Should never happen
        return null;
    }

    private Move minimax(PylosGameState gameState, PylosBoard board, Move latestMove, PylosPlayerColor playerColor, int depth, int alpha, int beta) {
        // Recursion base case
        if (depth <= 0 || gameState == PylosGameState.COMPLETED) {
            latestMove.setEvaluationScore(evaluator.evaluateBoard(board, this.PLAYER_COLOR));
            return latestMove;
        }

        StudentPlayerGameSimulator simulator = new StudentPlayerGameSimulator(gameState, playerColor, board);
        latestMove.generateAllLegalMoves(gameState, playerColor, board);
        Move currentBestMove = null;

        if (playerColor == this.PLAYER_COLOR) {
            // Maximize
            int bestEvaluationScore = Integer.MIN_VALUE;

            for (final Move move : latestMove.getChildren()) {
                simulator.doMove(move);
                Move nextBestMove = minimax(simulator.getState(), board, move, simulator.getColor(), depth - 1, alpha, beta);
                simulator.undoMove(move);

                // Beta cut-off
                if (nextBestMove.getEvaluationScore() > beta) {
                    move.setEvaluationScore(beta);
                    return move;
                }

                // Alpha beta pruning
                alpha = Math.max(alpha, nextBestMove.getEvaluationScore());
                if (beta <= alpha) {
                    move.setEvaluationScore(nextBestMove.getEvaluationScore());
                    return move;
                }

                // Update best move
                if (bestEvaluationScore < nextBestMove.getEvaluationScore()) {
                    bestEvaluationScore = nextBestMove.getEvaluationScore();
                    currentBestMove = move;
                    currentBestMove.setEvaluationScore(bestEvaluationScore);
                }
            }
        } else {
            // Minimize
            int bestEvaluationScore = Integer.MAX_VALUE;

            for (final Move move : latestMove.getChildren()) {
                simulator.doMove(move);
                Move nextBestMove = minimax(simulator.getState(), board, move, simulator.getColor(), depth - 1, alpha, beta);
                simulator.undoMove(move);

                // Alpha cut-off
                if (nextBestMove.getEvaluationScore() < alpha) {
                    move.setEvaluationScore(alpha);
                    return move;
                }

                // Alpha beta pruning
                beta = Math.min(beta, nextBestMove.getEvaluationScore());
                if (beta <= alpha) {
                    move.setEvaluationScore(nextBestMove.getEvaluationScore());
                    return move;
                }

                // Update best move
                if (bestEvaluationScore > nextBestMove.getEvaluationScore()) {
                    bestEvaluationScore = nextBestMove.getEvaluationScore();
                    currentBestMove = move;
                    currentBestMove.setEvaluationScore(bestEvaluationScore);
                }
            }
        }
        return currentBestMove;
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
    // evaluateReserveSpheres
    private static final int RESERVE_SPHERE_WEIGHT = 10;

    // evaluateSquares
    private static final int FULL_SQUARE_BONUS = 3;
    private static final int ALMOST_FULL_SQUARE_BONUS = 1;
    private static final int BLOCK_OTHER_SQUARE_BONUS = 2;
    private static final int SQUARE_WEIGHT = 5;

    public int evaluateBoard(PylosBoard board, PylosPlayerColor playerColor) {
        return (this.evaluateReserveSpheres(board, playerColor) +
                this.evaluateSquares(board, playerColor)
        );
    }

    private int evaluateReserveSpheres(PylosBoard board, PylosPlayerColor playerColor) {
        final int own_spheres = board.getReservesSize(playerColor);
        final int other_spheres = board.getReservesSize(playerColor.other());
        return (own_spheres - other_spheres) * RESERVE_SPHERE_WEIGHT;
    }

    private int evaluateSquares(PylosBoard board, PylosPlayerColor playerColor) {
        int own_score = 0;
        int other_score = 0;

        for (PylosSquare square : board.getAllSquares()) {
            final int own_spheres = square.getInSquare(playerColor);
            final int other_spheres = square.getInSquare(playerColor.other());

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
        return (own_score - other_score) * SQUARE_WEIGHT;
    }
}

class Move {
    private final MoveType moveType;
    private final PylosSphere sphere;
    private final PylosLocation startLocation, endLocation;
    private final PylosPlayerColor playerColor;
    private ArrayList<Move> children;
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

    public void initChildern() {
        this.children = new ArrayList<>();
    }

    public MoveType getMoveType() {
        return this.moveType;
    }

    public PylosSphere getSphere() {
        return this.sphere;
    }

    public PylosLocation getStartLocation() {
        return this.startLocation;
    }

    public PylosLocation getEndLocation() {
        return this.endLocation;
    }

    public PylosPlayerColor getPlayerColor() {
        return this.playerColor;
    }

    public int getEvaluationScore() {
        return this.evaluationScore;
    }

    public ArrayList<Move> getChildren() {
        return this.children;
    }

    public void setEvaluationScore(int evaluationScore) {
        this.evaluationScore = evaluationScore;
    }

    public void addChild(Move child) {
        this.children.add(child);
    }

    public void generateAllLegalMoves(PylosGameState gameState, PylosPlayerColor playerColor, PylosBoard board) {
        this.initChildern();
        switch (gameState) {
            case MOVE -> this.generateAllLegalMovesMove(playerColor, board);
            case REMOVE_FIRST -> this.generateAllLegalMovesRemoveFirst(playerColor, board);
            case REMOVE_SECOND -> this.generateAllLegalMovesRemoveSecond(playerColor, board);
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
