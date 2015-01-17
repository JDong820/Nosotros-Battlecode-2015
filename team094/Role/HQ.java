package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class HQ extends Role {
    final boolean benchBytecodeOutput = false;
    SearchAction req = new SearchAction(this, RobotType.BEAVER, Status.IDLE);
    SelectAction sel = new SelectAction(this, new SelectFilter() {
        @Override
        public double eval(Object o) {
            Msg m = (Msg)o;
            MapLocation loc = Duck.i2ml(m.getData().get(0));
            return loc.distanceSquaredTo(base.add(Direction.SOUTH));
        }
        public String toString() { return "distance_filter"; }
    }, 1, Duck.al2alo(req.results));

    int state = 0; // TODO: put in task.

    final Params p;

    Task task;

    int goalBeaverCount;
    // TODO: replace with a UnitInfo class or something
    ArrayList<RobotInfo> beavers;

    ArrayList<Integer> benchBytecodes;


    HQ(RobotController rc) {
        super(rc);
        p = new Params();
        goalBeaverCount = calcBeaverCap(p, baseToEnemySquared);

        BuildAction build = new BuildAction(this, RobotType.BEAVER);

        task = new Task();
        task.addSerial(build.copy());
        //task.addSerial(req);

        System.out.println("Initial task: \n" + task + "\n");
        //task.addSerial(s);


        // TODO: use constants file.
        inboxIndex = 0x0000;
        //System.out.println("F(Params) goalBeaverCount="+ goalBeaverCount);
    }

    // Should only get called once per turn.
    void update() {
        coreReady = rc.isCoreReady();
        weaponReady = rc.isWeaponReady();

        RobotInfo[] robots = rc.senseNearbyRobots(0x7fffffff, team);
        beavers = new ArrayList<RobotInfo>();
        for (RobotInfo ri : robots) {
            switch (ri.type) {
                case BEAVER:
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
                rc.addMatchObservation(Clock.getRoundNum() + ":  "
                        + calcScoreEvaluation(rc, beavers));
            }
            if (Clock.getRoundNum() == 200) {
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
                        if (!task.reset()) {
                            break;
                        } else {
                            curr = task.getCurrent();
                        }
                    }
                    //assert (curr.canAct());
                    //System.out.println("Executing action: " + curr.getAction());
                    curr.getAction().act();
                    task.nextActionable();


                    benchBytecodes.add(benchBytecodesBefore - Clock.getBytecodesLeft());
                }
            } else {
                if (beavers.size() < goalBeaverCount) {
                    task.addSerial(new BuildAction(this, RobotType.BEAVER));
                }
                if (state == 0 &&
                        RobotType.MINERFACTORY.oreCost - 50 <= rc.getTeamOre()) {
                    task.addSerial(req);
                    state = 1;
                }
                if (state == 1 &&
                        req.isComplete()) { 
                    task.addSerial(sel);
                    state = 2;
                }
                if (state == 2 &&
                        sel.isComplete()) {
                    //task.addSerial(new MoveCommand(this,
                    //            sel.getResults(), base.add(Direction.SOUTH)));
                }
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
            // Benchmarking
            if (benchBytecodeOutput) {
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
    }
    /*
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
    */
    protected Msg fetchNextMsg() throws GameActionException {
        return fetchNextMsg(0x0000, 0x1000);
    }


    private static int calcBeaverCap(Params p, int dsquared) {
        double goal = p.GOAL_BEAVERS_A*dsquared;
        return goal > p.GOAL_BEAVERS_B ? (int)goal : p.GOAL_BEAVERS_B;
    }
    private static int calcScoreEvaluation(RobotController rc,
            ArrayList<RobotInfo> beavers) {
        return beavers.size() * RobotType.BEAVER.oreCost + (int)rc.getTeamOre();
    }
}
