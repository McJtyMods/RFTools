package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.description.DimensionDescriptor;
import com.mcjty.rftools.dimension.network.PacketGetDimensionEnergy;
import com.mcjty.rftools.network.PacketHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @SideOnly(Side.CLIENT)
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
                DimensionInformation information = RfToolsDimensionManager.getDimensionManager(player.getEntityWorld()).getDimensionInformation(id);
                if (information == null) {
                    list.add(EnumChatFormatting.RED + "Dimension information Missing!");
                } else {
                    list.add(EnumChatFormatting.BLUE + "Dimension ready!");
                    int maintainCost = tagCompound.getInteger("rfMaintainCost");
                    int actualCost = information.getActualRfCost();
                    if (actualCost == maintainCost || actualCost == 0) {
                        list.add(EnumChatFormatting.YELLOW + "    Maintenance cost: " + maintainCost + " RF/tick");
                    } else {
                        list.add(EnumChatFormatting.YELLOW + "    Maintenance cost: " + actualCost + " RF/tick (Specified: " + maintainCost + " RF/tick)");
                    }
                    if (id != 0) {
                        if (System.currentTimeMillis() - lastTime > 500) {
                            lastTime = System.currentTimeMillis();
                            PacketHandler.INSTANCE.sendToServer(new PacketGetDimensionEnergy(id));
                        }

                        DimensionStorage storage = DimensionStorage.getDimensionStorage(player.getEntityWorld());
                        int power = storage.getEnergyLevel(id);
                        list.add(EnumChatFormatting.YELLOW + "    Current power: " + power + " RF");
                    }
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
        Map<DimletType,List<DimletKey>> dimletTypeListMap = new HashMap<DimletType, List<DimletKey>>();
        for (DimensionDescriptor.DimletDescriptor descriptor : DimensionDescriptor.parseDescriptionString(descriptionString)) {
            DimletType type = descriptor.getType();
            if (!dimletTypeListMap.containsKey(type)) {
                dimletTypeListMap.put(type, new ArrayList<DimletKey>());
            }
            dimletTypeListMap.get(descriptor.getType()).add(descriptor.getKey());
        }

        for (Map.Entry<DimletType, List<DimletKey>> entry : dimletTypeListMap.entrySet()) {
            DimletType type = entry.getKey();
            List<DimletKey> keys = entry.getValue();
            if (keys != null && !keys.isEmpty()) {
                if (type == DimletType.DIMLET_DIGIT) {
                    String digitString = "";
                    for (DimletKey key : keys) {
                        digitString += DimletObjectMapping.idToDigit.get(key);
                    }
                    list.add(EnumChatFormatting.GREEN + "Digits " + digitString);
                } else {
                    if (keys.size() == 1) {
                        list.add(EnumChatFormatting.GREEN + type.getName() + " 1 dimlet");
                    } else {
                        list.add(EnumChatFormatting.GREEN + type.getName() + " " + keys.size() + " dimlets");
                    }
                }
            }
        }
    }
}
