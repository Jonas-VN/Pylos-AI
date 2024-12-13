package be.kuleuven.pylos.player.student;

import be.kuleuven.pylos.player.PylosPlayer;
import be.kuleuven.pylos.player.PylosPlayerFactory;
import be.kuleuven.pylos.player.PylosPlayerType;

/**
 * Created by Jan on 20/02/2015.
 */
public class PlayerFactoryStudent extends PylosPlayerFactory {

    public PlayerFactoryStudent() {
        super("Student");
    }

    @Override
    protected void createTypes() {

        /* example */
        add(new PylosPlayerType("Gil") {
            @Override
            public PylosPlayer create() {
                return new StudentPlayer();
            }
        });

        add(new PylosPlayerType("Student - Random") {
            @Override
            public PylosPlayer create() {
                return new StudentPlayerRandomFit();
            }
        });

//        add(new PylosPlayerType("officialbot") {
//            @Override
//            public PylosPlayer create(){
//                return new StudentPlayerOfficial();
//            }
//        });

        add(new PylosPlayerType("Wij") {
            @Override
            public PylosPlayer create() {
                return new StudentPlayerGilEnJonas();
            }
        });
    }
}
