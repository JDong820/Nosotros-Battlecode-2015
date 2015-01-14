package team094;
import battlecode.common.*;
import java.util.*;

// Protocol
// bitmask data_name
// 0xffff0000 pid
// 0x0000ff00 code
// 0x000000ff data_len
// timeout_turn; if current_turn > timeout_turn, skip
// Followed by data_len ints
abstract class Msg {
    RobotController rc;
    short targetPid; // Max 32000 simul units
    byte code;
    byte dataLen;
    int timeout;
    ArrayList<Integer> data;
    ArrayList<Integer> packet;

    // Decode
    Msg(RobotController rc, ArrayList<Integer> msg) {
        this.rc = rc;
        int header = msg.get(0);
        targetPid = pidFromHeader(header);
        code = codeFromHeader(header);
        dataLen = lenFromHeader(header);
        timeout = msg.get(1);
        data = dataFromMsg(dataLen, msg);
        packet = msg;
    }
    // Shout
    Msg(RobotController rc, byte code, ArrayList<Integer> data) {
        targetPid = (short)0xffff;
        this.code = code;
        dataLen = (byte)data.size();
        if (data.size() != dataLen) dataLen = -1;
        timeout = GameConstants.ROUND_MAX_LIMIT;
        this.data = data;
        packet = encode(targetPid, code, dataLen, timeout, data);
    }
    // Whisper
    Msg(RobotController rc,
        short targetPid, byte code, ArrayList<Integer> data) {
        this.targetPid = (short)(targetPid % 0xffff);
        this.code = code;
        dataLen = (byte)data.size();
        if (data.size() != dataLen) dataLen = -1;
        timeout = GameConstants.ROUND_MAX_LIMIT;
        this.data = data;
        packet = encode(targetPid, code, dataLen, timeout, data);
    }

    // Accesssors
    public short getTargetPid() {
        return targetPid;
    }
    public byte getCode() {
        return code;
    }
    public ArrayList<Integer> getData() {
        return data;
    }
    public ArrayList<Integer> getPacket() {
        return packet;
    }

    // Methods
    abstract public Msg nextMsg();

    // Internal
    // Use this to build a send function.
    // TODO: transactional message writing
    protected void sendWithOffset(RobotController rc,
                                  short offset) throws GameActionException {
        for (int i = 0; i < packet.size(); ++i) {
            rc.broadcast(offset+i, packet.get(i));
        }
    }
    // TODO: Can be optimized.
    public ArrayList<Integer> encode(short pid, byte code, byte len,
                                     int timeout, ArrayList<Integer> data) {
        int header = (pid << 16) | (code << 8) | len;
        ArrayList<Integer> msg = new ArrayList<Integer>(data);
        msg.add(0, header);
        msg.add(1, timeout);
        return msg;
    }
    private short pidFromHeader(int header) {
        // Top 16 bits [0,15] = pid().
        // Max active robots = 0xffff
        return (short)(header >> 16);
    }
    private byte codeFromHeader(int header) {
        // Bits [16,23] = atom().
        // Max atoms = 0xff
        return (byte)((header >> 8) & 0xff);
    }
    private byte lenFromHeader(int header) {
        return (byte)(header & 0xff);
    }
    private ArrayList<Integer> dataFromMsg(byte dataLen, ArrayList<Integer> msg) {
        try {
            return new ArrayList<Integer>(msg.subList(1, 1 + dataLen));
        } catch (Exception e) {
            System.err.println(e + ", could not extract packet data.");
            return null;
        }
    }
}
