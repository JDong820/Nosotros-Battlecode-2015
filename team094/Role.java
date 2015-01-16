package team094;
import java.util.ArrayList;
import java.util.Random;
import battlecode.common.*;

abstract class Role {
    final RobotController rc;
    final int id;
    final Team team;
    final Team enemyTeam;
    final MapLocation base;
    final MapLocation enemy;
    final int range;
    final int baseToEnemySquared;

    final Random rand;

    // Status
    MapLocation location; // Update cost: 1
    boolean coreReady; // Update cost: 10
    boolean weaponReady; // Update cost: 10

    ArrayList<Msg> messages;
    int inboxIndex; // Absolute index.


    // Try to always have at least this many bytecodes remaining.
    final int safety = 1000;


    Role(RobotController rc) {
        this.rc = rc;
        id = rc.getID();
        team = rc.getTeam();
        enemyTeam = team.opponent();
        base = rc.senseHQLocation();
        enemy = rc.senseEnemyHQLocation();
        range = rc.getType().attackRadiusSquared;
        baseToEnemySquared = base.distanceSquaredTo(enemy);

        rand = new Random(id);

        location = rc.getLocation();
    }


    public int getID() {
        return id;
    }
 
    abstract Msg fetchNextMsg() throws GameActionException;
    abstract void update();
    abstract void execute();

    abstract protected void handleMessage(Msg m) throws GameActionException;

    protected void updateInbox() {
        try {
            messages = new ArrayList<Msg>();
            Msg tmp = fetchNextMsg();
            while (tmp != null) {
                messages.add(tmp);
                tmp = fetchNextMsg();
            }
        } catch (GameActionException e) {
            System.err.println("Could not fetch mail.");
        }
    }   
    protected ArrayList<Msg> removeMail(MailFilter f) {
        ArrayList<Msg> results = new ArrayList<Msg>(messages.size());
        for (int i = messages.size() - 1; i >= 0; --i) {
            if (f.pass(messages.get(i))) {
                results.add(messages.remove(i));
            }
        }
        return results;
    }
    protected boolean send(RobotType targetType,
            Code c, ArrayList<Integer> data) throws GameActionException {
        Msg m = new Msg(rc.getID(), Duck.val2i(c), data);
        return send(targetType, m);
    }
    private boolean send(RobotType targetType, Msg msg) throws GameActionException {
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
                    while (nextMessageOffset < 0x1000 &&
                           rc.readBroadcast(inboxOffset + nextMessageOffset) != 0) {
                        Header h = new Header(rc, inboxOffset + nextMessageOffset);
                        nextMessageOffset += h.getPacketLen();
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

    public void autotransferSupply() throws GameActionException {
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

    public void autotransferSupply(double a, double b, double c,
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
    
    protected Msg fetchNextMsg(int inboxBegin,
                               int inboxEnd) throws GameActionException {
        final Header h = Header.readNextUnreadHeader(rc, inboxBegin, inboxEnd,
                                                     inboxIndex);
        // Only get null headers if out of range; looking past inboxEnd.
        if (h == null) { 
            System.out.println("Mailbox full! Clearing.");
            // Clear the entire mailbox.
            clearMessages(inboxBegin, inboxEnd);
            inboxIndex = inboxBegin;
            return null;
        }

        // A sender of 0xffffffff indicates we are at the end of the mailbox.
        // Note that max senderPid is 0x0000fffe.
        if (h.getSenderPid() == -1) {
            // If all of the messages from the beginning have been read,
            // clear the mailbox.
            if (inboxBegin < inboxIndex) {
                final Header pre = Header.readNextUnreadHeader(rc,
                        inboxBegin, inboxIndex, inboxBegin);
                // Note: untested behavior near end of mailbox.
                if (pre == null) {
                    // Tricky optimization where the header holds the mailbox end.
                    clearMessages(inboxBegin, h.getAbsoluteOffset());
                    //inboxIndex = inboxBegin;
                }
            }
            // TODO: Figure out how to distinguish between awaiting new message
            // and read head too forward due to previous clearMessages call.
            inboxIndex = inboxBegin;
            return null;
        }

        // Cruft caused by using relative index.
        inboxIndex += h.getPacketLen();

        if (h.getTargetPid() == ((0xffff & id) % 0xffff) ||
            h.getTargetPid() == 0xffff) {
            rc.broadcast(h.getAbsoluteOffset(), (0xffff0000 | (0x00ff & h.getDataLen())));
            return new Msg(rc, h);
        }

        //System.out.println("Skipping message @ 0x" +
        //        Integer.toHexString(inboxIndex));
        // Skip over messages addresed to others.
        return fetchNextMsg(inboxBegin, inboxEnd);
    }

    // memset [start, stop) = {0}
    // NOTE: race condition if someone tries writing as we clear; lost message.
    // ^ should never happen with battlecode 'threading' model.
    // NOTE: if doesn't finish in a turn, everyone joins in in the derp.
    private void clearMessages(int start, int stop) throws GameActionException {
        // Reverse order means we can stop whenever.
        for (int i = stop - 1; i >= start; --i) {
            rc.broadcast(i, 0);
        }
    }

    public void debugPing(RobotType type, int id, int seq) throws GameActionException {
        ArrayList<Integer> pingSeq = new ArrayList<Integer>(1);
        pingSeq.add(seq);
        send(type, new Msg(this.id, id, 0xff, pingSeq));
    }
}
