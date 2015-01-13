package team094;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {

    static Role role;
    static int lifeTurn = 0;
    static int x;
    static int y;

    private static void setCurrentRole(RobotController rc,
                                       RobotType type,
                                       int mode) {
        switch (type) {
        case HQ:
            role = new HQ(rc);
            break;
        case BEAVER:
            role = new Beaver(rc);
            break;
        case TOWER:
            role = new Tower(rc);
            break;
        default:
            System.err.println("Can't handle this robot type: " +
                               type.toString());
            break;
        }
    }

    public static void run(RobotController rc) throws GameActionException {

        setCurrentRole(rc, rc.getType(), 0);

        while(true) {
            try {
                role.execute();
            } catch (Exception e) {
                System.err.println(e + " RobotPlayer Exception");
                System.exit(1);
            }
            rc.yield();
        }
    }
}
