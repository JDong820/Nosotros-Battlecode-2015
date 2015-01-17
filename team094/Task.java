package team094;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;
import battlecode.common.*;

class Task {
    Task parent;
    final Action action;
    // Internal debugging
    final int creationTurn;

    ArrayList<Task> children = null;

    int nodeIndex = 0;
    int subtreeSize = 1; // Subtree includes this.


    Task(Task p, Action a) {
        parent = p;
        if (a != null)
            action = a.copy(); // TODO: Dubious copy
        else
            action = null;
        creationTurn = Clock.getRoundNum();
    }
    Task(Action a) {
        this(null, a);
    }
    Task() { // Root node constructor.
        this(null, null); // Eventually replace this with a WinAction.
    }


    public Task getParent() {
        return parent;
    }
    public Action getAction() {
        return action;
    }
    public ArrayList<Task> getChildren() {
        return children;
    }
    public int getSubtreeSize() {
        return subtreeSize;
    }


    // Returns true if there are actionable tasks.
    public boolean reset() { // Set task to the first actionable task.
        nodeIndex = 0;
        Task curr = getCurrent();
        if (curr.action == null || !curr.action.canAct())
            nextActionable();
        //System.out.print("Reset to task: " + getCurrent());
        //System.out.println("@ nodeIndex: " + nodeIndex + ".");
        return nodeIndex < subtreeSize;
    }
    public Task getCurrent() {
        return getNodeByPriority(nodeIndex);
    }
    public void nextActionable() {
        // TODO: memoize the repeated calls to getCurrent()
        while (++nodeIndex < subtreeSize &&
               getCurrent().action != null &&
               !getCurrent().action.canAct());
    }

    public void addParallel(Action a) {
        parent.addSerial(a);
    }
    public void addParallel(Task t) {
        parent.addSerial(t);
    }
    public void addSerial(Action a) {
        if (children == null)
            children = new ArrayList<Task>();
        children.add(new Task(this, a));
        subtreeSizeUpdate(1);
    }
    public void addSerial(Task t) {
        if (children == null)
            children = new ArrayList<Task>();
        t.parent = this;
        children.add(t);
        subtreeSizeUpdate(t.getSubtreeSize());
    }

    public String toString() {
        String output = action + "(0)\n";
        Queue<Task> currentLevel = new ArrayDeque<Task>();
        currentLevel.add(this);
        int count = subtreeSize - 1;
        Task node = null;
        while (0 < count) {
            --count;
            node = currentLevel.remove();
            if (node.getChildren() != null) {
                for(Task child: node.getChildren()) {
                    --count;
                    output += child.getAction() +
                              "(" + (subtreeSize - 2 - count) + ") ";
                    currentLevel.add(child);
                }
            }
            output += "| ";
        }
        return output;
    }


    private void subtreeSizeUpdate(int n) {
        subtreeSize += n;
        if (parent != null) {
            parent.subtreeSizeUpdate(n);
        }
    }
    // Priority (min, max) = (subtreeSize - 1, 0)
    // Automagically returns nth most important node.
    private Task getNodeByPriority(int n) {
        //System.out.println("Grabbing node with prio: " + n);
        if (nodeIndex < subtreeSize) {
            if (n == 0) {
                return this;
            } else if (n - 1 < children.size()) {
                return children.get(n - 1);
            } else {
                // Level order (breadth-first) traversal.
                // Better algorithm may involve:
                // https://en.wikipedia.org/wiki/Tree_traversal#Infinite_trees
                // or "duplicate" counting to maximize task utility.
                Queue<Task> currentLevel = new ArrayDeque<Task>();
                currentLevel.addAll(children);
                n = n - children.size();
                Task node = null;
                while (n != 0) {
                    --n;
                    node = currentLevel.remove();
                    if (n == 0)
                        return node;
                    for(Task child: node.getChildren()) {
                        --n;
                        if (n == 0)
                            return child;
                        currentLevel.add(child);
                    }
                }
            }
        }
        return null;
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
    // }

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
