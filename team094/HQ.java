package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class HQ extends Role {
    int debugPingSeq = 0;

    int beaverCap;
    // TODO: replace with a UnitInfo class or something
    int numBeavers;
    ArrayList<RobotInfo> beavers;

    HQ(RobotController rc) {
        super(rc);
        beaverCap = calcBeaverCap(Params.BEAVER_CAP_A,
                                  Params.BEAVER_CAP_B,
                                  Params.BEAVER_CAP_C);
        // TODO: use constants file.
        inboxIndex = 0x0000;
        //System.out.println("F(Params) beaverCap="+ beaverCap);
    }

    // Bytecode cost: ~1000
    // 15.01T16:21
    void updateInbox() {
        try {
            unreadMsg = fetchNextMsg();
        } catch (GameActionException e) {
            System.err.println("Could not fetch mail.\n");
        }
    }

    // Should only happen once per turn.
    void update() {
        //RobotInfo[] robots = rc.senseNearbyRobots(0x7fffffff, team);
        //numBeavers = 0;
        //beavers = new ArrayList<RobotInfo>();
        //for (RobotInfo ri : robots) {
        //    switch (ri.type) {
        //        case BEAVER:
        //            ++numBeavers;
        //            beavers.add(ri);
        //            break;
        //        default:
        //            break;
        //    }
        //}
        updateInbox();
    }

    void execute() {
        try {
            if (Clock.getRoundNum() % 100 == 0) {
                rc.addMatchObservation(Clock.getRoundNum() + ":  " + rc.getTeamOre());
            }
            //if (Clock.getRoundNum() == 1000) {
            //    rc.resign();
            //}

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
            } else {
                // If can't do anything, send messages.
                //debugPing();
            }
            // Handle as many messages as possible.
            int benchMsgCount = 1;
            while (unreadMsg != null && Clock.getBytecodesLeft() > safety) {
                ++benchMsgCount;
                handleMessage(unreadMsg);
                updateInbox();
            }
            if (Clock.getBytecodesLeft() > safety) {
                autotransferSupply(Params.SUPPLY_HQ_A,
                                   Params.SUPPLY_HQ_B,
                                   Params.SUPPLY_HQ_C,
                                   Params.SUPPLY_HQ_D,
                                   Params.SUPPLY_HQ_E,
                                   Params.SUPPLY_HQ_F);
            }
            // Print benchmark output
            //System.out.println("Ended with " + Clock.getBytecodesLeft() + " bytecodes left.\n" +
            //        "Handled " + benchMsgCount + " total messages.");
        } catch (Exception e) {
            System.err.println(e.toString() + ": HQ Exception\n");
            e.printStackTrace();
        }
    }

    protected void handleMessage(Msg msg) {
        switch (msg.getHeader().getCode()) {
            case 0x00: // Do nothing.
                break;
            case 0x01: // Idle beaver.
                break;
            case 0xff: // Debug ping
                final Header debug = new Header(msg);
                System.out.println(debug.getTargetPid() + " ! {" +
                        debug.getSenderPid() + ", " +
                        debug.getTimeout() + ", " +
                        debug.getCode() + ", " +
                        debug.getDataLen() + "}");
                break;
            default:
                break;
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

    protected Msg fetchNextMsg() throws GameActionException {
        return fetchNextMsg(0x0000, 0x1000);
    }

    void debugPing() throws GameActionException {
        int randIndex = (int)(rand.nextDouble()*beavers.size());
        ArrayList<Integer> pingSeq = new ArrayList<Integer>(1);
        pingSeq.add(debugPingSeq++);
        send(RobotType.BEAVER,
                new Msg(rc,
                    beavers.get(randIndex).ID,
                    0xff, pingSeq));
    }            
}
