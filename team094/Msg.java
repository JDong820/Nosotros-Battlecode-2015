package team094;
import battlecode.common.*;
import java.util.*;

// Protocol
// Header 1
// 0xffff0000 self()
// 0x0000ffff Pid
// Header 2
// 0xffff0000 Timeout
// 0x0000ff00 Code
// 0x000000ff DataLen
// Data
class Msg {
    // Packet data; can you feel the immutable state?
    final int senderPid; // From:
    final int targetPid; // To:
    final int timeout;
    final int code;
    final short dataLen;
    final ArrayList<Integer> data;

    final RobotController rc;
    final ArrayList<Integer> packet;

    static final byte headerLen = 2;

    // Read and Decode
    Msg(RobotController rc, int offset) throws GameActionException{
        this(rc, readMessageFromOffset(rc, offset)); 
    }
    // Decode packet
    Msg(RobotController rc, ArrayList<Integer> msg) {
        if (msg == null) {
            senderPid = 0;
            targetPid = 0;
            timeout = -1;
            code = 0;
            dataLen = -1;
            data = null;

            this.rc = null;
            packet = null;
        } else {
            int header1 = msg.get(0);
            int header2 = msg.get(1);

            senderPid = senderPidFromHeader(header1);
            targetPid = targetPidFromHeader(header1);
            timeout = timeoutFromHeader(header2);
            code = codeFromHeader(header2);
            dataLen = dataLenFromHeader(header2);
            data = dataFromMsg(dataLen, msg);

            this.rc = rc;
            packet = msg;
        }
    }
    // Shout
    Msg(RobotController rc, int code, ArrayList<Integer> data) {
        senderPid = rc.getID() % 0x10000;
        targetPid = 0xffff; // Reserved as allcast.
        timeout = 2000;// GameConstants.ROUND_MAX_LIMIT;
        this.code = code;
        if (data == null) {
            dataLen = 0;
            this.data = null;

            this.rc = rc;
            packet = encode(senderPid, targetPid, timeout, code, dataLen, data);
        } else if (data.size() > 0xff) { // Error case, will null pointer exception.
            dataLen = -1;
            this.data = null;

            this.rc = null;
            packet = null;
        } else { // Non-null, valid data.
            dataLen = (short)(0xff & data.size());
            this.data = data;

            this.rc = rc;
            packet = encode(senderPid, targetPid, timeout, code, dataLen, data);
        }
    }
    // Whisper
    Msg(RobotController rc,
        int targetPid, int code, ArrayList<Integer> data) {
        senderPid = rc.getID() % 0x10000;
        this.targetPid = ((0xffff & targetPid) % 0xffff); // Note, not % 0x10000.
        timeout = 2000;// GameConstants.ROUND_MAX_LIMIT;
        this.code = code;
        if (data == null) {
            dataLen = 0;
            this.data = null;

            this.rc = rc;
            packet = encode(senderPid, targetPid, timeout, code, dataLen, data);
        } else if (data.size() > 0xff) { // Error case, will null pointer exception.
            dataLen = -1;
            this.data = null;

            this.rc = null;
            packet = null;
        } else { // Non-null, valid data.
            dataLen = (short)(0xff & data.size());
            this.data = data;

            this.rc = rc;
            packet = encode(senderPid, targetPid, timeout, code, dataLen, data);
        }
    }
    // Used in constructor Msg(rc, offset).
    private static ArrayList<Integer> readMessageFromOffset(RobotController rc,
                                                            int offset) throws GameActionException {
        int header1 = rc.readBroadcast(offset);
        if (header1 == 0) {
            return null;
        } else {
            int header2 = rc.readBroadcast(offset + 1);
            int dataLen = dataLenFromHeader(header2);
            ArrayList<Integer> newPacket = new ArrayList<Integer>();
            newPacket.add(header1);
            newPacket.add(header2);
            // Can be optimized with more variables.
            for (int i = 0; i < dataLen; ++i) {
                newPacket.add(rc.readBroadcast(offset + headerLen + i));
            }
            return newPacket;
        }
    }

    // Accesssors
    public int getSenderPid() {
        return senderPid;
    }
    public int getTargetPid() {
        return targetPid;
    }
    public int getTimeout() {
        return timeout;
    }
    public int getCode() {
        return code;
    }
    public ArrayList<Integer> getData() {
        return data;
    }
    public ArrayList<Integer> getPacket() {
        return packet;
    }
    public int getPacketLen() {
        assert ((dataLen + headerLen) == packet.size());
        return (dataLen + headerLen);
    }
    /*
    public static int getSenderFromHeader1(int header) {
        return senderPidFromHeader(header);
    }
    public static int getTargetFromHeader1(int header) {
        return targetPidFromHeader(header);
    }
    */
    public static int getPacketLenFromHeader2(int header) {
        return dataLenFromHeader(header);
    }

    // Internal
    // Use this to build a send function.
    // NOTE: No checks! No transaction!
    protected void writeWithOffset(RobotController rc,
                                  int offset) throws GameActionException {
        //System.out.println("Sending " + packet.size() + " ints over the wire...");
        for (int i = 0; i < packet.size(); ++i) {
            //System.out.print(" <<< " +
            //        Integer.toHexString(packet.get(i)));
            rc.broadcast(offset+i, packet.get(i));
        } 
        //System.out.println(", " + Integer.toHexString(targetPid) + " ! {" + Integer.toHexString(senderPid) + ", ...} @ offset=" + offset);
    }
    // TODO: Can be optimized.
    private ArrayList<Integer> encodeHeader(int senderPid, int targetPid,
                                            int timeout, int code, short len) {
        ArrayList<Integer> header = new ArrayList<Integer>();
        header.add(((0xffff & senderPid) << 16) | (0xffff & targetPid));
        header.add((code << 8) | len);
        return header;
    }
    private ArrayList<Integer> encode(int senderPid, int targetPid,
                                      int timeout, int code, short len,
                                      ArrayList<Integer> data) {
        ArrayList<Integer> msg = encodeHeader(senderPid, targetPid,
                                              timeout, code, len);
        if (data != null) {
            msg.addAll(data);
        }
        return msg;
    }

    // First header
    private static int senderPidFromHeader(int header) {
        return 0xffff & (header >> 16);
    }
    private static int targetPidFromHeader(int header) {
        return header & 0xffff;
    }
    // Second header
    private static int timeoutFromHeader(int header) {
        return 0xffff & (header >> 16);
    }
    private static int codeFromHeader(int header) {
        return (header >> 8) & 0xff;
    }
    private static short dataLenFromHeader(int header) {
        return (short)(header & 0xff);
    }
    
    private ArrayList<Integer> dataFromMsg(short dataLen, ArrayList<Integer> msg) {
        //try {
        return new ArrayList<Integer>(msg.subList(headerLen, headerLen + dataLen));
        //} catch (Exception e) {
        //    System.err.println(e + ", could not extract packet data.");
        //    return null;
        //}
    }    
}
