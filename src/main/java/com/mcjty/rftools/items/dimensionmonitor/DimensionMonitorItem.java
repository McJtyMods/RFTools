package com.mcjty.rftools.items.dimensionmonitor;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.network.PacketGetDimensionEnergy;
import com.mcjty.rftools.network.PacketHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class DimensionMonitorItem extends Item {
    private static long lastTime = 0;

    private IIcon powerLevel[] = new IIcon[9];

    public DimensionMonitorItem() {
        setMaxStackSize(1);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            int id = player.worldObj.provider.dimensionId;
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(player.worldObj);
            DimensionInformation dimensionInformation = dimensionManager.getDimensionInformation(id);
            if (dimensionInformation == null) {
                RFTools.message(player, "Not an RFTools dimension!");
            } else {
                String name = dimensionInformation.getName();
                DimensionStorage storage = DimensionStorage.getDimensionStorage(player.getEntityWorld());
                int power = storage != null ? storage.getEnergyLevel(id) : 0;

                RFTools.message(player, EnumChatFormatting.BLUE + "Name: " + name + " (Id " + id + ")" + EnumChatFormatting.YELLOW + "    Power: " + power + " RF");
                if (player.isSneaking()) {
                    RFTools.message(player, EnumChatFormatting.RED + "Description: " + dimensionInformation.getDescriptor().getDescriptionString());
                    System.out.println("Description:  = " + dimensionInformation.getDescriptor().getDescriptionString());
                }
            }
            return stack;
        }
        return stack;
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0 ; i <= 8 ; i++) {
            powerLevel[i] = iconRegister.registerIcon(RFTools.MODID + ":dimensionMonitorItemL" + i);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack stack) {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        int id = player.worldObj.provider.dimensionId;
        DimensionStorage storage = DimensionStorage.getDimensionStorage(player.worldObj);
        int energyLevel = storage.getEnergyLevel(id);
        int level = (9*energyLevel) / DimletConfiguration.MAX_DIMENSION_POWER;
        if (level < 0) {
            level = 0;
        } else if (level > 8) {
            level = 8;
        }
        return powerLevel[8-level];
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean whatIsThis) {
        super.addInformation(itemStack, player, list, whatIsThis);
        int id = player.worldObj.provider.dimensionId;
        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(player.worldObj);
        DimensionInformation dimensionInformation = dimensionManager.getDimensionInformation(id);
        if (dimensionInformation == null) {
            list.add("Not an RFTools dimension!");
        } else {
            if (System.currentTimeMillis() - lastTime > 500) {
                lastTime = System.currentTimeMillis();
                PacketHandler.INSTANCE.sendToServer(new PacketGetDimensionEnergy(id));
            }
            String name = dimensionInformation.getName();
            DimensionStorage storage = DimensionStorage.getDimensionStorage(player.getEntityWorld());
            int power = storage != null ? storage.getEnergyLevel(id) : 0;

            list.add(EnumChatFormatting.BLUE + "Name: " + name + " (Id " + id + ")");

            list.add(EnumChatFormatting.YELLOW + "Power: " + power + " RF");
        }
    }


}