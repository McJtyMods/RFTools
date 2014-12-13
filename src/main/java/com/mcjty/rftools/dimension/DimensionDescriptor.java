package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.teleporter.TeleportConfiguration;
import com.mcjty.rftools.items.dimlets.DimletEntry;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.nbt.NBTTagCompound;

import java.util.*;

/**
 * A unique descriptor of a dimension.
 */
public class DimensionDescriptor {
    private final String descriptionString;
    private final int rfCreateCost;
    private final int rfMaintainCost;
    private final int tickCost;

    private static <K, T> void add(Map<K, List<T>> map, K key, T element) {
        List<T> current = map.get(key);
        if (current == null) {
            current = new ArrayList<T>();
            map.put(key, current);
        }
        current.add(element);
    }

    public DimensionDescriptor(List<DimletDescriptor> descriptors) {
        StringBuilder s = new StringBuilder();

        // Lists of all non-modifier dimlets organized per type.
        Map<DimletType, List<Integer>> dimlets = new HashMap<DimletType, List<Integer>>();

        // Lists of all modifier dimlets that fit with the type they are modifying.
        Map<DimletType, List<DimletDescriptor>> modifiers = new HashMap<DimletType, List<DimletDescriptor>>();

        // A list of all current modifier that haven't been fitted into a type yet.
        List<DimletDescriptor> currentModifiers = new ArrayList<DimletDescriptor>();

        groupDimletsAndModifiers(descriptors, dimlets, modifiers, currentModifiers);
        constructDescriptionString(s, dimlets, modifiers, currentModifiers);

        descriptionString = s.toString();

        tickCost = calculateTickCost(dimlets);
        rfCreateCost = calculateCreationRfCost(dimlets, tickCost);
        rfMaintainCost = calculateMaintenanceRfCost(dimlets);
    }

    private void constructDescriptionString(StringBuilder s, Map<DimletType, List<Integer>> dimlets, Map<DimletType, List<DimletDescriptor>> modifiers, List<DimletDescriptor> currentModifiers) {
        for (DimletType type : DimletType.values()) {
            List<Integer> ids = dimlets.get(type);
            if (ids != null) {
                // Do not sort the digit dimlets.
                if (!type.equals(DimletType.DIMLET_DIGIT)) {
                    Collections.sort(ids);
                }
                // First the modifiers for this type:
                List<DimletDescriptor> mods = modifiers.get(type);
                if (mods != null) {
                    Collections.sort(mods, new Comparator<DimletDescriptor>() {
                        @Override
                        public int compare(DimletDescriptor o1, DimletDescriptor o2) {
                            return Integer.compare(o1.getId(), o2.getId());
                        }
                    });
                    for (DimletDescriptor descriptor : mods) {
                        if (s.length() > 0) {
                            s.append(',');
                        }
                        s.append('#').append(type.getOpcode()).append(descriptor.getType().getOpcode()).append(descriptor.getId());
                    }
                }

                for (Integer id : ids) {
                    if (s.length() > 0) {
                        s.append(',');
                    }
                    s.append(type.getOpcode()).append(id);
                }
            }
        }
        // Now add all unused modifiers to the end.
        for (DimletDescriptor modifier : currentModifiers) {
            if (s.length() > 0) {
                s.append(',');
            }
            s.append('?').append(modifier.getType().getOpcode()).append(modifier.getId());
        }
    }

    private void groupDimletsAndModifiers(List<DimletDescriptor> descriptors, Map<DimletType, List<Integer>> dimlets, Map<DimletType, List<DimletDescriptor>> modifiers, List<DimletDescriptor> currentModifiers) {
        for (DimletDescriptor descriptor : descriptors) {
            DimletType type = descriptor.getType();
            int id = descriptor.getId();
            if (type.isModifier()) {
                // Keep the modifier here until we find a dimlet for which it fits.
                currentModifiers.add(descriptor);
            } else {
                if (!currentModifiers.isEmpty()) {
                    // Check if we collected modifiers that fit with this type.
                    List<DimletDescriptor> copy = new ArrayList<DimletDescriptor>(currentModifiers);
                    // Iterate over a copy so that we can delete from original list.
                    for (DimletDescriptor modifier : copy) {
                        if (type.isModifiedBy(modifier.getType())) {
                            add(modifiers, type, modifier);
                            currentModifiers.remove(modifier);
                        }
                    }
                }
                add(dimlets, type, id);
            }
        }
    }

    public DimensionDescriptor(NBTTagCompound tagCompound) {
        descriptionString = tagCompound.getString("descriptionString");
        rfCreateCost = tagCompound.getInteger("rfCreateCost");
        rfMaintainCost = tagCompound.getInteger("rfMaintainCost");
        tickCost = tagCompound.getInteger("tickCost");
    }

    public static List<DimletDescriptor> parseDescriptionString(String descriptionString) {
        List<DimletDescriptor> result = new ArrayList<DimletDescriptor>();
        if (!descriptionString.isEmpty()) {
            String[] opcodes = descriptionString.split(",");
            for (String oc : opcodes) {
                DimletType type;
                Integer id;
                if (oc.startsWith("#")) {
                    // First comes '#', then type which is being modifed and then the type of the actual dimlet.
                    type = DimletType.getTypeByOpcode(oc.substring(2, 3));
                    id = Integer.parseInt(oc.substring(3));
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


    public static Map<DimletType,List<Integer>> getDimlets(String descriptionString) {
        Map<DimletType,List<Integer>> result = new HashMap<DimletType, List<Integer>>();
        for (DimletType type : DimletType.values()) {
            result.put(type, new ArrayList<Integer>());
        }
        for (DimletDescriptor descriptor : parseDescriptionString(descriptionString)) {
            result.get(descriptor.getType()).add(descriptor.getId());

        }
        return result;
    }

    public Map<DimletType,List<Integer>> getDimlets() {
        return getDimlets(descriptionString);
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

    private int calculateCreationRfCost(Map<DimletType, List<Integer>> input, int tickCost) {
        int rf = KnownDimletConfiguration.baseDimensionCreationCost;
        for (DimletType type : input.keySet()) {
            List<Integer> ids = input.get(type);
            for (Integer id : ids) {
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
                if (entry != null) {
                    int cost = entry.getRfCreateCost();
                    if (cost == -1) {
                        cost = KnownDimletConfiguration.typeRfCreateCost.get(type);
                    }
                    rf += cost;
                }
            }
        }

        // Compensate createCost for the cost to fill the matter receiver at the destination end.
        rf += TeleportConfiguration.RECEIVER_MAXENERGY / tickCost;

        return rf;
    }

    private int calculateMaintenanceRfCost(Map<DimletType, List<Integer>> input) {
        int rf = KnownDimletConfiguration.baseDimensionMaintenanceCost;
        for (DimletType type : input.keySet()) {
            List<Integer> ids = input.get(type);
            for (Integer id : ids) {
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
                if (entry != null) {
                    int cost = entry.getRfMaintainCost();
                    if (cost == -1) {
                        cost = KnownDimletConfiguration.typeRfMaintainCost.get(type);
                    }
                    rf += cost;
                }
            }
        }
        return rf;
    }

    private int calculateTickCost(Map<DimletType, List<Integer>> input) {
        int ticks = KnownDimletConfiguration.baseDimensionTickCost;
        for (DimletType type : input.keySet()) {
            List<Integer> ids = input.get(type);
            for (Integer id : ids) {
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
                if (entry != null) {
                    int cost = entry.getTickCost();
                    if (cost == -1) {
                        cost = KnownDimletConfiguration.typeTickCost.get(type);
                    }
                    ticks += cost;
                }
            }
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
