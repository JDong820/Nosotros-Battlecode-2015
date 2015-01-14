package team094;
import team094.Params;
import team094.BO;
import java.util.*;
import battlecode.common.*;

class Beaver extends Role {
    int minerFactoryCap;
    // TODO: Currently unreliable count; does not remove dead.
    // Probably use broadcasts.
    int minerFactoryCount = 0;

    Beaver(RobotController rc) {
        super(rc);
        minerFactoryCap = calcMinerFactoryCap(Params.MINERFACTORY_CAP_A,
                                              Params.MINERFACTORY_CAP_B,
                                              Params.MINERFACTORY_CAP_C);
        //System.out.println("F(Params) minerFactoryCap=" + minerFactoryCap);
    }

    void execute() {
        try {
            if (rc.isCoreReady()) {
                if (rc.senseOre(location) > Params.BEAVER_ORE_THRESHOLD) {
                    rc.mine();
                    return;
                }
                /*
                if (rc.getTeamOre() >= 500) {
                    Direction dirTarget = location.directionTo(base);
                    if (build(dirTarget, RobotType.MINERFACTORY)) {
                        return;
                    }
                }
                */
                move(location.directionTo(base).opposite());
                return;
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

    boolean build(Direction d, RobotType building) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = directions[(directionToInt(d)+offset+8)%8];
            if (rc.canMove(trialDir)) {
                rc.build(trialDir, building);
                return true;
            }
        }
        return false;
    }

    public int calcMinerFactoryCap(double a, int b, int c) {
        double minerFactoryCap = a*base.distanceSquaredTo(enemy) + b;
        return minerFactoryCap  > c ? (int)minerFactoryCap : c;
    }
}
