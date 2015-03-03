package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.blocks.ModBlocks;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.IBlockAccess;

public class SolidShieldBlock extends AbstractShieldBlock {

    public static int RENDERID_SHIELDBLOCK;


    public SolidShieldBlock() {
        super();
        setBlockName("solidShieldBlock");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RENDERID_SHIELDBLOCK;
    }


    @SideOnly(Side.CLIENT)
    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        if (world.getBlock(x, y, z) == ModBlocks.solidShieldBlock) {
            return false;
        }
        return true;
    }


}
