package team094;
import java.util.ArrayList;
import battlecode.common.RobotController;
import battlecode.common.GameActionException;

// Protocol
// Header 1
// 0xffff0000 self()
// 0x0000ffff Pid
// Header 2
// 0xffff0000 Timeout
// 0x0000ff00 Code
// 0x000000ff DataLen
class Header {
    final int header1;
    final int header2;
    final int absoluteOffset;

    // Derived data
    final int senderPid; // From:
    final int targetPid; // To:
    final int timeout;
    final int code;
    final short dataLen;

    static final byte headerLen = 2;

    // Note: constructs a bogus header that indicates an inbox end.
    Header(int offset) {
        header1 = -1;
        header2 = -1;
        absoluteOffset = offset;

        senderPid = -1;
        targetPid = -1;
        timeout = -1;
        code = -1;
        dataLen = -1;
    }

    Header(int h1, int h2, int offset) {
        header1 = h1; 
        header2 = h2;
        absoluteOffset = offset;

        // TODO: Add shortcut for read messages;
        // when senderPid = 0xffff, dataLen = dataLenFromHeader(header1);
        senderPid = senderPidFromHeader(header1);
        targetPid = targetPidFromHeader(header1);
        timeout = timeoutFromHeader(header2);
        code = codeFromHeader(header2);
        dataLen = dataLenFromHeader(header2);
    }
    Header(Header h) {
        this(h.getHeader1(), h.getHeader2(), h.getAbsoluteOffset());
    }
    // Note: second offset takes priority.
    Header(Header h, int offset) {
        this(h.getHeader1(), h.getHeader2(), offset);
    }
    Header(int h1, int h2) {
        this(h1, h2, -1);
    }
    Header(RobotController rc, int offset) throws GameActionException {
        this(rc.readBroadcast(offset),
             rc.readBroadcast(offset + 1),
             offset);
    }
    Header(Msg m) {
        this(m.getHeader());
    }
    Header(Msg m, int offset) {
        this(m.getHeader(), offset);
    }
    Header(int senderPid, int targetPid,
           int timeout, int code, short dataLen) {
        this(encodeHeader1(senderPid, targetPid),
             encodeHeader2(timeout, code, dataLen),
             -1);
    }

    public int getHeader1() {
        return header1;
    }
    public int getHeader2() {
        return header2;
    }
    public int getAbsoluteOffset() {
        return absoluteOffset;
    }

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
    public short getDataLen() {
        return dataLen;
    }
    public int getPacketLen() {
        if (dataLen >= 0)
            return dataLen + headerLen;
        else
            return -1;
    }


    public static int encodeHeader1(int sender, int target) {
        return ((0xffff & sender) << 16) | (0xffff & target);
    }
    public static int encodeHeader2(int timeout, int code, short dataLen) {
        return ((0xffff & timeout) << 16) | ((0xff & code) << 8) | (0xff & dataLen);
    }

    // This returns the header of the next unread message in the mailbox.
    // Starts searching with offset of index.
    // Returns null if at end of inbox or all messages read.
    public static Header readNextUnreadHeader(RobotController rc,
            int inboxBegin, int inboxEnd,
            int absoluteIndex) throws GameActionException {
        if (inboxBegin <= absoluteIndex && absoluteIndex < inboxEnd) {
            final Header h = new Header(rc, absoluteIndex);
            if (h.getHeader1() == 0) {
                // Nothing to read at end of inbox.
                // Return a special header whos offset is
                // right past the last message.
                return new Header(absoluteIndex);
            }
            // It's impossible to send as anycast, so 0xffff is the code for
            // an already read message. Skip over read messages.
            if (h.getSenderPid() == 0xffff) {
                // This works automagically beacuse the dataLen is encoded
                // into header's target for read messages.
                return readNextUnreadHeader(rc,
                        inboxBegin, inboxEnd,
                        absoluteIndex + h.getPacketLen());
            } 
            return h;
        } else {
            // Index out of range, no message found.
            // NOTE: Untested behaviour at end of inbox.
            return null;
        }
    }
 
    private static int senderPidFromHeader(int header) {
        return 0xffff & (header >> 16);
    }
    private static int targetPidFromHeader(int header) {
        return header & 0xffff;
    }

    private static int timeoutFromHeader(int header) {
        return 0xffff & (header >> 16);
    }
    private static int codeFromHeader(int header) {
        return (header >> 8) & 0xff;
    }
    private static short dataLenFromHeader(int header) {
        return (short)(header & 0xff);
    }
}
