package team094;
import java.util.ArrayList;
import battlecode.common.*;

class Task {
    Action action;

    int nodeIndex = 0;
    int treeSize = 1;

    Task(Action a) {
        action = a;
    }

            
    // Returns true if there are actionable tasks.
    public boolean reset() { // Set task to the first actionable task.
        nodeIndex = 0;
        if (!getCurrent().canAct())
            nextActionable();
        return nodeIndex < treeSize; 
    }
    public Action getCurrent() {
        if (nodeIndex < treeSize) {
            return action;
        } else {
            return null;
        }
    }
    public void nextActionable() {
        while (++nodeIndex < treeSize && !getCurrent().canAct());
    }
}
    /*
class Task {
    final Action action; 

    ArrayList<Task> subtasks = null;
    int treeIndex = 0;
    int treeSize = 1;


    Task(Action action) {
        this.action = action;
    }

    
    public boolean isComplete() {
        return action.isComplete();
    }
    public Task getCurrentSubtask() {
        Task node = new Task(action);
        for (int i = 0; i < treeIndex; ++i) {
            node = null;
        }
        return node; //node;
    }
    public boolean isActionable() {
        return getCurrentSubtask().action.canExecute();
    }
    public void act() throws GameActionException {
        getCurrentSubtask().action.enact();
    }
    public void reset() {
        cleanCompletedTasks();
        treeIndex = 0;

        if (!isActionable()) {
            if(!nextActionableTask()) {
                makeSubtasks();
                reset();
            }
        }
    }
    public boolean nextActionableTask() {
        while (true) {
            if (++treeIndex > treeSize) {
                return false;
            }
            if (isActionable()) break;
        }
        return true;
    }


    //public void makeAllSubtasks() {
    //    // Recursion
    //}
    private void makeSubtasks() {
        subtasks = action.getSubtasks();
    }

    // Issue: how to deal with macro?
    // e.g. too many minerals, need more helipads,
    // how to build more than 1 helipad?
   // private ArrayList<Task> makeSubtasks(BuildAction a) {
   //     ArrayList<Task> newTasks = new ArrayList<Task>();
   //     switch (a.type) {
   //         case BEAVER:
   //             break;
   //         case MINER:
   //             BuildAction tmp = new BuildAction(action.agent,
   //                     RobotType.MinerFactory);
   //             newTasks.add(new Task(tmp));
   //         case deafult:
   //             break;
   //     }
   //     subtasks.addAll(newTasks);
   //     if (newTasks.size() == 0) {
   //         return null;
   //     } else {
   //         treeSize += newTasks.size();
   //         return newTasks;
   //     }
   // }

    // Traverse the tree of tasks.
    // NOTE: Very suboptimal.
    // Need to flatten list to properly evaluate things.
    private void cleanCompletedTasks() {
        ArrayList<Task> tmp = new ArrayList<Task>(subtasks.size());
        tmp.addAll(subtasks);
        // TODO: optimize
        for (Task subtask : tmp) {
            if (subtask.isComplete()) {
                subtasks.remove(subtask);
            }
        }
    }
}
    */
