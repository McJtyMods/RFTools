package mcjty.rftools.dimension.description;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import mcjty.rftools.items.dimlets.*;
import mcjty.varia.Logging;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.StringUtils;
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

    public DimensionDescriptor(List<DimletKey> descriptors, long forcedSeed) {
        this.forcedSeed = forcedSeed;

        StringBuilder s = new StringBuilder();

        // List of all non-modifier dimlets with all associated modifiers.
        List<Pair<DimletKey,List<DimletKey>>> dimlets = new ArrayList<Pair<DimletKey, List<DimletKey>>>();

        // A list of all current modifier that haven't been fitted into a type yet.
        List<DimletKey> currentModifiers = new ArrayList<DimletKey>();

        groupDimletsAndModifiers(descriptors, dimlets, currentModifiers);
        constructDescriptionStringNew(s, dimlets, currentModifiers);

        descriptionString = s.toString();

        tickCost = calculateTickCost(dimlets);
        rfCreateCost = calculateCreationRfCost(dimlets, currentModifiers, tickCost);

        int rf = calculateMaintenanceRfCost(dimlets);
        int rfGain = calculateBonus(dimlets);
        if (rfGain > 0) {
            int rfMinimum = Math.max(10, rf * DimletConfiguration.minimumCostPercentage / 100);
            rf = rf - (rf * rfGain / 100);
            if (rf < rfMinimum) {
                rf = rfMinimum;        // Never consume less then this
            }
        }
        rfMaintainCost = rf;
    }

    private void constructDescriptionStringNew(StringBuilder s, List<Pair<DimletKey,List<DimletKey>>> dimlets, List<DimletKey> currentModifiers) {
        s.append('@');
        boolean first = true;
        for (Pair<DimletKey, List<DimletKey>> dimletWithModifiers : dimlets) {
            DimletKey key = dimletWithModifiers.getLeft();
            List<DimletKey> mods = dimletWithModifiers.getRight();
            if (mods != null) {
                for (DimletKey modifier : mods) {
                    if (!first) {
                        s.append(',');
                    }
                    first = false;
                    s.append('#').append(modifier);
                }
            }
            if (!first) {
                s.append(',');
            }
            first = false;
            s.append(key);
        }

        // Now add all unused modifiers to the end.
        for (DimletKey modifier : currentModifiers) {
            if (s.length() > 0) {
                s.append(',');
            }
            s.append('?').append(modifier);
        }
    }

    private void groupDimletsAndModifiers(List<DimletKey> descriptors, List<Pair<DimletKey,List<DimletKey>>> dimlets, List<DimletKey> currentModifiers) {
        for (DimletKey key : descriptors) {
            DimletType type = key.getType();
            if (type.dimletType.isModifier()) {
                // Keep the modifier here until we find a dimlet for which it fits.
                currentModifiers.add(key);
            } else {
                List<DimletKey> modifiers = new ArrayList<DimletKey>();
                if (!currentModifiers.isEmpty()) {
                    // Check if we collected modifiers that fit with this type.
                    List<DimletKey> copy = new ArrayList<DimletKey>(currentModifiers);
                    // Iterate over a copy so that we can delete from original list.
                    for (DimletKey modifier : copy) {
                        if (type.dimletType.isModifiedBy(modifier.getType())) {
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
            List<DimletKey> dimletDescriptors = parseOldDescriptionString(ds);

            List<Pair<DimletKey,List<DimletKey>>> dimlets = new ArrayList<Pair<DimletKey, List<DimletKey>>>();

            // A list of all current modifier that haven't been fitted into a type yet.
            List<DimletKey> currentModifiers = new ArrayList<DimletKey>();
            groupDimletsAndModifiers(dimletDescriptors, dimlets, currentModifiers);

            StringBuilder s = new StringBuilder();
            constructDescriptionStringNew(s, dimlets, currentModifiers);
            Logging.log("Converting dimension descriptor from: " + ds + " to: " + s);
            descriptionString = s.toString();
        }

        rfCreateCost = tagCompound.getInteger("rfCreateCost");
        rfMaintainCost = tagCompound.getInteger("rfMaintainCost");
        tickCost = tagCompound.getInteger("tickCost");
        forcedSeed = tagCompound.getLong("forcedSeed");
    }

    public List<Pair<DimletKey,List<DimletKey>>> getDimletsWithModifiers() {
        List<Pair<DimletKey,List<DimletKey>>> result = new ArrayList<Pair<DimletKey, List<DimletKey>>>();

        String ds = descriptionString;
        if (ds.startsWith("@")) {
            ds = ds.substring(1);
        }

        if (!ds.isEmpty()) {
            List<DimletKey> modifiers = new ArrayList<DimletKey>();

            String[] opcodes = StringUtils.split(ds, ",");
            for (String oc : opcodes) {
                DimletKey key;
                if (oc.startsWith("#")) {
                    // First comes '#', then the type of the actual dimlet.
                    key = DimletKey.parseKey(oc.substring(1));
                    modifiers.add(key);
                } else if (oc.startsWith("?")) {
                } else {
                    key = DimletKey.parseKey(oc);
                    result.add(Pair.of(key, modifiers));
                    modifiers = new ArrayList<DimletKey>();
                }
            }
        }
        return result;
    }

    public static List<DimletKey> parseDescriptionString(String descriptionString) {
        if (descriptionString.startsWith("@")) {
            return parseNewDescriptionString(descriptionString);
        } else {
            return parseOldDescriptionString(descriptionString);
        }
    }

    private static List<DimletKey> parseNewDescriptionString(String descriptionString) {
        List<DimletKey> result = new ArrayList<DimletKey>();
        String ds = descriptionString.substring(1);
        if (!ds.isEmpty()) {
            String[] opcodes = StringUtils.split(ds, ",");
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
                result.add(key);
            }
        }
        return result;
    }

    public static List<DimletKey> parseOldDescriptionString(String descriptionString) {
        DimletMapping mapping = DimletMapping.getInstance();
        List<DimletKey> result = new ArrayList<DimletKey>();
        if (!descriptionString.isEmpty()) {
            String[] opcodes = StringUtils.split(descriptionString, ",");
            for (String oc : opcodes) {
                Integer id;
                if (oc.startsWith("#")) {
                    // First comes '#', then the type of the actual dimlet.
                    id = Integer.parseInt(oc.substring(2));
                } else if (oc.startsWith("?")) {
                    // First comes '?', then the type of the actual dimlet.
                    id = Integer.parseInt(oc.substring(2));
                } else {
                    id = Integer.parseInt(oc.substring(1));
                }
                result.add(mapping.getKey(id));
            }
        }
        return result;
    }

    public long calculateSeed(long seed) {
        DimletMapping mapping = DimletMapping.getInstance();
        List<DimletKey> dimletDescriptors = parseDescriptionString(descriptionString);
        for (DimletKey key : dimletDescriptors) {
            seed = 31 * seed + mapping.getId(key);
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
                cost = type.dimletType.getCreationCost();
            }
        }
        return cost;
    }

    private int calculateCreationRfCost(List<Pair<DimletKey,List<DimletKey>>> dimlets, List<DimletKey> unusedModifiers, int tickCost) {
        int rf = DimletCosts.baseDimensionCreationCost;

        for (Pair<DimletKey, List<DimletKey>> dimletWithModifier : dimlets) {
            DimletKey key = dimletWithModifier.getLeft();
            DimletType type = key.getType();

            List<DimletKey> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletKey modifier : list) {
                    float mult = type.dimletType.getModifierCreateCostFactor(modifier.getType(), key);
                    rf += (int) (getCreationCost(modifier.getType(), modifier) * mult);
                }
            }

            rf += getCreationCost(type, key);
        }

        for (DimletKey modifier : unusedModifiers) {
            rf += getCreationCost(modifier.getType(), modifier);
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
                cost = type.dimletType.getMaintenanceCost();
            }
        }
        return cost;
    }

    // Calculate the cost of this dimension without taking bonus dimlets into account.
    public int calculateNominalCost() {
        List<DimletKey> dimletKeys = parseDescriptionString(descriptionString);

        // List of all non-modifier dimlets with all associated modifiers.
        List<Pair<DimletKey,List<DimletKey>>> dimlets = new ArrayList<Pair<DimletKey, List<DimletKey>>>();

        // A list of all current modifier that haven't been fitted into a type yet.
        List<DimletKey> currentModifiers = new ArrayList<DimletKey>();

        groupDimletsAndModifiers(dimletKeys, dimlets, currentModifiers);

        return calculateMaintenanceRfCost(dimlets);
    }

    // Calculate the maintenance cost of a dimension without bonus dimlets.
    private int calculateMaintenanceRfCost(List<Pair<DimletKey,List<DimletKey>>> dimlets) {
        int rf = DimletCosts.baseDimensionMaintenanceCost;

        for (Pair<DimletKey, List<DimletKey>> dimletWithModifier : dimlets) {
            DimletKey key = dimletWithModifier.getLeft();
            DimletType type = key.getType();

            List<DimletKey> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletKey modifier : list) {
                    float mult = type.dimletType.getModifierMaintainCostFactor(modifier.getType(), key);
                    rf += (int) (getMaintenanceCost(modifier.getType(), modifier) * mult);
                }
            }

            int c = getMaintenanceCost(type, key);
            if (c > 0) {
                rf += c;
            }
        }

        return rf;
    }

    private int calculateBonus(List<Pair<DimletKey,List<DimletKey>>> dimlets) {
        int rfGain = 0;

        for (Pair<DimletKey, List<DimletKey>> dimletWithModifier : dimlets) {
            DimletKey key = dimletWithModifier.getLeft();
            DimletType type = key.getType();

            int c = getMaintenanceCost(type, key);
            if (c < 0) {
                rfGain -= c;        // This dimlet gives a bonus in cost. This value is a percentage.
            }
        }

        return rfGain;
    }

    private int getTickCost(DimletType type, DimletKey key) {
        int cost = 0;
        DimletEntry entry = KnownDimletConfiguration.getEntry(key);
        if (entry != null) {
            cost = entry.getTickCost();
            if (cost == -1) {
                cost = type.dimletType.getTickCost();
            }
        }
        return cost;
    }

    private int calculateTickCost(List<Pair<DimletKey,List<DimletKey>>> dimlets) {
        int ticks = DimletCosts.baseDimensionTickCost;

        for (Pair<DimletKey, List<DimletKey>> dimletWithModifier : dimlets) {
            DimletKey key = dimletWithModifier.getLeft();
            DimletType type = key.getType();

            List<DimletKey> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletKey modifier : list) {
                    float mult = type.dimletType.getModifierTickCostFactor(modifier.getType(), key);
                    ticks += (int) (getTickCost(modifier.getType(), modifier) * mult);
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
}
