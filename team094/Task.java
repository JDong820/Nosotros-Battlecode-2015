package team094;
import java.util.ArrayList;
import battlecode.common.*;

class Task {
    final Action action; 

    ArrayList<Task> subtasks = null;
    int treeIndex = 0;
    int treeSize = 1;


    Task(Action action) {
        this.action = action;
    }


    public Action getAction() {
        return action;
    }

    public void reset() {
        // TODO: autocleanup completed actions.

        treeIndex = 0;
        if (!getCurrentSubtask().getAction().canExecute()) {
            nextActionableTask();
        }
    }
    public boolean nextActionableTask() {
        while (true) {
            if (++treeIndex > treeSize) {
                return false;
            }
            if (getCurrentSubtask().getAction().canExecute()) break;
        }
        return true;
    }

    public void makeAllSubtasks() {
        // Recursion
    }
    /*
    public ArrayList<Task> makeSubtasks() {
        makeSubtasks(action);
    }

    // Issue: how to deal with macro?
    // e.g. too many minerals, need more helipads,
    // how to build more than 1 helipad?
    private ArrayList<Task> makeSubtasks(BuildAction a) {
        ArrayList<Task> newTasks = new ArrayList<Task>();
        switch (a.type) {
            case BEAVER:
                break;
            case MINER:
                BuildAction tmp = new BuildAction(action.agent,
                        RobotType.MinerFactory);
                newTasks.add(new Task(tmp));
            case deafult:
                break;
        }
        subtasks.addAll(newTasks);
        if (newTasks.size() == 0) {
            return null;
        } else {
            treeSize += newTasks.size();
            return newTasks;
        }
    }
    */

    // Traverse the tree of tasks.
    // NOTE: Very suboptimal.
    // Need to flatten list to properly evaluate things.
    private Task getCurrentSubtask() {
        Task node = new Task(action);
        for (int i = 0; i < treeIndex; ++i) {
        }
        return node; //node;
    }
}
