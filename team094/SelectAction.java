// REQ
//
// * ! {req, Status};
//receive all
//  {ACK, _Data} ->
//      ret_messages.append({ACK, _Data})
//end.
package team094;
import java.util.ArrayList;
import battlecode.common.*;

class SelectAction extends Action {
    final SelectFilter filter;
    final ArrayList<Object> data;

    // Results
    int robotId;


    SelectAction(Role r, SelectFilter f, ArrayList<Object> d) {
        super(r);
        filter = f;
        data = d; // d.copy()?
    }


    public int getResults() {
        return robotId;
    }
    public boolean canAct() {
        return false;
    }
    public void act() throws GameActionException {
    }
    public SelectAction copy() {
        return null; // TODO: remove this.
    }
    public SelectAction copy(ArrayList<Object> d) {
        return new SelectAction(agent, filter, d);
    }

    public String toString() {
        return "{select, [" + filter + "]}";
    }


    protected void setComplete() {
        System.out.println("Completed action: " + this);
        completed = true;
    }
}
