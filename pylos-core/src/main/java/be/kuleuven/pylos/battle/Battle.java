package be.kuleuven.pylos.battle;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.game.PylosGameCrashedException;
import be.kuleuven.pylos.game.PylosGameState;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerType;

import java.util.Random;

public class Battle {

    public static BattleResult play(PylosPlayerType ppt1, PylosPlayerType ppt2, int runs) {
        return play(ppt1, ppt2, runs, true);
    }

    public static BattleResult play(PylosPlayerType ppt1, PylosPlayerType ppt2, int runs, boolean print) {
        if (runs % 2 != 0) {
            throw new IllegalArgumentException("Please specify an even number of runs");
        }

        Random random = new Random();
        String p1Name = ppt1.toString();
        String p2Name = ppt2.toString();
        PylosPlayer p1 = ppt1.create();
        PylosPlayer p2 = ppt2.create();

        long totalPlayTime = 0;

        int p1StartP1Win = 0;
        int p1StartP2Win = 0;
        int p1StartDraw = 0;

        if (print) System.out.println("Starting battle: " + p1Name + " vs " + p2Name);

        for (int i = 0; i < runs / 2; i++) {
            if (print) System.out.print("*");
            PylosBoard board = new PylosBoard();
            PylosGame game = new PylosGame(board, p1, p2, random);
            long startTime = System.currentTimeMillis();
            try{
                game.play();
                if (game.getState() == PylosGameState.DRAW) {
                    p1StartDraw++;
                } else {
                    if (game.getWinner() == p1) {
                        p1StartP1Win++;
                    } else {
                        p1StartP2Win++;
                    }
                }
            }
            catch(PylosGameCrashedException ge){
                System.err.println("Game crashed during turn of " + ge.getCurrentPlayer().getClass().getName() + ", giving forfeit");
                ge.getException().printStackTrace();
                if (ge.getCurrentPlayer() == p1){
                    p1StartP2Win++;
                }
                else{
                    p1StartP1Win++;
                }
            }
            long playTime = System.currentTimeMillis() - startTime;
            totalPlayTime += playTime;
        }

        int p2StartP1Win = 0;
        int p2StartP2Win = 0;
        int p2StartDraw = 0;

        for (int i = 0; i < runs / 2; i++) {
            if (print) System.out.print("*");
            PylosBoard board = new PylosBoard();
            PylosGame game = new PylosGame(board, p2, p1, random);
            long startTime = System.currentTimeMillis();
            try {
                game.play();
                if (game.getState() == PylosGameState.DRAW) {
                    p2StartDraw++;
                } else {
                    if (game.getWinner() == p1) {
                        p2StartP1Win++;
                    } else {
                        p2StartP2Win++;
                    }
                }
            }
            catch (PylosGameCrashedException ge){
                System.err.println("Game crashed during turn of " + ge.getCurrentPlayer().getClass().getName() + ", giving forfeit");
                ge.getException().printStackTrace();
                if (ge.getCurrentPlayer() == p2) {
                    p2StartP1Win++;
                } else {
                    p2StartP2Win++;
                }
            }
            long playTime = System.currentTimeMillis() - startTime;
            totalPlayTime += playTime;
        }

        if (print) System.out.println();

        BattleResult battleResult = new BattleResult(ppt1, ppt2, totalPlayTime, p1StartP1Win, p1StartDraw, p1StartP2Win, p2StartP1Win, p2StartDraw, p2StartP2Win);

        if (print) {
            battleResult.print();
        }

        return battleResult;
    }
}

