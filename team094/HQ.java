package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class HQ extends Role {
    int beaverCap;
    int numBeavers = 0;
    int numSoldiers = 0;
    int numBashers = 0;
    int numBarracks = 0;

    HQ(RobotController rc) {
        super(rc);
        beaverCap = calcBeaverCap(Params.BEAVER_CAP_A,
                                  Params.BEAVER_CAP_B,
                                  Params.BEAVER_CAP_C);
        //System.out.println("F(Params) beaverCap="+ beaverCap);
    }

    void update() {
        try {
            msg = nextMsg();
        } catch (GameActionException e) {
            System.err.println("Could not fetch mail.\n");
        }
    }

    void execute() {
        try {
            /*
            for (RobotInfo r : myRobots) {
                RobotType type = r.type;
                if (type == RobotType.SOLDIER) {
                    numSoldiers++;
                } else if (type == RobotType.BASHER) {
                    numBashers++;
                } else if (type == RobotType.BEAVER) {
                    numBeavers++;
                } else if (type == RobotType.BARRACKS) {
                    numBarracks++;
                }
            }
            */
            if (Clock.getRoundNum() % 100 == 0) {
                rc.addMatchObservation(Clock.getRoundNum() + ":  " + rc.getTeamOre());
            }
            if (Clock.getRoundNum() == 300) {
                rc.resign();
            }

            boolean coreReady = rc.isCoreReady();
            if (coreReady && rc.isWeaponReady()) {
                RobotInfo[] enemies = rc.senseNearbyRobots(range, enemyTeam);
                coreReady ^= amove(enemies);
            }
            if (coreReady) {
                // Make Beavers.
                if (numBeavers < beaverCap) {
                    if (rc.getTeamOre() >= 100) {
                        //spawn(Direction.NORTH);
                        coreReady ^= spawn(directions[(int)(rand.nextDouble()*8)],
                                RobotType.BEAVER);
                    }
                }
            }
            while (msg != null && Clock.getBytecodesLeft() > safety) {
                // Handle as many messages as possible.
                switch (msg.getCode()) {
                    case 0x00: // Do nothing.
                        break;
                    case 0x01: // Idle beaver.
                        break;
                    case 0xff: // Beaver debug ping
                        System.out.println(msg.getTargetPid() + " ! {" +
                                msg.getSenderPid() + ", " +
                                msg.getTimeout() + ", " +
                                msg.getCode() + ", " +
                                msg.getData().size() + "}");

                        break;
                    default:
                        break;
                }
                update();
            }
            //System.out.println("Read all messages.");
            if (Clock.getBytecodesLeft() > 500 + safety) {
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

    protected Msg nextMsg() throws GameActionException {
        return nextMsg(0x0000, 0x1000);
    }
}
