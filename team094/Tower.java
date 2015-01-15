package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class Tower extends Role {

    Tower(RobotController rc) {
        super(rc);
    }

    void update() {
    }

    void execute() {
        try {
            boolean coreReady = rc.isCoreReady();
            if (coreReady && rc.isWeaponReady()) {
                RobotInfo[] enemies = rc.senseNearbyRobots(range, enemyTeam);
                coreReady ^= amove(enemies);
            }
            // This code smells bad.
            if (rc.getSupplyLevel() > Params.TOWER_SUPPLY_THRESHOLD) {
                autotransferSupply();
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " Tower Exception\n");
            e.printStackTrace();
        }
    }
}
