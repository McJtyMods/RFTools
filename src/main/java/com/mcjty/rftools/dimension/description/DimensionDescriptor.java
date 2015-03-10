package com.mcjty.rftools.dimension.description;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import com.mcjty.rftools.items.dimlets.*;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A unique descriptor of a dimension.
 */
public class DimensionDescriptor {
    private final String descriptionString;
    private final int rfCreateCost;
    private final int rfMaintainCost;
    private final int tickCost;
    private final long forcedSeed;

    public DimensionDescriptor(List<DimletDescriptor> descriptors, long forcedSeed) {
        this.forcedSeed = forcedSeed;

        StringBuilder s = new StringBuilder();

        // List of all non-modifier dimlets with all associated modifiers.
        List<Pair<DimletKey,List<DimletDescriptor>>> dimlets = new ArrayList<Pair<DimletKey, List<DimletDescriptor>>>();

        // A list of all current modifier that haven't been fitted into a type yet.
        List<DimletDescriptor> currentModifiers = new ArrayList<DimletDescriptor>();

        groupDimletsAndModifiers(descriptors, dimlets, currentModifiers);
        constructDescriptionStringNew(s, dimlets, currentModifiers);

        descriptionString = s.toString();

        tickCost = calculateTickCost(dimlets);
        rfCreateCost = calculateCreationRfCost(dimlets, currentModifiers, tickCost);
        rfMaintainCost = calculateMaintenanceRfCost(dimlets);
    }

    private void constructDescriptionStringNew(StringBuilder s, List<Pair<DimletKey,List<DimletDescriptor>>> dimlets, List<DimletDescriptor> currentModifiers) {
        s.append('@');
        boolean first = true;
        for (Pair<DimletKey, List<DimletDescriptor>> dimletWithModifiers : dimlets) {
            DimletKey key = dimletWithModifiers.getLeft();
            List<DimletDescriptor> mods = dimletWithModifiers.getRight();
            if (mods != null) {
                for (DimletDescriptor modifier : mods) {
                    if (!first) {
                        s.append(',');
                    }
                    first = false;
                    s.append('#').append(modifier.getKey());
                }
            }
            if (!first) {
                s.append(',');
            }
            first = false;
            s.append(key);
        }

        // Now add all unused modifiers to the end.
        for (DimletDescriptor modifier : currentModifiers) {
            if (s.length() > 0) {
                s.append(',');
            }
            s.append('?').append(modifier.getKey());
        }
    }

    private void groupDimletsAndModifiers(List<DimletDescriptor> descriptors, List<Pair<DimletKey,List<DimletDescriptor>>> dimlets, List<DimletDescriptor> currentModifiers) {
        for (DimletDescriptor descriptor : descriptors) {
            DimletType type = descriptor.getType();
            DimletKey key = descriptor.getKey();
            if (type.isModifier()) {
                // Keep the modifier here until we find a dimlet for which it fits.
                currentModifiers.add(descriptor);
            } else {
                List<DimletDescriptor> modifiers = new ArrayList<DimletDescriptor>();
                if (!currentModifiers.isEmpty()) {
                    // Check if we collected modifiers that fit with this type.
                    List<DimletDescriptor> copy = new ArrayList<DimletDescriptor>(currentModifiers);
                    // Iterate over a copy so that we can delete from original list.
                    for (DimletDescriptor modifier : copy) {
                        if (type.isModifiedBy(modifier.getType())) {
                            modifiers.add(modifier);
                            currentModifiers.remove(modifier);
                        }
                    }
                }
                dimlets.add(Pair.of(key, modifiers));
            }
        }
    }

    public DimensionDescriptor(NBTTagCompound tagCompound) {
        String ds = tagCompound.getString("descriptionString");
        if (ds.startsWith("@")) {
            // New style already.
            descriptionString = ds;
        } else {
            // We need to convert.
            List<DimletDescriptor> dimletDescriptors = parseOldDescriptionString(ds);

            List<Pair<DimletKey,List<DimletDescriptor>>> dimlets = new ArrayList<Pair<DimletKey, List<DimletDescriptor>>>();

            // A list of all current modifier that haven't been fitted into a type yet.
            List<DimletDescriptor> currentModifiers = new ArrayList<DimletDescriptor>();
            groupDimletsAndModifiers(dimletDescriptors, dimlets, currentModifiers);

            StringBuilder s = new StringBuilder();
            constructDescriptionStringNew(s, dimlets, currentModifiers);
            RFTools.log("Converting dimension descriptor from: " + ds + " to: " + s);
            descriptionString = s.toString();
        }

        rfCreateCost = tagCompound.getInteger("rfCreateCost");
        rfMaintainCost = tagCompound.getInteger("rfMaintainCost");
        tickCost = tagCompound.getInteger("tickCost");
        forcedSeed = tagCompound.getLong("forcedSeed");
    }

