package team094;
import battlecode.common.*;
import java.util.*;

class BeaverMsg extends Msg {

    // Decode
    BeaverMsg(RobotController rc, ArrayList<Integer> msg) {
        super(rc, msg);
    }
    // Shout
    BeaverMsg(RobotController rc, byte code, ArrayList<Integer> data) {
        super(rc, code, data);
    }
    // Whisper
    BeaverMsg(RobotController rc,
              short targetPid, byte code, ArrayList<Integer> data) {
        super(rc, targetPid, code, data);
    }


    public BeaverMsg nextMsg() {
        /*
        int header = readBroadcast(0xfff0);
        if targetPidOf(header
        if () {
            return msg;
        }
        */
        return null;
    }
}
