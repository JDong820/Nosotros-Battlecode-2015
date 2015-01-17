package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class HQ extends Role {
    final Params p;

    Task task;

    int goalBeaverCount;
    // TODO: replace with a UnitInfo class or something
    int numBeavers;
    ArrayList<RobotInfo> beavers;

    ArrayList<Integer> benchBytecodes;


    HQ(RobotController rc) {
        super(rc);
        p = new Params();
        //p.BENCHMARKING_ON = true;

        SearchAction s = new SearchAction(this,
                                          RobotType.BEAVER, Status.ANY); // Select IDLE
        BuildAction b = new BuildAction(this, RobotType.BEAVER);
        Task subtask = new Task(b);
        //subtask.addSerial(b);

        task = new Task();
        task.addSerial(subtask);
        subtask.addParallel(s);

        System.out.println("Initial task: \n" + task + "\n");
        //task.addSerial(s);

        goalBeaverCount = calcBeaverCap(p, baseToEnemySquared);

        // TODO: use constants file.
        inboxIndex = 0x0000;
        //System.out.println("F(Params) goalBeaverCount="+ goalBeaverCount);
    }

    // Should only get called once per turn.
    void update() {
        coreReady = rc.isCoreReady();
        weaponReady = rc.isWeaponReady();

        RobotInfo[] robots = rc.senseNearbyRobots(0x7fffffff, team);
        numBeavers = 0;
        beavers = new ArrayList<RobotInfo>();
        for (RobotInfo ri : robots) {
            switch (ri.type) {
            case BEAVER:
                ++numBeavers;
                beavers.add(ri);
                break;
            default:
                break;
            }
        }

        updateInbox();
    }

    void execute() {
        try {
            int benchMsgCount = 0;
            benchBytecodes = new ArrayList<Integer>();


            if (Clock.getRoundNum() % 100 == 0) {
                rc.addMatchObservation(Clock.getRoundNum() + ":  " + rc.getTeamOre());
            }
            if (Clock.getRoundNum() == 83) {
                rc.resign();
            }

            if (coreReady && rc.isWeaponReady()) {
                RobotInfo[] enemies = rc.senseNearbyRobots(range, enemyTeam);
                coreReady ^= amove(enemies);
            }

            if (task.reset()) {
                while (Clock.getBytecodesLeft() > 500 + safety) {
                    int benchBytecodesBefore = Clock.getBytecodesLeft();


                    Task curr = task.getCurrent();
                    if (curr == null) {
                        // If there are no act-able tasks left,
                        // recheck from the beginning.
                        if (!task.reset()) break;
                    }
                    //assert (curr.canAct());
                    //System.out.println("Executing action: " + curr.getAction());
                    curr.getAction().act();
                    task.nextActionable();


                    benchBytecodes.add(benchBytecodesBefore - Clock.getBytecodesLeft());
                }
            } else {
                //System.out.println("HQ has no tasks to execute!");
            }
            if (Clock.getBytecodesLeft() > safety) {
                autotransferSupply(p.SUPPLY_HQ_A,
                                   p.SUPPLY_HQ_B,
                                   p.SUPPLY_HQ_C,
                                   p.SUPPLY_HQ_D,
                                   p.SUPPLY_HQ_E,
                                   p.SUPPLY_HQ_F);
            }
            /*
            // Handle as many messages as possible.
            int benchMsgCount = 0;
            while (unreadMsg != null && Clock.getBytecodesLeft() > safety) {
                benchMsgCount++;
                handleMessage(unreadMsg);
                updateInbox();
            }
            */
            // Benchmarking
            if (p.BENCHMARKING_ON) {
                Integer benchSum = 0;
                double benchAverage = 0;
                if(!benchBytecodes.isEmpty()) {
                    for (int benchLoop: benchBytecodes) {
                        benchSum += benchLoop;
                    }
                    benchAverage = benchSum.doubleValue() / benchBytecodes.size();
                }
                System.out.print("Turn stats:\n");
                System.out.print("Bytecodes used: " + (10000 - Clock.getBytecodesLeft()));
                if (benchAverage > 0)
                    System.out.print(". Average loop cost: " + benchAverage +
                                     ". Loops executed: " + benchBytecodes.size() + ".");
                if (benchMsgCount > 0) {
                    System.out.print("\nHandled " + benchMsgCount + " total messages.");
                }
                System.out.print("\n");
            }
        } catch (Exception e) {
            System.err.println(e.toString() + ": HQ Exception");
            System.err.println(e.getStackTrace()[0] + "\n");
        }
    }

    protected void handleMessage(Msg msg) throws GameActionException {
        switch (msg.getHeader().getCode()) {
        case ACK: // ACK can build.
            // Protocol
            // {builder_location}
            final MapLocation loc = Duck.i2ml(msg.getData().get(0));

            final Header debug2 = new Header(msg);
            System.out.println(debug2.getTargetPid() + " ! {" +
                               debug2.getSenderPid() + ", " +
                               debug2.getTimeout() + ", " +
                               debug2.getCode() + ", " +
                               debug2.getDataLen() + ", {loc: " + loc + "}} (ACK)");
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

    // Returns false when all possible spawn locations are blocked.
    boolean spawn(Direction d, RobotType type) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = Duck.i2d((Duck.val2i(d)+offset+8)%8);
            if (rc.canSpawn(trialDir, type)) {
                rc.spawn(trialDir, type);
                return true;
            }
        }
        return false;
    }

    private static int calcBeaverCap(Params p, int dsquared) {
        double goal = p.GOAL_BEAVERS_A*dsquared;
        return goal > p.GOAL_BEAVERS_B ? (int)goal : p.GOAL_BEAVERS_B;
    }

    protected Msg fetchNextMsg() throws GameActionException {
        return fetchNextMsg(0x0000, 0x1000);
    }
}
