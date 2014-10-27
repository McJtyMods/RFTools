package com.mcjty.rftools.blocks.endergen;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class EndergenicBlock extends GenericContainerBlock {

    private IIcon icon;
    private IIcon iconUp;

    public EndergenicBlock(Material material) {
        super(material, EndergenicTileEntity.class);
        setBlockName("endergenicBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERGENIC;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "machineEndergenic");
        iconUp = iconRegister.registerIcon(RFTools.MODID + ":" + "machineEndergenicUp");
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        super.onBlockPlacedBy(world, x, y, z, entityLivingBase, itemStack);
        checkForMonitor4Sides(world, x, y, z);
    }

    private void checkForMonitor4Sides(World world, int x, int y, int z) {
        if (!world.isRemote) {
            checkForMonitor(world, x+1, y, z);
            checkForMonitor(world, x-1, y, z);
            checkForMonitor(world, x, y, z+1);
            checkForMonitor(world, x, y, z-1);
        }
    }

    private void checkForMonitor(World world, int x, int y, int z) {
        if (ModBlocks.enderMonitorBlock.equals(world.getBlock(x, y, z))) {
            ModBlocks.enderMonitorBlock.registerWithEndergenic(world, x, y, z);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        boolean wrenchUsed = testWrenchUsage(x, y, z, player);
        if (wrenchUsed) {
            if (world.isRemote) {
                EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) world.getTileEntity(x, y, z);
                world.playSound(x, y, z, "note.pling", 1.0f, 1.0f, false);
                endergenicTileEntity.useWrench(player);
            }
            return true;
        } else {
            return super.onBlockActivated(world, x, y, z, player, side, sx, sy, sz);
        }
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        meta = BlockTools.setRedstoneSignal(meta, powered);
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return getIcon(side, 0);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconUp;
        } else {
            return icon;
        }
    }

}
