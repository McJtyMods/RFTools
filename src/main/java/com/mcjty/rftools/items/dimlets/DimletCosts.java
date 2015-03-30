package com.mcjty.rftools.items.dimlets;

import java.util.HashMap;
import java.util.Map;

public class DimletCosts {
    static final Map<DimletKey,Integer> dimletBuiltinRfCreate = new HashMap<DimletKey, Integer>();
    static final Map<DimletKey,Integer> dimletBuiltinRfMaintain = new HashMap<DimletKey, Integer>();
    static final Map<DimletKey,Integer> dimletBuiltinTickCost = new HashMap<DimletKey, Integer>();

    public static int baseDimensionCreationCost = 1000;
    public static int baseDimensionMaintenanceCost = 10;
    public static int baseDimensionTickCost = 100;
}
