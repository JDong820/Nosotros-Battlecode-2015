package team094;
import team094.Params;
import java.util.*;
import battlecode.common.*;

class MinerFactory extends Role {

    MinerFactory(RobotController rc) {
        super(rc);
    }

    void update() {
    }

    void execute() {
        try {
            if (rc.isCoreReady()) {
            } else {
                autotransferSupply();
            }
        } catch (Exception e) {
            System.err.println(e.toString() + ": MinerFactory Exception\n");
            e.printStackTrace();
        }
    }

    protected void handleMessage(Msg msg) {
    }
}
