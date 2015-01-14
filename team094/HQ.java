package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class HQ extends Role {
    int beaverCap;
    // TODO: Currently unreliable count; does not remove dead.
    // Probably use broadcasts.
    int beaverCount = 0;

    HQ(RobotController rc) {
        super(rc);
        beaverCap = calcBeaverCap(Params.BEAVER_CAP_A,
                                  Params.BEAVER_CAP_B,
                                  Params.BEAVER_CAP_C);
        //System.out.println("F(Params) beaverCap="+ beaverCap);
    }

    void update() {
    }

    void execute() {
        try {
            if (Clock.getRoundNum() % 100 == 0) {
                rc.addMatchObservation(Clock.getRoundNum() + ":  " + rc.getTeamOre());
            }
            if (Clock.getRoundNum() == 1000) {
                rc.resign();
            }

            if (rc.isCoreReady()) {
                if (beaverCount < beaverCap) {
                    if (rc.getTeamOre() >= 100) {
                        //spawn(Direction.NORTH);
                        if (spawn(directions[(int)(rand.nextDouble()*8)],
                                  RobotType.BEAVER)) {
                            ++beaverCount;
                        }
                    }
                }
            } else {
                autotransferSupply(Params.SUPPLY_HQ_A,
                                   Params.SUPPLY_HQ_B,
                                   Params.SUPPLY_HQ_C,
                                   Params.SUPPLY_HQ_D,
                                   Params.SUPPLY_HQ_E,
                                   Params.SUPPLY_HQ_F);
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " HQ Exception\n");
            e.printStackTrace();
        }
    }

    // Returns false when all possible spawn locations are blocked.
    boolean spawn(Direction d, RobotType type) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = directions[(directionToInt(d)+offset+8)%8];
            if (rc.canSpawn(trialDir, type)) {
                rc.spawn(trialDir, type);
                return true;
            }
        }
        return false;
    }

    private int calcBeaverCap(double a, int b, int c) {
        double beaverCap = a*base.distanceSquaredTo(enemy) + b;
        return beaverCap > c ? (int)beaverCap : c;
    }
}
