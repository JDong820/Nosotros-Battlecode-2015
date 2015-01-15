package team094;
import java.util.*;
import battlecode.common.*;

abstract class Role {
    static final Direction[] directions = {Direction.NORTH,
                                           Direction.NORTH_EAST,
                                           Direction.EAST,
                                           Direction.SOUTH_EAST,
                                           Direction.SOUTH,
                                           Direction.SOUTH_WEST,
                                           Direction.WEST,
                                           Direction.NORTH_WEST
                                          };
    final RobotController rc;
    Random rand;
    final Team team;
    final Team enemyTeam;
    final MapLocation base;
    final MapLocation enemy;
    MapLocation location;
    final int range;


    // Mailbox 
    Msg msg;
    int msgIndex = 0;

    // Try to always have at least this many bytecodes remaining.
    final int safety = 500;

    Role(RobotController rc) {
        this.rc = rc;
        rand = new Random(rc.getID());
        team = rc.getTeam();
        enemyTeam = team.opponent();
        base = rc.senseHQLocation();
        enemy = rc.senseEnemyHQLocation();
        location = rc.getLocation();
        range = rc.getType().attackRadiusSquared;
    }

    // Methods
    abstract void update();
    abstract void execute();

    static int directionToInt(Direction d) {
        switch(d) {
        case NORTH:
            return 0;
        case NORTH_EAST:
            return 1;
        case EAST:
            return 2;
        case SOUTH_EAST:
            return 3;
        case SOUTH:
            return 4;
        case SOUTH_WEST:
            return 5;
        case WEST:
            return 6;
        case NORTH_WEST:
            return 7;
        default:
            System.err.println("Unknown direction used. What?");
            return -1;
        }
    }

    protected Msg nextMsg() throws GameActionException {
        final int inboxBegin;
        switch (rc.getType()) {
            case HQ:
                inboxBegin = 0x0000;
                return nextMsg(inboxBegin, inboxBegin + 0x1000);
            default:
                inboxBegin = 0x0000;
                return null;
        }
    }

    protected boolean send(RobotType targetType, Msg msg) throws GameActionException {
        // dataLen is a byte, so max packet size is 0xff+2
        if (0 < msg.getPacketLen() && msg.getPacketLen() <= 0x101) {
            // "Transaction" semi-guarantee
            if (Clock.getBytecodesLeft() > msg.getPacketLen()*25 + safety) {
                final int inboxOffset;
                // TODO: put these into a constants file.
                switch (targetType) {
                    case HQ:
                        inboxOffset = 0x0000;
                        break;
                    case BEAVER:
                        inboxOffset = 0x1000;
                        break;
                    case MINER:
                        inboxOffset = 0x2000;
                        break;
                    case BASHER:
                    case LAUNCHER:
                    case SOLDIER:
                    case TANK:
                        inboxOffset = 0x2000;
                        break;
                    case DRONE:
                        inboxOffset = 0x3000;
                        break;
                    case MISSILE:
                        inboxOffset = 0x4000;
                        break;
                    case COMMANDER:
                        inboxOffset = 0x5000;
                        break;
                    case COMPUTER:
                        inboxOffset = 0x6000;
                        break;
                    // case HANDWASHSTATION: // No inbox.
                    case AEROSPACELAB:
                        inboxOffset = 0x7000;
                        break;
                    case MINERFACTORY:
                        inboxOffset = 0x8000;
                        break;
                    case BARRACKS:
                        inboxOffset = 0x9000;
                        break;
                    case HELIPAD:
                        inboxOffset = 0xa000;
                        break;
                    case SUPPLYDEPOT:
                        inboxOffset = 0xb000;
                        break;
                    case TANKFACTORY:
                        inboxOffset = 0xc000;
                        break;
                    case TECHNOLOGYINSTITUTE:
                        inboxOffset = 0xd000;
                        break;
                    // case TOWER:  // No inbox.
                    case TRAININGFIELD:
                        inboxOffset = 0xe000;
                        break;
                    default: // I didn't miss anything!!!
                        inboxOffset = 0xffff;
                        break;
                }
                if (inboxOffset != 0xffff) {
                    // Get next message spot
                    int nextMessageOffset = 0;
                    // TODO: use a constants file (INBOX_SIZE).
                    int header1 = rc.readBroadcast(inboxOffset + nextMessageOffset);
                    int headeR2 = rc.readBroadcast(inboxOffset + nextMessageOffset + 1);
                    while (nextMessageOffset < 0x1000 &&
                           rc.readBroadcast(inboxOffset + nextMessageOffset) != 0) {
                        // Get second header.
                        int header2 = rc.readBroadcast(inboxOffset + nextMessageOffset + 1);
                        nextMessageOffset += Msg.headerLen +
                            Msg.getPacketLenFromHeader2(header2);
                    }
                    // "Transaction" semi-guarantee
                    if (Clock.getBytecodesLeft() > msg.getPacketLen()*25 + safety) {
                        msg.writeWithOffset(rc, inboxOffset + nextMessageOffset);
                    } 
                }
                return true;
            }
        }
        return false;
    }

