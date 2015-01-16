// REQ
package team094;
import java.util.ArrayList;
import battlecode.common.*;

class SearchAction extends Action {
    final RobotType type;
    final Status status;
    public static enum Status {
        IDLE, ANY
    };


    // Internal states:
    // 0 = sending
    // 1 = waiting
    // 2 = listening
    // -1 = done
    byte state = 0;
    int broadcastTurn;
    ArrayList<Msg> results = null;


    SearchAction(Role robot, RobotType targetType, Status targetStatus) {
        super(robot);
        type = targetType;
        status = targetStatus;
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
                if (roundDiff > 1) { // Messages get discarded after 1 turn.
                    state = 0;
                    return false;
                }
                if (roundDiff == 1) {
                    ++state; 
                    return true;
                }
                // ronudDiff == 0 means we must wait.
                return false;
            case 2:
                return true;
            case -1:
                return false;
            default:
                System.err.println("Reached invalid state in action: " + this);
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
                System.err.println("Executed waiting state in action: " + this);
                break;
            case 2:
                results = fetchACKs();
                if (results.size() > 0) {
                    ++state;
                    completed = true;
                } else {
                    state = 0;
                }
                break;
            case -1:
                completed = true;
                System.err.println("Executed after completed action: " + this);
                break;
            default:
                System.err.println("Executed invalid state in action: " + this);
                break;
        }
    }

    public String toString() {
        return "{req, [" + type + ", " + status + "]}";
    }

    private boolean canReqUnits() {
        return Clock.getBytecodesLeft() > BCOST_SEND;
    }
    private void reqUnits() throws GameActionException {
        switch (status) {
            case IDLE:
                break;
            default:
                return;
        }
        agent.send(type, Code.REQ, Duck.val2ali(0));
    }

    private boolean canCheckMail() {
        return true;
    }
    private ArrayList<Msg> fetchACKs() {
        return agent.removeMail((Msg m) ->
                (m.getHeader().getCode() == Code.ACK));
    }
}
