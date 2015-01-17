// Each robot can execute its own actions.
package team094;
import battlecode.common.*;

// Maybe make abstract to customize params.
// {buildAction, [Arg]}
// {trainAction, [Arg]}
// {analyzeAction, [Arg]}
abstract class Action {
    final Role agent;

    boolean completed = false;

    static final int BCOST_SEND = 500;
    
    Action(Role robot) {
        agent = robot;
    }

   
    public boolean isComplete() {
        return completed;
    }

    abstract public boolean canAct();
    abstract public void act() throws GameActionException;
}
