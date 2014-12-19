package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import com.mcjty.rftools.items.dimlets.DimletCosts;
import com.mcjty.rftools.items.dimlets.DimletEntry;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
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

    public DimensionDescriptor(List<DimletDescriptor> descriptors) {
        StringBuilder s = new StringBuilder();

        // List of all non-modifier dimlets with all associated modifiers.
        List<Pair<Integer,List<DimletDescriptor>>> dimlets = new ArrayList<Pair<Integer, List<DimletDescriptor>>>();

        // A list of all current modifier that haven't been fitted into a type yet.
        List<DimletDescriptor> currentModifiers = new ArrayList<DimletDescriptor>();

        groupDimletsAndModifiers(descriptors, dimlets, currentModifiers);
        constructDescriptionString(s, dimlets, currentModifiers);

        descriptionString = s.toString();

        tickCost = calculateTickCost(dimlets);
        rfCreateCost = calculateCreationRfCost(dimlets, currentModifiers, tickCost);
        rfMaintainCost = calculateMaintenanceRfCost(dimlets);
    }

    private void constructDescriptionString(StringBuilder s, List<Pair<Integer,List<DimletDescriptor>>> dimlets, List<DimletDescriptor> currentModifiers) {
        for (Pair<Integer, List<DimletDescriptor>> dimletWithModifiers : dimlets) {
            int id = dimletWithModifiers.getLeft();
            List<DimletDescriptor> mods = dimletWithModifiers.getRight();
            if (mods != null) {
                for (DimletDescriptor modifier : mods) {
                    if (s.length() > 0) {
                        s.append(',');
                    }
                    s.append('#').append(modifier.getType().getOpcode()).append(modifier.getId());
                }
            }
            if (s.length() > 0) {
                s.append(',');
            }
            s.append(KnownDimletConfiguration.idToDimlet.get(id).getKey().getType().getOpcode()).append(id);
        }

        // Now add all unused modifiers to the end.
        for (DimletDescriptor modifier : currentModifiers) {
            if (s.length() > 0) {
                s.append(',');
            }
            s.append('?').append(modifier.getType().getOpcode()).append(modifier.getId());
        }
    }

    private void groupDimletsAndModifiers(List<DimletDescriptor> descriptors, List<Pair<Integer,List<DimletDescriptor>>> dimlets, List<DimletDescriptor> currentModifiers) {
        for (DimletDescriptor descriptor : descriptors) {
            DimletType type = descriptor.getType();
            int id = descriptor.getId();
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
                dimlets.add(Pair.of(id, modifiers));
            }
        }
    }

    public DimensionDescriptor(NBTTagCompound tagCompound) {
        descriptionString = tagCompound.getString("descriptionString");
        rfCreateCost = tagCompound.getInteger("rfCreateCost");
        rfMaintainCost = tagCompound.getInteger("rfMaintainCost");
        tickCost = tagCompound.getInteger("tickCost");
    }

    public List<Pair<DimletDescriptor,List<DimletDescriptor>>> getDimletsWithModifiers() {
        List<Pair<DimletDescriptor,List<DimletDescriptor>>> result = new ArrayList<Pair<DimletDescriptor, List<DimletDescriptor>>>();

        if (!descriptionString.isEmpty()) {
            List<DimletDescriptor> modifiers = new ArrayList<DimletDescriptor>();
//            List<DimletDescriptor> unknownModifiers = new ArrayList<DimletDescriptor>();

            String[] opcodes = descriptionString.split(",");
            for (String oc : opcodes) {
                DimletType type;
                Integer id;
                if (oc.startsWith("#")) {
                    // First comes '#', then the type of the actual dimlet.
                    type = DimletType.getTypeByOpcode(oc.substring(1, 2));
                    id = Integer.parseInt(oc.substring(2));
                    modifiers.add(new DimletDescriptor(type, id));
                } else if (oc.startsWith("?")) {
                    // First comes '?', then the type of the actual dimlet.
//                    type = DimletType.getTypeByOpcode(oc.substring(1, 2));
//                    id = Integer.parseInt(oc.substring(2));
//                    unknownModifiers.add(new DimletDescriptor(type, id));
                } else {
                    type = DimletType.getTypeByOpcode(oc.substring(0, 1));
                    id = Integer.parseInt(oc.substring(1));
                    result.add(Pair.of(new DimletDescriptor(type, id), modifiers));
                    modifiers = new ArrayList<DimletDescriptor>();
                }
            }
        }
        return result;
    }

    public static List<DimletDescriptor> parseDescriptionString(String descriptionString) {
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
                result.add(new DimletDescriptor(type, id));
            }
        }
        return result;
    }

    /**
     * Get a list of all modifier dimlets that modify the given baseType.
     */
    public List<DimletDescriptor> getModifierDimlets(DimletType baseType) {
        List<DimletDescriptor> result = new ArrayList<DimletDescriptor>();
        if (!descriptionString.isEmpty()) {
            String[] opcodes = descriptionString.split(",");
            for (String oc : opcodes) {
                if (oc.startsWith("#")) {
                    // First comes '#', then type which is being modifed and then the type of the actual dimlet.
                    DimletType typeToModify = DimletType.getTypeByOpcode(oc.substring(1, 2));
                    if (baseType.equals(typeToModify)) {
                        DimletType type = DimletType.getTypeByOpcode(oc.substring(2, 3));
                        result.add(new DimletDescriptor(type, Integer.parseInt(oc.substring(3))));
                    }
                }
            }
        }
        return result;
    }

    public int calculateSeed() {
        int seed = 1;
        List<DimletDescriptor> dimletDescriptors = parseDescriptionString(descriptionString);
        for (DimletDescriptor descriptor : dimletDescriptors) {
            seed = 31 * seed + descriptor.getId();
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

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("descriptionString", descriptionString);
        tagCompound.setInteger("rfCreateCost", rfCreateCost);
        tagCompound.setInteger("rfMaintainCost", rfMaintainCost);
        tagCompound.setInteger("tickCost", tickCost);
        tagCompound.setInteger("ticksLeft", tickCost);
    }

    private int getModifierMultiplier(Map<Pair<DimletType,DimletType>,Integer> modifierMap, DimletType type1, DimletType type2) {
        Integer multiplier = modifierMap.get(Pair.of(type1, type2));
        if (multiplier == null) {
            return 1;
        }
        return multiplier;
    }

    private int getCreationCost(DimletType type, int id) {
        int cost = 0;
        DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
        if (entry != null) {
            cost = entry.getRfCreateCost();
            if (cost == -1) {
                cost = DimletCosts.typeRfCreateCost.get(type);
            }
        }
        return cost;
    }

    private int calculateCreationRfCost(List<Pair<Integer,List<DimletDescriptor>>> dimlets, List<DimletDescriptor> unusedModifiers, int tickCost) {
        int rf = DimletCosts.baseDimensionCreationCost;

        for (Pair<Integer, List<DimletDescriptor>> dimletWithModifier : dimlets) {
            int id = dimletWithModifier.getLeft();
            DimletType type = KnownDimletConfiguration.idToDimlet.get(id).getKey().getType();

            List<DimletDescriptor> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletDescriptor modifier : list) {
                    int mult = getModifierMultiplier(DimletCosts.rfCreateModifierMultiplier, modifier.getType(), type);
                    rf += getCreationCost(modifier.getType(), modifier.getId()) * mult;
                }
            }

            rf += getCreationCost(type, id);
        }

        for (DimletDescriptor modifier : unusedModifiers) {
            rf += getCreationCost(modifier.getType(), modifier.getId());
        }

        // Compensate createCost for the cost to fill the matter receiver at the destination end.
        rf += TeleportConfiguration.RECEIVER_MAXENERGY / tickCost;

        return rf;
    }

    private int getMaintenanceCost(DimletType type, int id) {
        int cost = 0;
        DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
        if (entry != null) {
            cost = entry.getRfMaintainCost();
            if (cost == -1) {
                cost = DimletCosts.typeRfMaintainCost.get(type);
            }
        }
        return cost;
    }

    private int calculateMaintenanceRfCost(List<Pair<Integer,List<DimletDescriptor>>> dimlets) {
        int rf = DimletCosts.baseDimensionMaintenanceCost;
        int rfGain = 0;

        for (Pair<Integer, List<DimletDescriptor>> dimletWithModifier : dimlets) {
            int id = dimletWithModifier.getLeft();
            DimletType type = KnownDimletConfiguration.idToDimlet.get(id).getKey().getType();

            List<DimletDescriptor> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletDescriptor modifier : list) {
                    int mult = getModifierMultiplier(DimletCosts.rfMaintainModifierMultiplier, modifier.getType(), type);
                    rf += getMaintenanceCost(modifier.getType(), modifier.getId()) * mult;
                }
            }

            int c = getMaintenanceCost(type, id);
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

    private int getTickCost(DimletType type, int id) {
        int cost = 0;
        DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
        if (entry != null) {
            cost = entry.getTickCost();
            if (cost == -1) {
                cost = DimletCosts.typeTickCost.get(type);
            }
        }
        return cost;
    }

    private int calculateTickCost(List<Pair<Integer,List<DimletDescriptor>>> dimlets) {
        int ticks = DimletCosts.baseDimensionTickCost;

        for (Pair<Integer, List<DimletDescriptor>> dimletWithModifier : dimlets) {
            int id = dimletWithModifier.getLeft();
            DimletType type = KnownDimletConfiguration.idToDimlet.get(id).getKey().getType();

            List<DimletDescriptor> list = dimletWithModifier.getRight();
            if (list != null) {
                for (DimletDescriptor modifier : list) {
                    int mult = getModifierMultiplier(DimletCosts.tickCostModifierMultiplier, modifier.getType(), type);
                    ticks += getTickCost(modifier.getType(), modifier.getId()) * mult;
                }
            }

            ticks += getTickCost(type, id);
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
        private final Integer id;

        public DimletDescriptor(DimletType type, Integer id) {
            this.type = type;
            this.id = id;
        }

        public DimletType getType() {
            return type;
        }

        public Integer getId() {
            return id;
        }
    }
}
