package com.mcjty.container;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraftforge.common.util.ForgeDirection;

public class GenericEnergyHandlerContainer extends GenericContainer {

    private final GenericEnergyHandlerTileEntity energyHandlerTileEntity;

    public GenericEnergyHandlerContainer(ContainerFactory factory, EntityPlayer player, GenericEnergyHandlerTileEntity energyHandlerTileEntity) {
        super(factory, player);
        this.energyHandlerTileEntity = energyHandlerTileEntity;
        energyHandlerTileEntity.setOldRF(-1);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int energyStored = energyHandlerTileEntity.getEnergyStored(ForgeDirection.DOWN);
        if (energyStored != energyHandlerTileEntity.getOldRF()) {
            energyHandlerTileEntity.setOldRF(energyStored);
            for (Object crafter : this.crafters) {
                ICrafting icrafting = (ICrafting) crafter;
                icrafting.sendProgressBarUpdate(this, 1, energyStored);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int type, int value) {
        super.updateProgressBar(type, value);
        if (type == 1) {
            energyHandlerTileEntity.setCurrentRF(value);
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
    }

}
