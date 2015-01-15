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
        // TODO: use constants file.
        inboxIndex = 0x1000;
        //System.out.println("F(Params) minerFactoryCap=" + minerFactoryCap);
    }

    void updateInbox() {
        try {
            unreadMsg = fetchNextMsg();
        } catch (GameActionException e) {
            System.err.println("Could not fetch mail.");
        }
    }

    void update() {
        location = rc.getLocation();
        updateInbox();
    }

    void execute() {
        try {
            boolean coreReady = rc.isCoreReady();
                // TODO: add calc
            if (coreReady && rc.senseOre(location) > Params.BEAVER_ORE_THRESHOLD) {
                    rc.mine();
                    coreReady = false;
            } else {
                // If not mining, tell HQ idle.
                send(RobotType.HQ, new Msg(rc, 0x01, null));
            }
            // Handle as many messages as possible.
            while (unreadMsg != null && Clock.getBytecodesLeft() > safety) {
                handleMessage(unreadMsg);
                update();
            }
            if (coreReady) {
                coreReady ^= move(location.directionTo(base).opposite());
            }
            if (Clock.getBytecodesLeft() > 500 + safety) {
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
    
    protected void handleMessage(Msg msg) {
        switch (msg.getHeader().getCode()) {
            case 0x01:
                break;
            case 0xff: // Debug ping
                final Header debug = new Header(msg);
                System.out.println(debug.getTargetPid() + " ! {" +
                        debug.getSenderPid() + ", " +
                        debug.getTimeout() + ", " +
                        debug.getCode() + ", " +
                        debug.getDataLen() + ", {seq: 0x" +
                        Integer.toHexString(msg.getData().get(0)) + "}}");
                break;
            default:
                break;
        }
    }

    // Note: always moves, even backwards, unless completely blocked.
    boolean move(Direction d) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = directions[(directionToInt(d)+offset+8)%8];
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

    protected Msg fetchNextMsg() throws GameActionException {
        return fetchNextMsg(0x1000, 0x2000);
    }
}
