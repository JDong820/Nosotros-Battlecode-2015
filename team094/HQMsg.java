package team094;
import battlecode.common.*;
import java.util.*;

class HQMsg extends Msg {

    // Decode
    HQMsg(RobotController rc, ArrayList<Integer> msg) {
        super(rc, msg);
    }
    // Shout
    HQMsg(RobotController rc, byte code, ArrayList<Integer> data) {
        super(rc, code, data);
    }
    // Whisper
    HQMsg(RobotController rc,
          short targetPid, byte code, ArrayList<Integer> data) {
        super(rc, targetPid, code, data);
    }

    private boolean send(RobotType targetType) throws GameActionException {
        // TODO: bytecode + packet.size()*25 < LIMIT
        if (0 < packet.size() && packet.size() <= 0x10 && true) {
            short offset;
            switch (targetType) {
            case BEAVER:
                offset = (short)0xfff0;
                break;
            default:
                offset = (short)0xffe0;
                break;
            }
            sendWithOffset(rc, offset);
            return true;
        }
        return false;
    }

    public Msg nextMsg() {
        return null;
    }
}
