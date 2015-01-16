// BUILD
package team094;
import java.util.ArrayList;
import battlecode.common.*;

class BuildAction extends Action {
    final RobotType type;

    int lastTry;
    boolean canSpawn = true;

    BuildAction(Role robot, RobotType targetType) {
        super(robot);
        type = targetType;
    }

    public boolean canAct() {
        if (lastTry < Clock.getRoundNum())
            canSpawn = true;
        return (!completed &&
                agent.rc.getTeamOre() >= type.oreCost &&
                agent.rc.isCoreReady() &&
                canSpawn);
    }
    public void act() throws GameActionException {
        lastTry = Clock.getRoundNum();
        if (spawn(Direction.NORTH, type)) {
            System.out.println("Completed action: " + this);
            completed = true;
        } else {
            canSpawn = false;
        }
    }

    public String toString() {
        return "{build, [" + type + "]}";
    }


    private boolean spawn(Direction d,
            RobotType type) throws GameActionException {
        int[] offsets = {0,1,-1,2,-2,3,-3,4};
        for (int offset: offsets) {
            Direction trialDir = Duck.i2d((Duck.val2i(d)+offset+8)%8);
            if (agent.rc.canSpawn(trialDir, type)) {
                agent.rc.spawn(trialDir, type);
                return true;
            }
        }
        return false;
    }
}
/*
class BuildAction extends Action {
    boolean attempted = false;

    public final RobotType type;
    Integer buildCount;

    BuildAction(RobotController rc, RobotType arg, int count) {
        super(rc);
        assert(count > 0);
        type = arg;
        buildCount = count;
    }
    BuildAction(RobotController rc, RobotType arg) {
        this(rc, arg, 1);
    }


    public void reset() {
        attempted = false;
    }
    public boolean canExecute() {
        return (agent.getTeamOre() >= type.oreCost && agent.isCoreReady());
    }
    public void enact() throws GameActionException {
        System.out.println("Executing task: " + this);
        attempted = true;
        if (spawn(Direction.NORTH, type)) {
            --buildCount;
        }
        if (buildCount == 0) {
            System.out.println("Completed action: " + this);
            completed = true;
        }
    }
    public ArrayList<Task> getSubtasks() {
        switch (type) {
            case BEAVER:
                return null;
            case MINER:
                BuildAction tmp = new BuildAction(agent, RobotType.MINERFACTORY);
                return Duck.t2alt(new Task(tmp));
            default:
                System.err.println("Subtasks not implemented for " + this);
                return null;
        }
    }



}
*/