    public List<Pair<DimletDescriptor,List<DimletDescriptor>>> getDimletsWithModifiers() {
        List<Pair<DimletDescriptor,List<DimletDescriptor>>> result = new ArrayList<Pair<DimletDescriptor, List<DimletDescriptor>>>();

        if (!descriptionString.isEmpty()) {
            List<DimletDescriptor> modifiers = new ArrayList<DimletDescriptor>();

            String[] opcodes = descriptionString.split(",");
            for (String oc : opcodes) {
                DimletKey key;
                if (oc.startsWith("#")) {
                    // First comes '#', then the type of the actual dimlet.
                    key = DimletKey.parseKey(oc.substring(1));
                    modifiers.add(new DimletDescriptor(key.getType(), key));
                } else if (oc.startsWith("?")) {
                } else {
                    key = DimletKey.parseKey(oc);
                    result.add(Pair.of(new DimletDescriptor(key.getType(), key), modifiers));
                    modifiers = new ArrayList<DimletDescriptor>();
                }
            }
        }
        return result;
    }

    public static List<DimletDescriptor> parseDescriptionString(String descriptionString) {
        if (descriptionString.startsWith("@")) {
            return parseNewDescriptionString(descriptionString);
        } else {
            return parseOldDescriptionString(descriptionString);
        }
    }

    private static List<DimletDescriptor> parseNewDescriptionString(String descriptionString) {
        List<DimletDescriptor> result = new ArrayList<DimletDescriptor>();
        if (!descriptionString.isEmpty()) {
            String[] opcodes = descriptionString.substring(1).split(",");
            for (String oc : opcodes) {
                DimletKey key;
                if (oc.startsWith("#")) {
                    // First comes '#', then the type of the actual dimlet.
                    key = DimletKey.parseKey(oc.substring(1));
                } else if (oc.startsWith("?")) {
                    // First comes '?', then the type of the actual dimlet
                    key = DimletKey.parseKey(oc.substring(1));
                } else {
                    key = DimletKey.parseKey(oc);
                }
                result.add(new DimletDescriptor(key.getType(), key));
            }
        }
        return result;
    }

    public static List<DimletDescriptor> parseOldDescriptionString(String descriptionString) {
        DimletMapping mapping = DimletMapping.getInstance();
        List<DimletDescriptor> result = new ArrayList<DimletDescriptor>();
        if (!descriptionString.isEmpty()) {
            String[] opcodes = descriptionString.split(",");
            for (String oc : opcodes) {
                DimletType type;
                Integer id;
                if (oc.startsWith("#")) {
                    // First comes '#', then the type of the actual dimlet.
                    type = DimletType.getTypeByOpcode(oc.substring(1, 2));
                    id = Integer.parseInt(oc.substring(2));
                } else if (oc.startsWith("?")) {
                    // First comes '?', then the type of the actual dimlet.
                    type = DimletType.getTypeByOpcode(oc.substring(1, 2));
                    id = Integer.parseInt(oc.substring(2));
                } else {
                    type = DimletType.getTypeByOpcode(oc.substring(0, 1));
                    id = Integer.parseInt(oc.substring(1));
                }
                result.add(new DimletDescriptor(type, mapping.getKey(id)));
            }
        }
        return result;
    }

    public long calculateSeed(long seed) {
        DimletMapping mapping = DimletMapping.getInstance();
        List<DimletDescriptor> dimletDescriptors = parseDescriptionString(descriptionString);
        for (DimletDescriptor descriptor : dimletDescriptors) {
            seed = 31 * seed + mapping.getId(descriptor.getKey());
        }
        return seed;
    }

    public String getDescriptionString() {
        return descriptionString;
    }

    public int getRfCreateCost() {
        return rfCreateCost;
    }

    public int getRfMaintainCost() {
        return rfMaintainCost;
    }

    public int getTickCost() {
        return tickCost;
    }

    public long getForcedSeed() {
        return forcedSeed;
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("descriptionString", descriptionString);
        tagCompound.setInteger("rfCreateCost", rfCreateCost);
        tagCompound.setInteger("rfMaintainCost", rfMaintainCost);
        tagCompound.setInteger("tickCost", tickCost);
        tagCompound.setInteger("ticksLeft", tickCost);
        tagCompound.setLong("forcedSeed", forcedSeed);
    }

