package team094;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;

class Beaver extends Role {

    Beaver(RobotController rc) {
        super(rc);
    }

    void execute() {
        try {
            autotransferSupply();

            if (rc.isCoreReady()) {
                Direction d = directions[(int)(rand.nextDouble()*8)];
                if (rc.canMove(d)) {
                    rc.move(d);
                }
            }
        } catch (Exception e) {
            System.err.println(e.toString() + " HQ Exception\n");
            e.printStackTrace();
        }
    }
}
