package com.mcjty.rftools.items.dimensionmonitor;

import cofh.api.energy.IEnergyContainerItem;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

import java.util.List;

public class PhasedFieldGeneratorItem extends Item implements IEnergyContainerItem {

    private int capacity;
    private int maxReceive;
    private int maxExtract;

    private IIcon powerLevel[] = new IIcon[9];

    public PhasedFieldGeneratorItem() {
        setMaxStackSize(1);

        capacity = DimletConfiguration.PHASEDFIELD_MAXENERGY;
        maxReceive = DimletConfiguration.PHASEDFIELD_RECEIVEPERTICK;
        maxExtract = DimletConfiguration.PHASEDFIELD_CONSUMEPERTICK * DimensionTickEvent.MAXTICKS;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0 ; i <= 8 ; i++) {
            powerLevel[i] = iconRegister.registerIcon(RFTools.MODID + ":phasedFieldGeneratorItemL" + i);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconIndex(ItemStack stack) {
        NBTTagCompound tagCompound = stack.getTagCompound();
        int energy = 0;
        if (tagCompound != null) {
            energy = tagCompound.getInteger("Energy");
        }
        int level = (9*energy) / DimletConfiguration.PHASEDFIELD_MAXENERGY;
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
        NBTTagCompound tagCompound = itemStack.getTagCompound();
        if (tagCompound != null) {
            list.add(EnumChatFormatting.BLUE + "Energy: " + tagCompound.getInteger("Energy") + " RF");
        }
        list.add("This RF/charged module gives a temporary");
        list.add("protection while visiting an unpowered dimension.");
        list.add("Use at your own risk and don't let power run out!");
    }

    @Override
    public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
        if (container.stackTagCompound == null) {
            container.stackTagCompound = new NBTTagCompound();
        }
        int energy = container.stackTagCompound.getInteger("Energy");
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));

        if (!simulate) {
            energy += energyReceived;
            container.stackTagCompound.setInteger("Energy", energy);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Energy")) {
            return 0;
        }
        int energy = container.stackTagCompound.getInteger("Energy");
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));

        if (!simulate) {
            energy -= energyExtracted;
            container.stackTagCompound.setInteger("Energy", energy);
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored(ItemStack container) {
        if (container.stackTagCompound == null || !container.stackTagCompound.hasKey("Energy")) {
            return 0;
        }
        return container.stackTagCompound.getInteger("Energy");
    }

    @Override
    public int getMaxEnergyStored(ItemStack container) {
        return capacity;
    }
}