    private int getModifierMultiplier(Map<Pair<DimletType,DimletType>,Integer> modifierMap, DimletType type1, DimletType type2) {
        Integer multiplier = modifierMap.get(Pair.of(type1, type2));
        if (multiplier == null) {
            return 1;
        }
        return multiplier;
    }

    private int getCreationCost(DimletType type, DimletKey key) {
        int cost = 0;
        DimletEntry entry = KnownDimletConfiguration.getEntry(key);
        if (entry != null) {
            cost = entry.getRfCreateCost();
            if (cost == -1) {
                cost = DimletCosts.typeRfCreateCost.get(type);
            }
        }
        return cost;
    }

    private int calculateCreationRfCost(List<Pair<DimletKey,List<DimletDescriptor>>> dimlets, List<DimletDescriptor> unusedModifiers, int tickCost) {
        int rf = DimletCosts.baseDimensionCreationCost;

        for (Pair<DimletKey, List<DimletDescriptor>> dimletWithModifier : dimlets) {
            DimletKey key = dimletWithModifier.getLeft();
            DimletType type = key.getType();

            List<DimletDescriptor> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletDescriptor modifier : list) {
                    int mult = getModifierMultiplier(DimletCosts.rfCreateModifierMultiplier, modifier.getType(), type);
                    rf += getCreationCost(modifier.getType(), modifier.getKey()) * mult;
                }
            }

            rf += getCreationCost(type, key);
        }

        for (DimletDescriptor modifier : unusedModifiers) {
            rf += getCreationCost(modifier.getType(), modifier.getKey());
        }

        // Compensate createCost for the cost to fill the matter receiver at the destination end.
        rf += TeleportConfiguration.RECEIVER_MAXENERGY / tickCost;

        return rf;
    }

    private int getMaintenanceCost(DimletType type, DimletKey key) {
        int cost = 0;
        DimletEntry entry = KnownDimletConfiguration.getEntry(key);
        if (entry != null) {
            cost = entry.getRfMaintainCost();
            if (cost == -1) {
                cost = DimletCosts.typeRfMaintainCost.get(type);
            }
        }
        return cost;
    }

    private int calculateMaintenanceRfCost(List<Pair<DimletKey,List<DimletDescriptor>>> dimlets) {
        int rf = DimletCosts.baseDimensionMaintenanceCost;
        int rfGain = 0;

        for (Pair<DimletKey, List<DimletDescriptor>> dimletWithModifier : dimlets) {
            DimletKey key = dimletWithModifier.getLeft();
            DimletType type = key.getType();

            List<DimletDescriptor> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletDescriptor modifier : list) {
                    int mult = getModifierMultiplier(DimletCosts.rfMaintainModifierMultiplier, modifier.getType(), type);
                    rf += getMaintenanceCost(modifier.getType(), modifier.getKey()) * mult;
                }
            }

            int c = getMaintenanceCost(type, key);
            if (c < 0) {
                rfGain -= c;        // This dimlet gives a bonus in cost. This value is a percentage.
            } else {
                rf += c;
            }
        }

        if (rfGain > 0) {
            rf = rf - (rf * rfGain / 100);
            if (rf < 10) {
                rf = 10;        // Never consume less then 10 RF/tick
            }
        }

        return rf;
    }

    private int getTickCost(DimletType type, DimletKey key) {
        int cost = 0;
        DimletEntry entry = KnownDimletConfiguration.getEntry(key);
        if (entry != null) {
            cost = entry.getTickCost();
            if (cost == -1) {
                cost = DimletCosts.typeTickCost.get(type);
            }
        }
        return cost;
    }

    private int calculateTickCost(List<Pair<DimletKey,List<DimletDescriptor>>> dimlets) {
        int ticks = DimletCosts.baseDimensionTickCost;

        for (Pair<DimletKey, List<DimletDescriptor>> dimletWithModifier : dimlets) {
            DimletKey key = dimletWithModifier.getLeft();
            DimletType type = key.getType();

            List<DimletDescriptor> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletDescriptor modifier : list) {
                    int mult = getModifierMultiplier(DimletCosts.tickCostModifierMultiplier, modifier.getType(), type);
                    ticks += getTickCost(modifier.getType(), modifier.getKey()) * mult;
                }
            }

            ticks += getTickCost(type, key);
        }

        return ticks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DimensionDescriptor that = (DimensionDescriptor) o;

        if (!descriptionString.equals(that.descriptionString)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return descriptionString.hashCode();
    }

    public static class DimletDescriptor {
        private final DimletType type;
        private final DimletKey key;

        public DimletDescriptor(DimletType type, DimletKey key) {
            this.type = type;
            this.key = key;
        }

        public DimletType getType() {
            return type;
        }

        public DimletKey getKey() {
            return key;
        }
    }
}
