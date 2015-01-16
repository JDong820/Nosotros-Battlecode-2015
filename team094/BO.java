// Build order file
package team094;
import battlecode.common.*;
import java.util.*;

class BO {
    public final ArrayList<RobotType> buildings;

    BO() {
        ArrayList<RobotType> tmp = new ArrayList<RobotType>();
        tmp.add(RobotType.MINERFACTORY);
        tmp.add(RobotType.HELIPAD);
        tmp.add(RobotType.MINERFACTORY);
        tmp.add(RobotType.MINERFACTORY);
        tmp.add(RobotType.MINERFACTORY);
        tmp.add(RobotType.MINERFACTORY);
        buildings = tmp;
    }
}
