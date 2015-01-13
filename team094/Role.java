package team094;
import battlecode.common.Direction;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.*;
import java.util.*;
import java.util.Collection;

abstract class Role {
    static Direction[] directions = {Direction.NORTH,
                                     Direction.NORTH_EAST,
                                     Direction.EAST,
                                     Direction.SOUTH_EAST,
                                     Direction.SOUTH,
                                     Direction.SOUTH_WEST,
                                     Direction.WEST,
                                     Direction.NORTH_WEST
                                    };
    RobotController rc;
    Random rand;
    Team team;
    Team enemyTeam;
    int range;

    Role(RobotController rc) {
        this.rc = rc;
        rand = new Random(rc.getID());
        team = rc.getTeam();
        enemyTeam = team.opponent();
        range = rc.getType().attackRadiusSquared;
    }

    abstract void execute();

    static int directionToInt(Direction d) {
        switch(d) {
        case NORTH:
            return 0;
        case NORTH_EAST:
            return 1;
        case EAST:
            return 2;
        case SOUTH_EAST:
            return 3;
        case SOUTH:
            return 4;
        case SOUTH_WEST:
            return 5;
        case WEST:
            return 6;
        case NORTH_WEST:
            return 7;
        default:
            System.err.println("Unknown direction used. What?");
            return -1;
        }
    }



    void autotransferSupply() throws GameActionException {
        RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),
                                   GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, team);
        if (nearbyAllies.length > 0) {
            double supply = rc.getSupplyLevel();
            RobotInfo bestTarget = nearbyAllies[0];
            for (RobotInfo ri: nearbyAllies) {
                if (ri.supplyLevel < supply)
                    bestTarget = ri;
            }
            int transferQuantity = (int)((supply - bestTarget.supplyLevel)/2);
            if (transferQuantity > 42) {
                rc.transferSupplies(transferQuantity, bestTarget.location);
            }
        }
    }

    void amove(RobotInfo[] enemies) throws GameActionException {
        if (enemies.length > 0) {
            rc.attackLocation(enemies[0].location);
        }
    }
}
