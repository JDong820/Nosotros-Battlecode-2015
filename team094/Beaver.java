package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class Beaver extends Role {

    Beaver(RobotController rc) {
        super(rc);
    }

    void execute() {
        try {
            if (rc.isCoreReady()) {
                if (rc.senseOre(location) > Params.BEAVER_ORE_THRESHOLD) {
                    rc.mine();
                } else if (false) {
                    build(location.directionTo(base), RobotType.MINERFACTORY);
                } else {
                    move(location.directionTo(base).opposite());
                } 
            } else {
                autotransferSupply(Params.SUPPLY_BEAVER_A,
                        Params.SUPPLY_BEAVER_B,
                        Params.SUPPLY_BEAVER_C,
                        Params.SUPPLY_BEAVER_D,
                        Params.SUPPLY_BEAVER_E,
                        Params.SUPPLY_BEAVER_F);
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " Beaver Exception\n");
            e.printStackTrace();
        }
    }

    // Note: always moves, even backwards, unless completely blocked.
    // Won't need to make this a bool unless severe crowding is a thing.
    void move(Direction d) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = directions[(directionToInt(d)+offset+8)%8];
            if (rc.canMove(trialDir)) {
                rc.move(trialDir);
                return;
            }
        }
    }

    void build(Direction d, RobotType building) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = directions[(directionToInt(d)+offset+8)%8];
            if (rc.canMove(trialDir)) {
                rc.build(trialDir, building);
                return;
            }
        }
    }
}
