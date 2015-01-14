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
    MapLocation base;
    MapLocation enemy;
    MapLocation location;
    int range;

    Role(RobotController rc) {
        this.rc = rc;
        rand = new Random(rc.getID());
        team = rc.getTeam();
        enemyTeam = team.opponent();
        base = rc.senseHQLocation();
        enemy = rc.senseEnemyHQLocation();
        location = rc.getLocation();
        range = rc.getType().attackRadiusSquared;
    }

    void update() {
        location = rc.getLocation();
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

    // optimal supply trasfer = f(X, Y, D) >= 0, X, Y, D >= 0.
    // Note: originSupply is probobaly always rc.getSupply()
    private int calcSupplyTransfer(double originSupply,
                                   double targetSupply,
                                   int dsquared,
                                   RobotType targetType,
                                   double a, double b, double c, int d, int e) {
        switch (targetType) {
            case HQ:
                return 0;
            default:
                int optimalTransfer = (int) (a*originSupply - b*targetSupply + c*dsquared);
                if (optimalTransfer < d || targetSupply > e) {
                    return 0;
                }
                return optimalTransfer;
        }
    };

    void autotransferSupply() throws GameActionException {
        // Params (a,b,c,d,e,f):
        // (origin_coeff, target_coeff, d_coeff,
        //  min_transfer, max_target, baseline)
        // Constraints:
        // d >= 0, e >= 0, f >= 0
        switch (rc.getType()) {
            default:
                autotransferSupply(0.5, 0.5, 0, 42, 42, 42);
                break;
        }
    }
    
    void autotransferSupply(double a, double b, double c,
                            int d, int e, int f) throws GameActionException {
        double supply = rc.getSupplyLevel();
        if (supply > f) {
            RobotInfo[] nearbyAllies = rc.senseNearbyRobots(rc.getLocation(),
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, team);
            if (nearbyAllies.length > 0) {
                RobotInfo bestTarget = nearbyAllies[0];

                for (RobotInfo ri: nearbyAllies) {
                    if (ri.supplyLevel < bestTarget.supplyLevel) {
                        bestTarget = ri;
                    }
                    if (ri.supplyLevel == 0) {
                        break;
                    }
                }
                int targetTransfer = calcSupplyTransfer(supply,
                        bestTarget.supplyLevel,
                        bestTarget.location.distanceSquaredTo(rc.getLocation()),
                        bestTarget.type,
                        a, b, c, d, e);
                if (targetTransfer > 0) {
                    rc.transferSupplies(targetTransfer, bestTarget.location);
                }
            }
        }
    }

    void amove(RobotInfo[] enemies) throws GameActionException {
        if (enemies.length > 0) {
            rc.attackLocation(enemies[0].location);
        }
    }
}
