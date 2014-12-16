package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.*;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.List;

public class RealizedDimensionTab extends Item {
    private static long lastTime = 0;

    public RealizedDimensionTab() {
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if ((!world.isRemote) && player.isSneaking()) {
            NBTTagCompound tagCompound = stack.getTagCompound();
            RFTools.message(player, tagCompound.getString("descriptionString"));
            int id = tagCompound.getInteger("id");
            if (id != 0) {
                RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
                DimensionInformation information = dimensionManager.getDimensionInformation(id);
                if (information != null) {
                    information.dump(player);
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
        String digitString = "";
        DimletType prevType = null;
        int cnt = 0;
        for (DimensionDescriptor.DimletDescriptor descriptor : DimensionDescriptor.parseDescriptionString(descriptionString)) {
            DimletType type = descriptor.getType();
            int id = descriptor.getId();
            if (type == DimletType.DIMLET_DIGIT) {
                digitString += KnownDimletConfiguration.idToDigit.get(id);
            }
            if (type == prevType) {
                cnt++;
            } else {
                if (prevType != null) {
                    if (prevType != DimletType.DIMLET_DIGIT) {
                        list.add(EnumChatFormatting.GREEN + prevType.getName() + " " + cnt + " dimlets");
                    }
                }
                prevType = type;
                cnt = 1;
            }
        }
        if (prevType != null && cnt != 0) {
            if (prevType != DimletType.DIMLET_DIGIT) {
                list.add(EnumChatFormatting.GREEN + prevType.getName() + " " + cnt + " dimlets");
            }
        }
        if (!digitString.isEmpty()) {
            list.add(EnumChatFormatting.GREEN + "Digits " + digitString);
        }
    }
}
