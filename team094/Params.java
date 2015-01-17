// Woo hoo paramaterization :)
// Various constants that may be optimized are listed here.
package team094;

class Params {
    //
    // HQ
    //

    // 0 < BEAVER_CAP_A
    // 0 <= BEAVER_CAP_B = min beavers
    //
    // beaverCap = f(Maparea > 0)
    // beaverCap = max(b, a*0.0001*D)
    public final double GOAL_BEAVERS_A;
    public final int GOAL_BEAVERS_B;


    // SUPPLY_HQ_A = origin_coeff
    // SUPPLY_HQ_B = target_coeff
    // SUPPLY_HQ_C = d_coeff,
    // 0 <= SUPPLY_HQ_D = min_transfer
    // 0 <= SUPPLY_HQ_E = max_target
    // 0 <= SUPPLY_HQ_F = baseline)
    //
    // optimalTrasfer = f(X, Y, D) >= 0, X, Y, D >= 0.
    // optimalTransfer = (a*S1 - b*S2 + c*D) if _ > d else 0
    // S2 > e -> optimalTransfer = 0
    public final double SUPPLY_HQ_A = 1;
    public final double SUPPLY_HQ_B = 0;
    public final double SUPPLY_HQ_C = 0;
    public final int SUPPLY_HQ_D = 0;
    public final int SUPPLY_HQ_E = 9000;
    public final int SUPPLY_HQ_F = 0;

    //
    // Beaver
    //

    // 0 < BEAVER_CAP_A << 1.
    // 0 <= BEAVER_CAP_B
    // 0 <= BEAVER_CAP_C = min beavers
    //
    // minerFactoryCap = f(Maparea > 0)
    // minerFactoryCap = max(c, a*D + b)
    public final double GOAL_MINERFACTORIES_A;
    public final int GOAL_MINERFACTORIES_B;

    // if current ore < X, stop mining
    public final int BEAVER_ORE_THRESHOLD = 10;

    // SUPPLY_HQ_A = origin_coeff
    // SUPPLY_HQ_B = target_coeff
    // SUPPLY_HQ_C = d_coeff,
    // 0 <= SUPPLY_HQ_D = min_transfer
    // 0 <= SUPPLY_HQ_E = max_target
    // 0 <= SUPPLY_HQ_F = baseline)
    //
    // optimalTrasfer = f(X, Y, D) >= 0, X, Y, D >= 0.
    // optimalTransfer = (a*S1 - b*S2 + c*D) if _ > d else 0
    // S2 > e -> optimalTransfer = 0
    public final double SUPPLY_BEAVER_A = 0.5;
    public final double SUPPLY_BEAVER_B = 0.5;
    public final double SUPPLY_BEAVER_C = 0;
    public final int SUPPLY_BEAVER_D = 42;
    public final int SUPPLY_BEAVER_E = 42;
    public final int SUPPLY_BEAVER_F = 42;

    //
    // Tower
    //

    // supposed to be min_tower_supply
    public final int TOWER_SUPPLY_THRESHOLD = 420;


    // Things that don't belong here
    public boolean BENCHMARKING_ON = false;


    Params() {
        GOAL_BEAVERS_A = 0.001;
        GOAL_BEAVERS_B = 1;

        GOAL_MINERFACTORIES_A = 0.00042;
        GOAL_MINERFACTORIES_B = 1;
    }
}
