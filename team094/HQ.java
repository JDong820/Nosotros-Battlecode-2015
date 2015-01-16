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
        p.BENCHMARKING_ON = true;

        task = new Task(new BuildAction(rc, RobotType.BEAVER));
        goalBeaverCount = calcBeaverCap(p, baseToEnemySquared);

        // TODO: use constants file.
        inboxIndex = 0x0000;
        //System.out.println("F(Params) goalBeaverCount="+ goalBeaverCount);
    }

    // Bytecode cost: 400-1000
    // 15.01T16:21
    void updateInbox() {
        try {
            unreadMsg = fetchNextMsg();
        } catch (GameActionException e) {
            System.err.println("Could not fetch mail.\n");
        }
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
            if (Clock.getRoundNum() == 1000) {
                rc.resign();
            }

            // Set task to the first actionable task.
            task.reset();
            while (Clock.getBytecodesLeft() > 500 + safety) {
                int benchBytecodesBefore = Clock.getBytecodesLeft();
                
                Action action = task.getAction();
                if (action.canExecute()) {
                    action.enact();
                }
                 
                // Keeps iterating until task gets to the root node (victory?).
                if (!task.nextActionableTask()) break;
               
                // Add bytecodes used per loop.
                benchBytecodes.add(benchBytecodesBefore - Clock.getBytecodesLeft());
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
            if (coreReady && rc.isWeaponReady()) {
                RobotInfo[] enemies = rc.senseNearbyRobots(range, enemyTeam);
                coreReady ^= amove(enemies);
            }
            if (coreReady) {
                // Make Beavers.
                if (numBeavers < goalBeaverCount) {
                    if (rc.getTeamOre() >= 100) {
                        //spawn(Direction.NORTH);
                        coreReady ^= spawn(Duck.i2d((int)(rand.nextDouble()*8)),
                                RobotType.BEAVER);
                    }
                }
            }
            // Check if done with build order.
            if (boIndex < bo.buildings.size()) {
                RobotType nextBuilding = bo.buildings.get(boIndex);
                if (nextBuilding != null && nextBuilding.oreCost <= rc.getTeamOre()) {
                    ArrayList<Integer> data = Duck.val2al(nextBuilding);
                    // TODO: implement loc chooser.
                    data.addAll(Duck.val2al(base.add(0, 1)));
                    send(RobotType.BEAVER, new Msg(rc, 0x02, data));
                }
            }
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
                System.out.println("Ended with " + Clock.getBytecodesLeft() + " bytecodes left.");
                System.out.println("Average loop cost: " + benchAverage);
                System.out.println("" + benchBytecodes); // TODO: fix
                if (benchMsgCount > 0) {
                    System.out.println("Handled " + benchMsgCount + " total messages.");
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString() + ": HQ Exception\n");
            e.printStackTrace();
        }
    }

    protected void handleMessage(Msg msg) throws GameActionException {
        switch (msg.getHeader().getCode()) {
            case 0x02: // ACK can build.
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
            case 0xff: // Debug ping
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
            Direction trialDir = Duck.i2d((Duck.d2i(d)+offset+8)%8);
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
