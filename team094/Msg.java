package team094;
import battlecode.common.*;
import java.util.*;

// TODO: use the Header class.
class Msg {
    final Header header;
    final ArrayList<Integer> data;

    final ArrayList<Integer> packet;

    // Decode packet
    Msg(int senderId, ArrayList<Integer> msg) {
        if (msg == null) {
            header = null;
            data = null;

            packet = null;
        } else {
            header = new Header(msg.get(0), msg.get(1));
            data = dataFromMsg(header.getDataLen(), msg);

            packet = msg;
        }
    }
    // Read
    Msg(RobotController rc, int offset) throws GameActionException {
        this(rc.getID(), readPacketFromOffset(rc, offset));
    }
    // Read from header
    Msg(RobotController rc, Header h) throws GameActionException {
        header = h;
        data = readDataAfterHeader(rc, h);

        packet = encode(h, data);
    }
    // Shout
    Msg(int senderId, int code, ArrayList<Integer> data) {
        final short dataLen;
        if (data == null) {
            dataLen = 0;
        } else if (data.size() > 0xff) {
            System.err.println("Tried encoding packet that was too long.");
            dataLen = 0;
            data = null;
        } else {
            dataLen = (short)(0x00ff & data.size());
        }
        this.data = data;
        header = new Header(senderId % 0x10000,
                            0xffff, // Reserved as allcast.
                            2000,// GameConstants.ROUND_MAX_LIMIT;
                            code,
                            dataLen);
        packet = encode(header, data);
    }
    // Whisper
    Msg(int senderId,
        int targetPid, int code, ArrayList<Integer> data) {
        final short dataLen;
        if (data == null) {
            dataLen = 0;
        } else if (data.size() > 0xff) {
            System.err.println("Tried encoding packet that was too long.");
            dataLen = 0;
            data = null;
        } else {
            dataLen = (short)(0x00ff & data.size());
        }
        this.data = data;
        header = new Header(senderId % 0x10000,
                            ((0xffff & targetPid) % 0xffff), // Reserved as allcast.
                            2000,// GameConstants.ROUND_MAX_LIMIT;
                            code,
                            dataLen);
        packet = encode(header, data);
    }


    public ArrayList<Integer> getData() {
        return data;
    }
    public ArrayList<Integer> getPacket() {
        return packet;
    }
    public int getPacketLen() {
        return header.getPacketLen();
    }
    public Header getHeader() {
        return header;
    }


    private static ArrayList<Integer> readDataAfterHeader(RobotController rc,
            Header h) throws GameActionException {
        assert(h.getAbsoluteOffset() >= 0);
        ArrayList<Integer> data = new ArrayList<Integer>(h.getDataLen());
        for (int i = 0; i < h.getDataLen(); ++i) {
            data.add(rc.readBroadcast(h.getAbsoluteOffset() + Header.headerLen + i));
        }
        return data;
    }
    private static ArrayList<Integer> readPacketFromOffset(RobotController rc,
            int offset) throws GameActionException {
        final Header h = new Header(rc, offset);
        return encode(h, readDataAfterHeader(rc, h));
    }

    // NOTE: No checks! No transaction!
    protected void writeWithOffset(RobotController rc,
                                   int offset) throws GameActionException {
        for (int i = 0; i < packet.size(); ++i) {
            rc.broadcast(offset+i, packet.get(i));
            //System.out.print(" <<< 0x" + Integer.toHexString(packet.get(i)));
        }
        //System.out.println(" @ offset=0x" + Integer.toHexString(offset));
    }

    private static ArrayList<Integer> encode(Header h, ArrayList<Integer> data) {
        ArrayList<Integer> msg = new ArrayList<Integer>();
        msg.add(h.getHeader1());
        msg.add(h.getHeader2());
        if (data != null) {
            msg.addAll(data);
        }
        return msg;
    }
    private ArrayList<Integer> dataFromMsg(short dataLen, ArrayList<Integer> msg) {
        return new ArrayList<Integer>(msg.subList(Header.headerLen,
                                      Header.headerLen + dataLen));
    }
}
