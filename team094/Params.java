// Woo hoo paramaterization :)
// Various constants that may be optimized are listed here.
package team094;

class Params {
    //
    // HQ
    //

    // 0 < BEAVER_CAP_A << 1.
    // 0 <= BEAVER_CAP_B
    // 0 <= BEAVER_CAP_C = min beavers
    //
    // beaverCap = f(Maparea > 0)
    // beaverCap = max(c, a*D + b)
    public static final double BEAVER_CAP_A = 0.01;
    public static final int BEAVER_CAP_B = 0;
    public static final int BEAVER_CAP_C = 10;


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
    public static final double SUPPLY_HQ_A = 1;
    public static final double SUPPLY_HQ_B = 0;
    public static final double SUPPLY_HQ_C = 0;
    public static final int SUPPLY_HQ_D = 0;
    public static final int SUPPLY_HQ_E = 9000;
    public static final int SUPPLY_HQ_F = 0;

    //
    // Beaver
    //

    // 0 < BEAVER_CAP_A << 1.
    // 0 <= BEAVER_CAP_B
    // 0 <= BEAVER_CAP_C = min beavers
    //
    // minerFactoryCap = f(Maparea > 0)
    // minerFactoryCap = max(c, a*D + b)
    public static final double MINERFACTORY_CAP_A = 0.0005;
    public static final int MINERFACTORY_CAP_B = 0;
    public static final int MINERFACTORY_CAP_C = 4;

    // if current ore < X, stop mining
    public static final int BEAVER_ORE_THRESHOLD = 10;

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
    public static final double SUPPLY_BEAVER_A = 0.5;
    public static final double SUPPLY_BEAVER_B = 0.5;
    public static final double SUPPLY_BEAVER_C = 0;
    public static final int SUPPLY_BEAVER_D = 42;
    public static final int SUPPLY_BEAVER_E = 42;
    public static final int SUPPLY_BEAVER_F = 42;

    //
    // Tower
    //

    // supposed to be min_tower_supply
    public static final int TOWER_SUPPLY_THRESHOLD = 420;
}