    // optimal supply trasfer = f(X, Y, D) >= 0, X, Y, D >= 0.
    // Note: originSupply is probobaly always rc.getSupply()
    private int calcSupplyTransfer(double originSupply,
                                   double targetSupply,
                                   int dsquared,
                                   RobotType targetType,
                                   double a, double b, double c, int d, int e) {
        switch (targetType) {
        default:
            int optimalTransfer = (int) (a*originSupply - b*targetSupply + c*dsquared);
            if (optimalTransfer < d || targetSupply > e) {
                return 0;
            }
            return optimalTransfer;
        }
    };

    void autotransferSupply() throws GameActionException {
        // Params (a,b,c,d,e,f):
        // (origin_coeff, target_coeff, d_coeff,
        //  min_transfer, max_target, baseline)
        // Constraints:
        // d >= 0, e >= 0, f >= 0
        switch (rc.getType()) {
        default:
            autotransferSupply(1, 0, 0, 0, 9000, 0);
            break;
        }
    }

    void autotransferSupply(double a, double b, double c,
                            int d, int e, int f) throws GameActionException {
        double supply = rc.getSupplyLevel();
        if (supply > f) {
            RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),
                                       GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, team);
            if (nearbyAllies.length > 0) {
                RobotInfo bestTarget = null;

                for (RobotInfo ri: nearbyAllies) {
                    switch (ri.type) {
                    // Don't supply these types.
                    case HQ:
                    case MINERFACTORY:
                        continue;
                    default:
                        break;
                    }
                    if (bestTarget == null || ri.supplyLevel < bestTarget.supplyLevel) {
                        bestTarget = ri;
                    }
                    if (ri.supplyLevel == 0) {
                        break;
                    }
                }
                if (bestTarget != null) {
                    int targetTransfer = calcSupplyTransfer(supply,
                                                            bestTarget.supplyLevel,
                                                            bestTarget.location.distanceSquaredTo(rc.getLocation()),
                                                            bestTarget.type,
                                                            a, b, c, d, e);
                    if (targetTransfer > 0) {
                        rc.transferSupplies(targetTransfer, bestTarget.location);
                    }
                }
            }
        }
    }

    public boolean amove(RobotInfo[] enemies) throws GameActionException {
        if (enemies.length > 0) {
            rc.attackLocation(enemies[0].location);
            return true;
        }
        return false;
    }

    protected Msg nextMsg(int inboxBegin,
                          int inboxEnd) throws GameActionException {
        if (inboxBegin + msgIndex < inboxEnd) {
            Msg msg = new Msg(rc, 0xffff & (inboxBegin + msgIndex));
            msgIndex += msg.getPacketLen();
            if (msg.getSenderPid() == 0 && msg.getTargetPid() == 0) {
                if (msgIndex > 0) {
                    if (clearMessages(0, msgIndex)) {
                        msgIndex = 0;
                    }
                }
                return null;
            } else {
                return msg;
            }
        } else {
            System.out.println("Read no messages because mailbox full. Clearing.");
            // Mailbox full and needs clearing; impossible to have messages.
            if (clearMessages(0, inboxEnd)) {
                msgIndex = 0;
            }
            return null;
        }
    }

    // memset [start, stop) = {0}
    // Idea: distributed clear; use idle buildings (supply depot).
    // NOTE: race condition if someone tries writing as we clear; lost message.
    private boolean clearMessages(int start, int stop) throws GameActionException {
        // "Transaction" semi-guarantee
        if (Clock.getBytecodesLeft() > (stop - start)*25 + safety) {
            for (int i = start; i < stop; ++i) {
                rc.broadcast(i, 0);
            }
            return true;
        }
        return false;
    }
}
