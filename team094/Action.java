// Each robot can execute its own actions.
package team094;
import java.util.ArrayList;
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

    //abstract public void reset(); // Does not affect `completed`.
    //abstract public boolean canExecute();
    //abstract public void enact() throws GameActionException;
    //abstract public ArrayList<Task> getSubtasks();
}
