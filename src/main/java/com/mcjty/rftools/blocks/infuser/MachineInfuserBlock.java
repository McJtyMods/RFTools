package com.mcjty.rftools.blocks.infuser;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class MachineInfuserBlock extends GenericContainerBlock {

    public MachineInfuserBlock() {
        super(Material.iron, MachineInfuserTileEntity.class);
        setBlockName("machineInfuserBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MACHINE_INFUSER;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        MachineInfuserTileEntity MachineInfuserTileEntity = (MachineInfuserTileEntity)world.getTileEntity(x, y, z);

        if (MachineInfuserTileEntity != null) {
            BlockTools.emptyInventoryInWorld(world, x, y, z, block, MachineInfuserTileEntity);
        }

        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    protected void breakWithWrench(World world, int x, int y, int z) {
        MachineInfuserTileEntity MachineInfuserTileEntity = (MachineInfuserTileEntity)world.getTileEntity(x, y, z);

        if (MachineInfuserTileEntity != null) {
            for (int i = 0 ; i < MachineInfuserTileEntity.getSizeInventory() ; i++) {
                MachineInfuserTileEntity.setInventorySlotContents(i, null);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        MachineInfuserTileEntity MachineInfuserTileEntity = (MachineInfuserTileEntity) tileEntity;
        MachineInfuserContainer MachineInfuserContainer = new MachineInfuserContainer(entityPlayer, MachineInfuserTileEntity);
        return new GuiMachineInfuser(MachineInfuserTileEntity, MachineInfuserContainer);
    }

    @Override
    public Container createServerContainer(EntityPlayer entityPlayer, TileEntity tileEntity) {
        return new MachineInfuserContainer(entityPlayer, (MachineInfuserTileEntity) tileEntity);
    }


    @Override
    public String getIdentifyingIconName() {
        return "machineMachineInfuser";
    }
}
