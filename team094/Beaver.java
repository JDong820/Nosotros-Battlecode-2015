package team094;
import team094.Params;
import team094.BO;
import java.util.*;
import battlecode.common.*;

class Beaver extends Role {
    final Params p;
    //int goalMinerFactoryCount;

    boolean coreReady;

    Beaver(RobotController rc) {
        super(rc);
        p = new Params();
        //goalMinerFactoryCount = calcMinerFactoryCap(p, baseToEnemySquared);

        // TODO: use constants file.
        inboxIndex = 0x1000;
        //System.out.println("F(Params) minerFactoryCap=" + minerFactoryCap);
    }

    void update() {
        location = rc.getLocation();
        coreReady = rc.isCoreReady();
        weaponReady = rc.isWeaponReady();

        updateInbox();
    }

    void execute() {
        try {
            // TODO: add calc from map analysis.
            if (coreReady && rc.senseOre(location) > p.BEAVER_ORE_THRESHOLD) {
                    rc.mine();
                    coreReady = false;
            } else {
                // If not mining, tell HQ idle.
                //send(RobotType.HQ, new Msg(rc, 0x01, null));
            }
            // Handle as many messages as possible.
            /*
            while (unreadMsg != null && Clock.getBytecodesLeft() > safety) {
                handleMessage(unreadMsg);
                update();
            }
            */
            if (coreReady) {
                coreReady ^= move(location.directionTo(base).opposite());
            }
            if (Clock.getBytecodesLeft() > safety) {
                autotransferSupply(p.SUPPLY_BEAVER_A,
                                   p.SUPPLY_BEAVER_B,
                                   p.SUPPLY_BEAVER_C,
                                   p.SUPPLY_BEAVER_D,
                                   p.SUPPLY_BEAVER_E,
                                   p.SUPPLY_BEAVER_F);
            }
            if (p.BENCHMARKING_ON) {
                System.out.println("Ended with " + Clock.getBytecodesLeft() + " bytecodes left.");
                System.out.println("Received " + messages.size() + " total messages this turn.");
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " Beaver Exception\n");
            e.printStackTrace();
        }
    }
    
    protected void handleMessage(Msg msg) throws GameActionException {
        switch (msg.getHeader().getCode()) {
            case REQ: // Request to build
                if (coreReady) {
                    // Protocol
                    // {building, desired_location}
                    final RobotType building = Duck.i2rt(msg.getData().get(0));
                    final MapLocation loc = Duck.i2ml(msg.getData().get(1));

                    // Check if we are fit to build.
                    if (true) {
                        // Ack
                        send(RobotType.HQ, Code.ACK, Duck.val2ali(location));
                    }
                    

                    final Header debug1 = new Header(msg);
                    System.out.println(debug1.getTargetPid() + " ! {" +
                            debug1.getSenderPid() + ", " +
                            debug1.getTimeout() + ", " +
                            debug1.getCode() + ", " +
                            debug1.getDataLen() + ", {building: " +
                            building + ", loc: " + loc + "}} (REQ)");
                }
                break;
            case DEBUG: // Debug ping
            default:
                final Header debug = new Header(msg);
                System.out.println(debug.getTargetPid() + " ! {" +
                        debug.getSenderPid() + ", " +
                        debug.getTimeout() + ", " +
                        debug.getCode() + ", " +
                        debug.getDataLen() + ", {seq: 0x" +
                        Integer.toHexString(msg.getData().get(0)) + "}}");
                break;
        }
    }

    // Note: always moves, even backwards, unless completely blocked.
    boolean move(Direction d) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = Duck.i2d((Duck.val2i(d)+offset+8)%8);
            if (rc.canMove(trialDir)) {
                rc.move(trialDir);
                return true;
            }
        }
        return false;
    }

    boolean build(Direction d, RobotType building) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = Duck.i2d((Duck.val2i(d)+offset+8)%8);
            if (rc.canMove(trialDir)) {
                rc.build(trialDir, building);
                return true;
            }
        }
        return false;
    }

    public static int calcMinerFactoryCap(Params p, int d) {
        double goal = p.GOAL_MINERFACTORIES_A*d;
        return goal > p.GOAL_MINERFACTORIES_B ? (int)goal : p.GOAL_MINERFACTORIES_B;
    }

    protected Msg fetchNextMsg() throws GameActionException {
        return fetchNextMsg(0x1000, 0x2000);
    }
}
