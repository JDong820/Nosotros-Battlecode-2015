package team094;
import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    
    public static void run(RobotController rc) throws GameActionException {
        while(true) {
            try {
            } catch (Exception e) {
                System.err.println(e + " RobotPlayer Exception");
            }
            rc.yield();
        }
    }
}
