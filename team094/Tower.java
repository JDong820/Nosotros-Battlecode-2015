package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class Tower extends Role {
    final Params p;

    Tower(RobotController rc) {
        super(rc);
        p = new Params();
    }

    void update() {
        coreReady = rc.isCoreReady();
        weaponReady = rc.isWeaponReady();
    }

    void execute() {
        try {
            boolean coreReady = rc.isCoreReady();
            if (coreReady && rc.isWeaponReady()) {
                RobotInfo[] enemies = rc.senseNearbyRobots(range, enemyTeam);
                coreReady ^= amove(enemies);
            }
            // This code smells bad.
            if (rc.getSupplyLevel() > p.TOWER_SUPPLY_THRESHOLD) {
                autotransferSupply();
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " Tower Exception\n");
            e.printStackTrace();
        }
    }

    protected void handleMessage(Msg msg) {
    }
}
