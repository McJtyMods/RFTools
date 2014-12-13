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

    public DimensionDescriptor(Map<DimletType, List<Integer>> input) {
        StringBuilder s = new StringBuilder();

        for (DimletType type : DimletType.values()) {
            List<Integer> ids = input.get(type);
            if (ids != null) {
                // Do not sort the digit dimlets.
                if (!type.equals(DimletType.DIMLET_DIGIT)) {
                    Collections.sort(ids);
                }
                for (Integer id : ids) {
                    if (s.length() > 0) {
                        s.append(',');
                    }
                    s.append(type.getOpcode()).append(id);
                }
            }
        }
        descriptionString = s.toString();

        tickCost = calculateTickCost(input);
        rfCreateCost = calculateCreationRfCost(input, tickCost);
        rfMaintainCost = calculateMaintenanceRfCost(input);
    }

    public static List<DimletDescriptor> parseDescriptionString(String descriptionString) {
        List<DimletDescriptor> result = new ArrayList<DimletDescriptor>();
        if (!descriptionString.isEmpty()) {
            String[] opcodes = descriptionString.split(",");
            for (String oc : opcodes) {
                DimletType type = DimletType.getTypeByOpcode(oc.substring(0, 1));
                Integer id = Integer.parseInt(oc.substring(1));
                result.add(new DimletDescriptor(type, id));
            }
        }
        return result;
    }


    public DimensionDescriptor(NBTTagCompound tagCompound) {
        descriptionString = tagCompound.getString("descriptionString");
        rfCreateCost = tagCompound.getInteger("rfCreateCost");
        rfMaintainCost = tagCompound.getInteger("rfMaintainCost");
        tickCost = tagCompound.getInteger("tickCost");
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
