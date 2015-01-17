package team094;
import java.util.ArrayList;
import battlecode.common.*;

class Task {
    final Task parent;
    final Action action;

    ArrayList<Task> children = null;

    int nodeIndex = 0;
    int subtreeSize = 1; // Subtree includes this.

    Task() { // Root node constructor.
        parent = null;
        action = null; // Eventually replace this with a WinAction.
    }
    Task(Task p, Action a) {
        parent = p;
        action = a;
    }


    public Task getParent() {
        return parent;
    }
    public ArrayList<Task> getChildren() {
        return children;
    }
    public Action getAction() {
        return action;
    }

            
    // Returns true if there are actionable tasks.
    public boolean reset() { // Set task to the first actionable task.
        nodeIndex = 0;
        Task curr = getCurrent();
        if (curr.action == null || !curr.action.canAct())
            nextActionable();
        //System.out.println("Reset to action: " + getCurrent().action);
        //System.out.println("nodeIndex: " + nodeIndex + ", subtreeSize: " + subtreeSize);
        return nodeIndex < subtreeSize; 
    }
    public Task getCurrent() {
        if (nodeIndex < subtreeSize) {
            return getNodeByPriority(nodeIndex);
        } else {
            return null;
        }
    }
    public void nextActionable() {
        while (++nodeIndex < subtreeSize &&
               getCurrent().action != null &&
               !getCurrent().action.canAct());
    }

    public void addParallel(Action a) {
        parent.addSerial(a);
    }
    public void addSerial(Action a) {
        if (children == null)
            children = new ArrayList<Task>();
        children.add(new Task(this, a));
        subtreeSizeUpdate(1);
    }


    private void subtreeSizeUpdate(int n) {
        if (parent != null) {
            parent.subtreeSizeUpdate(n);
        }
        subtreeSize += n;
    }
    // Priority (min, max) = (subtreeSize - 1, 0)
    // Automagically returns nth most important node.
    private Task getNodeByPriority(int n) {
        if (n == 0) {
            return this;
        } else if (n - 1 < children.size()) {
            return children.get(n - 1);
        } else {
            // Recursion
            System.err.println("Searched for node of priority: " + n + ". Not implemented.");
            return null;
        }
    }
}
/*
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
