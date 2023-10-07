package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.game.PylosBoard;
import be.kuleuven.pylos.game.PylosGameIF;
import be.kuleuven.pylos.game.PylosLocation;
import be.kuleuven.pylos.game.PylosSphere;
import be.kuleuven.pylos.player.PylosPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ine on 5/05/2015.
 */

public class StudentPlayerRandomFit extends PylosPlayer {

    @Override
    public void doMove(PylosGameIF game, PylosBoard board) {
        /* add a reserve sphere to a feasible random location */

        // Altijd een nieuwe bal nemen
        PylosSphere sphere = board.getReserve(this);

        List<PylosLocation> locations = new ArrayList<>();
        for (PylosLocation location : board.getLocations()) {
            if (location.isUsable()) {
                locations.add(location);
            }
        }
        PylosLocation randomLocation = locations.get((int) (Math.random() * locations.size()));
        game.moveSphere(sphere, randomLocation);
    }

    @Override
    public void doRemove(PylosGameIF game, PylosBoard board) {
        /* removeSphere a random sphere */
        PylosSphere removeSphere;
        List<PylosSphere> removableSpheres = new ArrayList<>();
        for (PylosSphere sphere : board.getSpheres(this)) {
            if (sphere.canRemove()) {
                removableSpheres.add(sphere);
            }
        }
        removeSphere = removableSpheres.get((int) (Math.random() * removableSpheres.size()));
        game.removeSphere(removeSphere);
    }

    @Override
    public void doRemoveOrPass(PylosGameIF game, PylosBoard board) {
        /* always pass */
        game.pass();

    }
}
