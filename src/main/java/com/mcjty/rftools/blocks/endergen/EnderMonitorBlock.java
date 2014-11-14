package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.EmptyContainer;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.logic.LogicSlabBlock;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EnderMonitorBlock extends LogicSlabBlock {

    public EnderMonitorBlock(Material material) {
        super(material, "enderMonitorBlock", EnderMonitorTileEntity.class);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERMONITOR;
    }

    @Override
    public GuiContainer createClientGui(EntityPlayer entityPlayer, TileEntity tileEntity) {
        EnderMonitorTileEntity enderMonitorTileEntity = (EnderMonitorTileEntity) tileEntity;
        return new GuiEnderMonitor(enderMonitorTileEntity, new EmptyContainer(entityPlayer));
    }

    @Override
    public String getIdentifyingIconName() {
        return "machineEnderMonitorTop";
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        if (!world.isRemote) {
            registerWithEndergenic(world, x, y, z);
        }
    }

    public void registerWithEndergenic(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        TileEntity te = world.getTileEntity(x + k.offsetX, y + k.offsetY, z + k.offsetZ);
        if (te instanceof EndergenicTileEntity) {
            EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
            endergenicTileEntity.addMonitor(new Coordinate(x, y, z));
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        TileEntity te = world.getTileEntity(x + k.offsetX, y + k.offsetY, z + k.offsetZ);
        if (te instanceof EndergenicTileEntity) {
            EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
            endergenicTileEntity.removeMonitor(new Coordinate(x, y, z));
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        // We don't want to do what LogicSlabBlock does as we don't react on redstone input.
    }
}
