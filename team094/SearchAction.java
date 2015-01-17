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

class SearchAction extends Action {
    final RobotType type;
    final Status status;

    // Internal states:
    // 0 = sending
    // 1 = waiting
    // 2 = listening
    // 4 = completed
    // -1 = done
    byte state = 0;
    int broadcastTurn;
    ArrayList<Msg> results = null;


    SearchAction(Role robot, RobotType targetType, Status status) {
        super(robot);
        type = targetType;
        this.status = status;
    }


    public ArrayList<Msg> getResults() {
        // assert (completed);
        return results;
    }
    public boolean canAct() {
        switch (state) {
        case 0:
            return canReqUnits();
        case 1:
            // When returning false,
            // next execution will not be on the same turn.
            int roundDiff = Clock.getRoundNum() - broadcastTurn;
            if (roundDiff > 1) { // Messages get discarded every turn.
                System.err.println("Missed turn to check inbox. Requerying.");
                state = 0;
                return false;
            }
            if (roundDiff == 1) {
                state = 2;
                return true;
            }
            // ronudDiff == 0 means we must wait.
            return false;
        case 2:
            return true;
        case -1:
            return false;
        case 4: // Already completed.
            return false;
        default:
            System.err.println("Reached invalid state (" + state +
                               ")in action: " + this);
            return false;
        }
    }
    public void act() throws GameActionException {
        switch (state) {
        case 0:
            reqUnits();
            broadcastTurn = Clock.getRoundNum();
            state = 1;
            break;
        case 1:
            // TODO: add request probes while waiting instead of idling?
            System.err.println("Executed waiting state (!) in action: " + this);
            break;
        case 2:
            results = fetchACKs();
            if (results.size() > 0) {
                setComplete();
            } else {
                state = 0;
            }
            break;
        case -1:
            completed = true;
            System.err.println("Executed after completed (!) action: " + this);
            break;
        default:
            System.err.println("Executed invalid state (!) in action: " + this);
            break;
        }
    }
    public SearchAction copy() {
        return new SearchAction(agent, type, status);
    }

    public String toString() {
        return "{req, [" + type + ", " + status + "]}";
    }


    protected void setComplete() {
        state = 4;
        System.out.println("Completed action: " + this);
        completed = true;
    }


    private boolean canReqUnits() {
        return Clock.getBytecodesLeft() > BCOST_SEND;
    }
    private void reqUnits() throws GameActionException {
        agent.send(type, Code.REQ, Duck.val2ali(status));
    }

    private boolean canCheckMail() {
        return true;
    }
    private ArrayList<Msg> fetchACKs() {
        return agent.removeMail(new MailFilter() {
            @Override
            public boolean pass(Msg m) {
                return (m.getHeader().getCode() == Code.ACK);
            }
        });
    }
}
