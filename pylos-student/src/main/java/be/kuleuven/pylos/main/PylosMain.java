package be.kuleuven.pylos.main;

import be.kuleuven.pylos.battle.Battle;
import be.kuleuven.pylos.battle.BattleMT;
import be.kuleuven.pylos.battle.BattleResult;
import be.kuleuven.pylos.battle.RoundRobin;
import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGame;
import be.kuleuven.pylos.game.PylosGameObserver;
import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerObserver;
import be.kuleuven.pylos.player.PylosPlayerType;
import be.kuleuven.pylos.player.codes.PlayerFactoryCodes;
import be.kuleuven.pylos.player.codes.PylosPlayerBestFit;
import be.kuleuven.pylos.player.codes.PylosPlayerMiniMax;
<<<<<<< HEAD
import be.kuleuven.pylos.player.student.StudentPlayerOfficial;
=======
import be.kuleuven.pylos.player.codes.PylosPlayerRandomFit;
import be.kuleuven.pylos.player.student.StudentPlayer;
import be.kuleuven.pylos.player.student.StudentPlayerJonas;
import be.kuleuven.pylos.player.student.StudentPlayerRandomFit;
>>>>>>> 38797ef6f776f899ba8058df8ab8d4af94bfc880

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class PylosMain {

    public static void main(String[] args) {
        /* !!! jvm argument !!! -ea */

        //startSingleGame();
<<<<<<< HEAD
        startBattle();
        //startBattleMultithreaded();
=======
        //startBattle();
        startBattleMultithreaded();
>>>>>>> 38797ef6f776f899ba8058df8ab8d4af94bfc880
        //startRoundRobinTournament();
    }

    public static void startSingleGame() {

        Random random = new Random(0);

        PylosPlayer playerLight = new PylosPlayerBestFit();
        PylosPlayer playerDark = new PylosPlayerMiniMax(2);

        PylosBoard pylosBoard = new PylosBoard();
        PylosGame pylosGame = new PylosGame(pylosBoard, playerLight, playerDark, random, PylosGameObserver.CONSOLE_GAME_OBSERVER, PylosPlayerObserver.NONE);

        pylosGame.play();
    }

    public static void startBattle() {
<<<<<<< HEAD
        int nRuns = 300;
        PylosPlayerType p1 = new PylosPlayerType("BestFit") {
            @Override
            public PylosPlayer create() {
                return new PylosPlayerMiniMax(5);
            }
        };

        PylosPlayerType p2 = new PylosPlayerType("wij") {
            @Override
            public PylosPlayer create() {
                return new StudentPlayerOfficial();
=======
        int nRuns = 100;
        PylosPlayerType p1 = new PylosPlayerType("Jonas") {
            @Override
            public PylosPlayer create() {
                return new StudentPlayerJonas();
            }
        };

        PylosPlayerType p2 = new PylosPlayerType("Minimax 10") {
            @Override
            public PylosPlayer create() {
                return new PylosPlayerMiniMax(10);
>>>>>>> 38797ef6f776f899ba8058df8ab8d4af94bfc880
            }
        };

        Battle.play(p1, p2, nRuns);
    }

    public static void startBattleMultithreaded() {
        //Please refrain from using Collections.shuffle(List<?> list) in your player,
        //as this is not ideal for use across multiple threads.
        //Use Collections.shuffle(List<?> list, Random random) instead, with the Random object from the player (PylosPlayer.getRandom())

        int nRuns = 100;
        int nThreads = 6;

        PylosPlayerType p1 = new PylosPlayerType("Jonas") {
            @Override
            public PylosPlayer create() {
                return new StudentPlayerJonas();
            }
        };
        PylosPlayerType p2 = new PylosPlayerType("CODES - 4") {
            @Override
            public PylosPlayer create() {
                return new PylosPlayerMiniMax(4);
            }
        };

        System.out.println("Starting battle between " + p1 + " and " + p2 + " with " + nRuns + " runs and " + nThreads + " threads.");
        BattleMT.play(p1, p2, nRuns, nThreads);
    }

    public static void startRoundRobinTournament() {
        //Same requirements apply as for startBattleMultithreaded()

        //Create your own PlayerFactory containing all PlayerTypes you want to test
        PlayerFactoryCodes pFactory = new PlayerFactoryCodes();
        //PlayerFactoryStudent pFactory = new PlayerFactoryStudent();

        int nRunsPerCombination = 1000;
        int nThreads = 8;

        Set<RoundRobin.Match> matches = RoundRobin.createTournament(pFactory);

        RoundRobin.play(matches, nRunsPerCombination, nThreads);

        List<BattleResult> results = matches.stream().map(c -> c.battleResult).collect(Collectors.toList());

        RoundRobin.printWinsMatrix(results, pFactory.getTypes());
    }
}
