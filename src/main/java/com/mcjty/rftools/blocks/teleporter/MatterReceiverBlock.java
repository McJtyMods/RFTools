package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MatterReceiverBlock extends GenericContainerBlock {

    private IIcon iconSide;
    private IIcon iconTop;

    public MatterReceiverBlock(Material material) {
        super(material, MatterReceiverTileEntity.class);
        setBlockName("matterReceiverBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_RECEIVER;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        // We don't want what GenericContainerBlock does.
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float sx, float sy, float sz, int meta) {
        int rc = super.onBlockPlaced(world, x, y, z, side, sx, sy, sz, meta);
        if (world.isRemote) {
            return rc;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.addDestination(new Coordinate(x, y, z), world.provider.dimensionId);
        destinations.save(world);
        return rc;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (world.isRemote) {
            return;
        }
        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        destinations.removeDestination(new Coordinate(x, y, z), world.provider.dimensionId);
        destinations.save(world);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + "machineReceiver");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else {
            return iconSide;
        }
    }

}
