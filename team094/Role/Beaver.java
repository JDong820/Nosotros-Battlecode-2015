package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class Beaver extends Role {
    final boolean benchBytecodeOutput = false;
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
            }
            for (Msg m: messages) {
                if (Clock.getBytecodesLeft() < safety)
                    break;
                handleMessage(m);
            }
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

            if (benchBytecodeOutput) {
                System.out.println("Ended with " + Clock.getBytecodesLeft() + " bytecodes left.");
                System.out.println("Received " + messages.size() + " total messages this turn.");
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " Beaver Exception\n");
            e.printStackTrace();
        }
    }

    protected void handleMessage(Msg msg) throws GameActionException {
        /*
        final Header debug = new Header(msg);
        System.out.println(debug.getTargetPid() + " ! {" +
                debug.getSenderPid() + ", " +
                debug.getTimeout() + ", " +
                debug.getCode() + ", " +
                debug.getDataLen() + "}");
                */

        switch (msg.getHeader().getCode()) {
        case REQ: // Request by status
            final Status status = Duck.i2s(msg.getData().get(0));
            //System.out.println("{status: " + status + "}");
            switch (status) {
            case IDLE:
                if (coreReady) {
                    send(RobotType.HQ, Code.ACK, Duck.val2ali(location));
                }
                break;
            case ANY:
            default:
                send(RobotType.HQ, Code.ACK, Duck.val2ali(location));
                break;
            }
            final Header debug1 = new Header(msg);
            break;
        case DEBUG: // Debug ping
        default:
            System.out.println("{seq: 0x" +
                               Integer.toHexString(msg.getData().get(0)) + "}");
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
