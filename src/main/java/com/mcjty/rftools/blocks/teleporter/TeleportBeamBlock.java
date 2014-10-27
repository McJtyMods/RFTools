package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class TeleportBeamBlock extends Block {

    public static int RENDERID_BEAM;

    private IIcon icon;
    private IIcon iconWarn;
    private IIcon iconTransparent;

    public TeleportBeamBlock(Material material) {
        super(material);
        setBlockName("teleportBeamBlock");
        setBlockUnbreakable();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {
        // We don't want left and right clicks to do anything for this beam
        return null;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "machineTeleporter");
        iconWarn = iconRegister.registerIcon(RFTools.MODID + ":" + "machineTeleporterWarn");
        iconTransparent = iconRegister.registerIcon(RFTools.MODID + ":" + "transparent");
    }

    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public int getRenderType() {
        return RENDERID_BEAM;
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.DOWN.ordinal() || side == ForgeDirection.UP.ordinal()) {
            return iconTransparent;
        }
        int meta = blockAccess.getBlockMetadata(x, y, z);
        if ((meta & 1) != 0) {
            return iconWarn;
        }
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.DOWN.ordinal() || side == ForgeDirection.UP.ordinal()) {
            return iconTransparent;
        }
        if ((meta & 1) != 0) {
            return iconWarn;
        }
        return icon;
    }

}
