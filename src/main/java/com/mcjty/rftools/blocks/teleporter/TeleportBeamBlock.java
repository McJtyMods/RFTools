package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TeleportBeamBlock extends Block {

    private IIcon icon;
    private IIcon iconTransparent;

    public TeleportBeamBlock(Material material) {
        super(material);
        setBlockName("teleportBeamBlock");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }


    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "machineTeleporter");
        iconTransparent = iconRegister.registerIcon(RFTools.MODID + ":" + "transparent");
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.DOWN.ordinal() || side == ForgeDirection.UP.ordinal()) {
            return iconTransparent;
        }
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.DOWN.ordinal() || side == ForgeDirection.UP.ordinal()) {
            return iconTransparent;
        }
        return icon;
    }

}
