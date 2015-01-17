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
            setComplete();
        } else {
            canSpawn = false;
        }
    }
    public BuildAction copy() {
        // TODO: add count
        return new BuildAction(agent, type);
    }

    public String toString() {
        return "{build, [" + type + "]}";
    }


    protected void setComplete() {
        System.out.println("Completed action: " + this);
        completed = true;
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
    */
