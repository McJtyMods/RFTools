package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.PacketGetDimensionEnergy;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RealizedDimensionTab extends Item {
    private static long lastTime = 0;

    public RealizedDimensionTab() {
        setMaxStackSize(1);
    }

    private void logDebug(EntityPlayer player, String message) {
        RFTools.message(player, EnumChatFormatting.YELLOW + message);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if ((!world.isRemote) && player.isSneaking()) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            logDebug(player, tagCompound.getString("descriptionString"));
            int id = tagCompound.getInteger("id");
            if (id != 0) {
                RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
                DimensionInformation information = dimensionManager.getDimensionInformation(id);
                if (information != null) {
                    TerrainType terrainType = information.getTerrainType();
                    logDebug(player, "    Terrain: " + terrainType.toString());
                    for (BiomeGenBase biome : information.getBiomes()) {
                        logDebug(player, "    Biome: " + biome.biomeName);
                    }
                    for (FeatureType featureType : information.getFeatureTypes()) {
                        logDebug(player, "    Feature: " + featureType.toString());
                    }
                    for (StructureType structureType : information.getStructureTypes()) {
                        logDebug(player, "    Structure: " + structureType.toString());
                    }
                }
            }
        }
        return stack;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            String name = tagCompound.getString("name");
            int id = 0;
            if (name != null) {
                id = tagCompound.getInteger("id");
                if (id == 0) {
                    list.add(EnumChatFormatting.BLUE + "Name: " + name);
                } else {
                    list.add(EnumChatFormatting.BLUE + "Name: " + name + " (Id " + id + ")");
                }
            }
            String descriptionString = tagCompound.getString("descriptionString");
            constructDescriptionHelp(list, descriptionString);

            Integer ticksLeft = tagCompound.getInteger("ticksLeft");
            if (ticksLeft == 0) {
                list.add(EnumChatFormatting.BLUE + "Dimension ready!");
                int maintainCost = tagCompound.getInteger("rfMaintainCost");
                list.add(EnumChatFormatting.YELLOW + "    Maintenance cost: " + maintainCost + " RF/tick");
                if (id != 0) {
                    if (System.currentTimeMillis() - lastTime > 500) {
                        lastTime = System.currentTimeMillis();
                        PacketHandler.INSTANCE.sendToServer(new PacketGetDimensionEnergy(id));
                    }

                    DimensionStorage storage = DimensionStorage.getDimensionStorage(player.getEntityWorld());
                    int power = storage.getEnergyLevel(id);
                    list.add(EnumChatFormatting.YELLOW + "    Current power: " + power + " RF");
                }
            } else {
                int createCost = tagCompound.getInteger("rfCreateCost");
                int maintainCost = tagCompound.getInteger("rfMaintainCost");
                int tickCost = tagCompound.getInteger("tickCost");
                int percentage = (tickCost - ticksLeft) * 100 / tickCost;
                list.add(EnumChatFormatting.BLUE + "Dimension progress: " + percentage + "%");
                list.add(EnumChatFormatting.YELLOW + "    Creation cost: " + createCost + " RF/tick");
                list.add(EnumChatFormatting.YELLOW + "    Maintenance cost: " + maintainCost + " RF/tick");
                list.add(EnumChatFormatting.YELLOW + "    Tick cost: " + tickCost + " ticks");
            }
        }
    }

    private void constructDescriptionHelp(List list, String descriptionString) {
        String[] opcodes = descriptionString.split(",");
        DimletType prevType = null;
        int cnt = 0;
        for (String oc : opcodes) {
            DimletType type = DimletType.getTypeByOpcode(oc.substring(0, 1));
            if (type == prevType) {
                cnt++;
            } else {
                if (prevType != null) {
                    list.add(EnumChatFormatting.GREEN + prevType.getName() + " " + cnt + " dimlets");
                }
                prevType = type;
                cnt = 1;
            }
        }
        if (prevType != null && cnt != 0) {
            list.add(EnumChatFormatting.GREEN + prevType.getName() + " " + cnt + " dimlets");
        }
    }
}
