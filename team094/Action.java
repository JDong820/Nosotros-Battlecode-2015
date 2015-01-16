// Each robot can execute its own actions.
package team094;
import java.util.ArrayList;
import battlecode.common.*;

// Maybe make abstract to customize params.
// {buildAction, [Arg]}
// {trainAction, [Arg]}
// {analyzeAction, [Arg]}
abstract class Action{
    final RobotController agent;

    Integer attempted = null; // Turn attempted
    boolean completed = false;

    // Don't do this.
    Action(RobotController rc) {
        agent = rc;
    
        // This will cause it to be auto-removed.
        completed = true;
    }


    abstract public boolean canExecute();
    abstract public void enact() throws GameActionException;
}
